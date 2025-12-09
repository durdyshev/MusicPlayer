package com.justme.musicplayer.utils

import android.content.Context
import com.justme.musicplayer.MainActivity

class MusicSharedPref(context: Context) {
    private var sharedMusicPref = context.getSharedPreferences("Music", Context.MODE_PRIVATE)
    private var prefEditor = sharedMusicPref.edit()

    fun saveShared() {
        prefEditor.putInt("musicPos", MainActivity.Companion.position.value!!)
        prefEditor.putString("musicName", MainActivity.Companion.audio.value!!.name)
        prefEditor.apply()
    }

    fun getMusicId(): Int {
        return sharedMusicPref.getInt("musicPos", -1)
    }

    fun getMusicName(): String? {
        return sharedMusicPref.getString("musicName", null)
    }
}