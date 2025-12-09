package com.justme.musicplayer

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media.session.MediaButtonReceiver
import com.justme.musicplayer.service.NotificationReceiver
import com.justme.musicplayer.utils.Constants

class MusicPlayerService : Service() {

    private lateinit var builder: NotificationCompat.Builder
    private lateinit var mediaSession: MediaSessionCompat
    private var playbackPosition: Int = 0
    private var isMediaPlayerInitialized: Boolean = false

    private val noisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                // Pause when headphones / audio output becomes noisy (including bluetooth disconnect)
                MainActivity.buttonClick.postValue(2)
            }
        }
    }

    companion object {
        var isServiceRunning = false
        private const val NOTIFICATION_ID = 1
        lateinit var mediaPlayer: MediaPlayer

        fun isMusicPlaying(): Boolean {
            return if (isInit()) {
                mediaPlayer.isPlaying
            } else {
                false
            }
        }

        fun isInit(): Boolean {
            return ::mediaPlayer.isInitialized
        }
    }

    override fun onCreate() {
        super.onCreate()
        isServiceRunning = true
        registerReceiver(noisyReceiver, IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY))
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        try {
            unregisterReceiver(noisyReceiver)
        } catch (e: Exception) {
            // ignore
        }
        if (MainActivity.isPlaying.value == true) {
            stopMusic()
        }
        if (::mediaSession.isInitialized) {
            mediaSession.release()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Let MediaButtonReceiver handle media button intents from bluetooth/headset
        if (intent?.action == Intent.ACTION_MEDIA_BUTTON) {
            MediaButtonReceiver.handleIntent(mediaSession, intent)
            return START_NOT_STICKY
        }

        when (intent?.action) {
            Constants.ACTION.INIT_MUSIC -> {
                startForeground(NOTIFICATION_ID, createNotification())
                initMusic()
            }

            Constants.ACTION.START_FOREGROUND_ACTION -> {
                startForeground(NOTIFICATION_ID, createNotification())
                MainActivity.isPlaying.value = true
                playMusicFirst()
            }

            Constants.ACTION.STOP_FOREGROUND_ACTION -> {
                MainActivity.isPlaying.value = false
                stopMusic()
                stopSelf()
            }

            Constants.ACTION.PLAY_MUSIC -> {
                if (isMediaPlayerInitialized && mediaPlayer.isPlaying) {
                    MainActivity.isPlaying.value = false
                    pauseMusic()
                } else {
                    MainActivity.isPlaying.value = true
                    continuePlay()
                }
            }

            Constants.ACTION.STOP_MUSIC -> {
                MainActivity.isPlaying.value = false
                stopMusic()
            }

            Constants.ACTION.CHANGE_MUSIC -> {
                MainActivity.isPlaying.value = true
                changeMusic()
            }

            Constants.ACTION.PAUSE_MUSIC -> {
                MainActivity.isPlaying.value = false
                pauseMusic()
            }
        }
        return START_NOT_STICKY
    }

    private fun changeMusic() {
        if (isMediaPlayerInitialized && mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        mediaPlayer.reset()
        mediaPlayer.setDataSource(application, MainActivity.audio.value!!.uri)
        mediaPlayer.prepare()
        mediaPlayer.start()
        isMediaPlayerInitialized = true
        updatePlaybackState()
        updateNotification()
    }

    private fun initMusic() {
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(application, MainActivity.audio.value!!.uri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                prepare()
                isMediaPlayerInitialized = true
                setOnPreparedListener {
                    MainActivity.initSeekValue.postValue(true)
                    updatePlaybackState()
                }
                setOnCompletionListener {
                    MainActivity.buttonClick.postValue(3)
                }
                requestAudioFocus()
            }
            updatePlaybackState()
            updateNotification()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun continuePlay() {
        if (isMediaPlayerInitialized && !mediaPlayer.isPlaying) {
            mediaPlayer.seekTo(playbackPosition)
            mediaPlayer.start()
            updatePlaybackState()
            updateNotification()
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return MyBinder()
    }

    class MyBinder : Binder() {
        val service: MusicPlayerService
            get() = MusicPlayerService()
    }

    private fun createNotification(): Notification {
        mediaSession = MediaSessionCompat(this, "MusicService")
        mediaSession.setFlags(
            MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                    MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
        )
        // Provide a media button pending intent so bluetooth/headset buttons are routed here
        val mediaButtonPendingIntent = MediaButtonReceiver.buildMediaButtonPendingIntent(
            this,
            PlaybackStateCompat.ACTION_PLAY_PAUSE
        )
        mediaSession.setMediaButtonReceiver(mediaButtonPendingIntent)

        mediaSession.setCallback(object : MediaSessionCompat.Callback() {
            override fun onPlay() {
                super.onPlay()
                MainActivity.buttonClick.postValue(2)
                requestAudioFocus()
            }

            override fun onPause() {
                super.onPause()
                MainActivity.buttonClick.postValue(2)
            }

            override fun onSkipToNext() {
                super.onSkipToNext()
                MainActivity.buttonClick.postValue(3)
            }

            override fun onSkipToPrevious() {
                super.onSkipToPrevious()
                MainActivity.buttonClick.postValue(1)
            }

            override fun onStop() {
                super.onStop()
                stopMusic()
            }

            override fun onMediaButtonEvent(mediaButtonIntent: Intent?): Boolean {
                // Let MediaButtonReceiver handle and route to callbacks
                return super.onMediaButtonEvent(mediaButtonIntent)
            }
        })
        mediaSession.isActive = true

        val prevIntent = Intent(this, NotificationReceiver::class.java)
        prevIntent.action = "com.example.musicplayer.action.PREV_MUSIC"
        val playIntent = Intent(this, NotificationReceiver::class.java)
        playIntent.action = "com.example.musicplayer.action.PAUSE_MUSIC"
        val nextIntent = Intent(this, NotificationReceiver::class.java)
        nextIntent.action = "com.example.musicplayer.action.NEXT_MUSIC"

        val pendingPrevIntent = PendingIntent.getBroadcast(
            applicationContext, 0, prevIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val pendingPlayIntent = PendingIntent.getBroadcast(
            applicationContext, 0, playIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val pendingNextIntent = PendingIntent.getBroadcast(
            applicationContext, 0, nextIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        builder = NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
            .setLargeIcon(BitmapFactory.decodeFile(MainActivity.audio.value!!.data))
            .setContentTitle(MainActivity.audio.value!!.name)
            .setContentText(MainActivity.audio.value!!.name)
            .addAction(R.drawable.baseline_skip_previous_24, "Previous", pendingPrevIntent)
            .addAction(R.drawable.baseline_play_arrow_24, "Play", pendingPlayIntent)
            .addAction(R.drawable.baseline_skip_next_24, "Next", pendingNextIntent)
            .setSmallIcon(R.drawable.baseline_music_note_24)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOnlyAlertOnce(true)
            .setWhen(0)

        return builder.build()
    }

    private fun playMusicFirst() {
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(application, MainActivity.audio.value!!.uri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                prepare()
                start()
            }
            isMediaPlayerInitialized = true
            updatePlaybackState()
            updateNotification()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun pauseMusic() {
        if (isMediaPlayerInitialized && mediaPlayer.isPlaying) {
            playbackPosition = mediaPlayer.currentPosition
            mediaPlayer.pause()
            updatePlaybackState()
            updateNotification()
        }
    }

    private fun stopMusic() {
        if (isMediaPlayerInitialized) {
            try {
                if (mediaPlayer.isPlaying) mediaPlayer.stop()
            } catch (e: Exception) {
                // ignore
            } finally {
                mediaPlayer.release()
                isMediaPlayerInitialized = false
                updatePlaybackState()
                updateNotification()
            }
        }
    }

    private fun updateNotification() {
        val playText = if (MainActivity.isPlaying.value == true) "Pause" else "Play"
        val playPauseResource =
            if (MainActivity.isPlaying.value == true) R.drawable.baseline_pause_24 else R.drawable.baseline_play_arrow_24
        val prevIntent = Intent(this, NotificationReceiver::class.java)
        prevIntent.action = "com.example.musicplayer.action.PREV_MUSIC"
        val playIntent = Intent(this, NotificationReceiver::class.java)
        playIntent.action = "com.example.musicplayer.action.PAUSE_MUSIC"
        val nextIntent = Intent(this, NotificationReceiver::class.java)
        nextIntent.action = "com.example.musicplayer.action.NEXT_MUSIC"
        val pendingPrevIntent = PendingIntent.getBroadcast(
            applicationContext,
            0,
            prevIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val pendingPlayIntent = PendingIntent.getBroadcast(
            applicationContext,
            0,
            playIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val pendingNextIntent = PendingIntent.getBroadcast(
            applicationContext,
            0,
            nextIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        builder
            .clearActions()
            .setContentTitle(MainActivity.audio.value!!.name)
            .setContentText(MainActivity.audio.value!!.name)
            .addAction(R.drawable.baseline_skip_previous_24, "Prev", pendingPrevIntent)
            .addAction(playPauseResource, playText, pendingPlayIntent)
            .addAction(R.drawable.baseline_skip_next_24, "Next", pendingNextIntent)

        with(NotificationManagerCompat.from(applicationContext))
        {
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            notify(NOTIFICATION_ID, builder.build())
        }
    }

    private fun updatePlaybackState() {
        if (!::mediaSession.isInitialized) return
        val actions = (PlaybackStateCompat.ACTION_PLAY
                or PlaybackStateCompat.ACTION_PAUSE
                or PlaybackStateCompat.ACTION_PLAY_PAUSE
                or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                or PlaybackStateCompat.ACTION_STOP)
        val state = if (MainActivity.isPlaying.value == true) {
            PlaybackStateCompat.Builder()
                .setActions(actions)
                .setState(PlaybackStateCompat.STATE_PLAYING, playbackPosition.toLong(), 1.0f)
                .build()
        } else {
            PlaybackStateCompat.Builder()
                .setActions(actions)
                .setState(PlaybackStateCompat.STATE_PAUSED, playbackPosition.toLong(), 0f)
                .build()
        }
        mediaSession.setPlaybackState(state)
    }

    private fun requestAudioFocus() {
        val audioManager = getSystemService(AudioManager::class.java)
        val result = audioManager.requestAudioFocus(
            { focusChange ->
                when (focusChange) {
                    AudioManager.AUDIOFOCUS_GAIN -> {
                        if (!mediaPlayer.isPlaying) mediaPlayer.start()
                    }

                    AudioManager.AUDIOFOCUS_LOSS -> stopMusic()
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> pauseMusic()
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                        if (mediaPlayer.isPlaying) mediaPlayer.setVolume(0.1f, 0.1f)
                    }
                }
            },
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN
        )
    }

}
