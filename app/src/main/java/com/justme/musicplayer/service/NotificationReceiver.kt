package com.justme.musicplayer.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.justme.musicplayer.MusicPlayerService
import com.justme.musicplayer.MainActivity
import com.justme.musicplayer.utils.MusicSharedPref

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
                    MainActivity.Companion.buttonClick.postValue(1)
                }

                "com.example.musicplayer.action.PAUSE_MUSIC" -> {
                    MainActivity.Companion.buttonClick.postValue(2)
                }

                "com.example.musicplayer.action.NEXT_MUSIC" -> {
                  MainActivity.Companion.buttonClick.postValue(3)
                }
            }
        }
    }
}