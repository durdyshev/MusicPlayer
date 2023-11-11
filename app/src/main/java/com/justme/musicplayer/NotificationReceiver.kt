package com.justme.musicplayer

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.content.getSystemService
import com.justme.musicplayer.MainActivity.Companion.audioList

class NotificationReceiver : BroadcastReceiver() {
    private lateinit var context: Context
    override fun onReceive(p0: Context?, p1: Intent?) {
        context = p0!!
        val intent = Intent(p0, MusicPlayerService::class.java)
        if (p1?.action != null) {
            when (p1.action) {
                "com.example.musicplayer.action.PREV_MUSIC" -> {
                    if (isMyServiceRunning(MusicPlayerService::class.java)) {
                        if (MainActivity.isPlaying.value!!) {
                            intent.action = Constants.ACTION.STOP_MUSIC
                            MainActivity.isPlaying.value = false
                            p0.startService(intent)
                        }
                        if (MainActivity.position.value == 0) {
                            MainActivity.position.value = audioList.size - 1

                            MainActivity.audio.value = audioList[MainActivity.position.value!!]
                        } else {
                            MainActivity.position.value = MainActivity.position.value!! - 1
                            MainActivity.audio.value = audioList[MainActivity.position.value!!]
                        }
                        intent.action = Constants.ACTION.PLAY_MUSIC
                        MainActivity.isPlaying.value = true
                        p0.startService(intent)

                    } else {
                        intent.action = Constants.ACTION.START_FOREGROUND_ACTION
                        MainActivity.isPlaying.value = true
                        p0.startService(intent)
                    }
                }

                "com.example.musicplayer.action.PAUSE_MUSIC" -> {
                    Log.e("aaaa", "bbb")
                    if (isMyServiceRunning(MusicPlayerService::class.java)) {
                        if (MainActivity.isPlaying.value!!) {
                            intent.action = Constants.ACTION.PAUSE_MUSIC
                            MainActivity.isPlaying.value = false
                            p0.startService(intent)
                        } else {
                            intent.action = Constants.ACTION.PLAY_MUSIC
                            MainActivity.isPlaying.value = true
                            p0.startService(intent)
                        }
                    } else {
                        intent.action = Constants.ACTION.START_FOREGROUND_ACTION
                        MainActivity.isPlaying.value = true
                        p0.startService(intent)
                        //binding.playImg.setImageResource(R.drawable.baseline_pause_24)

                    }
                }

                "com.example.musicplayer.action.NEXT_MUSIC" -> {
                    if (isMyServiceRunning(MusicPlayerService::class.java)) {
                        if (MainActivity.isPlaying.value!!) {
                            intent.action = Constants.ACTION.STOP_MUSIC
                            MainActivity.isPlaying.value = false
                            p0.startService(intent)
                        }
                        if (MainActivity.position.value == audioList.size - 1) {
                            MainActivity.position.value = 0
                            MainActivity.audio.value = audioList[MainActivity.position.value!!]
                        } else {
                            MainActivity.position.value = MainActivity.position.value!! + 1
                            MainActivity.audio.value = audioList[MainActivity.position.value!!]
                        }
                        intent.action = Constants.ACTION.PLAY_MUSIC
                        MainActivity.isPlaying.value = true
                        p0.startService(intent)
                    } else {
                        intent.action = Constants.ACTION.START_FOREGROUND_ACTION
                        MainActivity.isPlaying.value = true
                        p0.startService(intent)
                    }
                }
            }
        }
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = context.getSystemService() as ActivityManager?
        for (service in manager!!.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }
}