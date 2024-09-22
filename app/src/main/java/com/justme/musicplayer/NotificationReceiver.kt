package com.justme.musicplayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.justme.musicplayer.MainActivity.Companion.isPlaying
import com.justme.musicplayer.use_cases.DecreaseAudioPosition.decreaseAudioPosition
import com.justme.musicplayer.use_cases.IncreaseAudioPosition.increaseAudioPosition

class NotificationReceiver : BroadcastReceiver() {
    private lateinit var context: Context
    private lateinit var intent: Intent
    private lateinit var musicSharedPref: MusicSharedPref
    override fun onReceive(p0: Context?, p1: Intent?) {

        context = p0 ?: return
        intent = Intent(p0, MusicPlayerService::class.java)
        musicSharedPref = MusicSharedPref(p0)
        if (p1?.action != null) {
            when (p1.action) {
                "com.example.musicplayer.action.PREV_MUSIC" -> {
                    MainActivity.buttonClick.postValue(1)
                }

                "com.example.musicplayer.action.PAUSE_MUSIC" -> {
                    MainActivity.buttonClick.postValue(2)
                }

                "com.example.musicplayer.action.NEXT_MUSIC" -> {
                  MainActivity.buttonClick.postValue(3)
                }
            }
        }
    }
}