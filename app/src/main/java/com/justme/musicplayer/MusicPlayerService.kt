package com.justme.musicplayer

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media.session.MediaButtonReceiver

class MusicPlayerService : Service() {

    private lateinit var builder: NotificationCompat.Builder
    private lateinit var mediaSession: MediaSessionCompat
    private var playbackPosition: Int = 0 // Track the last known position
    private var isMediaPlayerInitialized: Boolean = false // Track if mediaPlayer is initialized

    companion object {
        var isServiceRunning = false // Tracks if the service is running
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
        isServiceRunning = true // Set when service starts
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false // Set when service stops
        if (MainActivity.isPlaying.value == true) {
            stopMusic()
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        when (intent.action) {
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
                if (mediaPlayer.isPlaying) {
                    MainActivity.isPlaying.value = false
                    pauseMusic() // Pauses and saves the playback position
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
                pauseMusic() // Pauses and saves the playback position
            }
        }
        return START_NOT_STICKY
    }

    private fun changeMusic() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        mediaPlayer.reset()
        mediaPlayer.setDataSource(application, MainActivity.audio.value!!.uri)
        mediaPlayer.prepare()
        mediaPlayer.start()
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
                isMediaPlayerInitialized = true // Mark mediaPlayer as initialized
                setOnPreparedListener {
                    MainActivity.initSeekValue.postValue(true)
                }
                setOnCompletionListener {
                    MainActivity.buttonClick.postValue(3)
                }
                requestAudioFocus()
            }
            updateNotification()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun continuePlay() {
        if (isMediaPlayerInitialized && !mediaPlayer.isPlaying) {
            mediaPlayer.seekTo(playbackPosition) // Resume from saved position
            mediaPlayer.start() // Start or resume playback
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
        mediaSession = MediaSessionCompat(this, "PlayerAudio")
        mediaSession.setFlags(
            MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                    MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
        )
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
            updateNotification()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun pauseMusic() {
        if (isMediaPlayerInitialized && mediaPlayer.isPlaying) {
            playbackPosition = mediaPlayer.currentPosition // Save the current position
            mediaPlayer.pause() // Pauses the media and remembers the current position
            updateNotification()
        }
    }

    private fun stopMusic() {
        if (isMediaPlayerInitialized && mediaPlayer.isPlaying) {
            mediaPlayer.stop()
            mediaPlayer.release()
            isMediaPlayerInitialized = false // Mark mediaPlayer as uninitialized
            updateNotification()
        }
    }

    private fun updateNotification() {
        val playText = if (MainActivity.isPlaying.value!!) "Pause" else "Play"
        val playPauseResource =
            if (MainActivity.isPlaying.value!!) R.drawable.baseline_pause_24 else R.drawable.baseline_play_arrow_24
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
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT // setting the mutability flag
        )
        val pendingPlayIntent = PendingIntent.getBroadcast(
            applicationContext,
            0,
            playIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT // setting the mutability flag
        )
        val pendingNextIntent = PendingIntent.getBroadcast(
            applicationContext,
            0,
            nextIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT // setting the mutability flag
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

    private fun requestAudioFocus() {
        val audioManager = getSystemService(AudioManager::class.java)
        val result = audioManager.requestAudioFocus(
            { focusChange ->
                when (focusChange) {
                    AudioManager.AUDIOFOCUS_GAIN -> {
                        if (mediaPlayer == null) initMusic()
                        else if (!mediaPlayer.isPlaying) mediaPlayer.start()
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
