package com.justme.musicplayer.service


import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.justme.musicplayer.receiver.NotificationReceiver
import com.justme.musicplayer.R
import com.justme.musicplayer.ui.MainActivity
import com.justme.musicplayer.utils.ActionPlaying
import com.justme.musicplayer.utils.Constants


class MusicPlayerService : Service() {

    private lateinit var builder: NotificationCompat.Builder
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var actionPlaying: ActionPlaying
    override fun onCreate() {
        super.onCreate()


    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (intent.action == Constants.ACTION.START_FOREGROUND_ACTION) {
            startForeground(NOTIFICATION_ID, createNotification())
            MainActivity.isPlaying.value = true
            playMusic()
        } else if (intent.action == Constants.ACTION.STOP_FOREGROUND_ACTION) {
            //stopForeground(true)
            MainActivity.isPlaying.value = false

            stopMusic()
            stopSelf()
        } else if (intent.action == Constants.ACTION.PLAY_MUSIC) {
            MainActivity.isPlaying.value = true
            playMusic()

        } else if (intent.action == Constants.ACTION.STOP_MUSIC) {
            MainActivity.isPlaying.value = false
            stopMusic()
        } else if (intent.action == Constants.ACTION.PAUSE_MUSIC) {
            MainActivity.isPlaying.value = false
            pauseMusic()
        } else if (intent.action == Constants.ACTION.PAUSE_MUSIC) {
            MainActivity.isPlaying.value = true
            continuePlay()
        } else if (intent.action == Constants.ACTION.START_MUSIC) {
            startForeground(NOTIFICATION_ID, createNotification())
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        if (MainActivity.isPlaying.value!!) {
            stopMusic()
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    class MyBinder : Binder() {
        val service: MusicPlayerService
            get() = MusicPlayerService()
    }
    @SuppressLint("LaunchActivityFromNotification")
    private fun createNotification(): Notification {
        mediaSession = MediaSessionCompat(this, "PlayerAudio")

        val prevIntent= Intent(this, NotificationReceiver::class.java)
        prevIntent.action="com.example.musicplayer.action.PREV_MUSIC"
        val playIntent= Intent(this, NotificationReceiver::class.java)
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

       builder = NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
           // .setSmallIcon(trackFilesArrayList[position].thumbnail)
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

    private fun playMusic() {
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
        if (!mediaPlayer.isPlaying) {
            mediaPlayer.start()
        }
        updateNotification()
    }

    private fun continuePlay() {
        if (!mediaPlayer.isPlaying) {
            mediaPlayer.start()
        }
        updateNotification()
    }

    private fun stopMusic() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
            mediaPlayer.release()
            updateNotification()
        }
    }

    private fun pauseMusic() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            updateNotification()
        }
    }
    fun setCallback(actionPlaying: ActionPlaying){
        this.actionPlaying=actionPlaying
    }
    private fun imgSource(path: String): ByteArray? {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(path)
        val pic = retriever.embeddedPicture
        retriever.release()
        return pic
    }
    private fun updateNotification() {
        val playText=if (MainActivity.isPlaying.value!!) "Pause" else "Play"
        val playPauseResource = if (MainActivity.isPlaying.value!!) R.drawable.baseline_pause_24 else R.drawable.baseline_play_arrow_24
        val prevIntent= Intent(this, NotificationReceiver::class.java)
        prevIntent.action="com.example.musicplayer.action.PREV_MUSIC"
        val playIntent= Intent(this, NotificationReceiver::class.java)
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
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            notify(NOTIFICATION_ID, builder.build())
        }
    }

    companion object {
        private const val NOTIFICATION_ID = 1
    }
}
