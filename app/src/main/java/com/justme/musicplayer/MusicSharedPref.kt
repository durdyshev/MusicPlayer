package com.justme.musicplayer

import android.content.Context
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import com.justme.musicplayer.MainActivity.Companion.audio
import com.justme.musicplayer.MainActivity.Companion.position

class MusicSharedPref(context: Context) {
    private var sharedMusicPref = context.getSharedPreferences("Music", MODE_PRIVATE)
    private var prefEditor = sharedMusicPref.edit()

    fun saveShared() {
        prefEditor.putInt("musicPos", position.value!!)
        prefEditor.putString("musicName", audio.value!!.name)
        prefEditor.apply()
    }

    fun getMusicId(): Int {
        return sharedMusicPref.getInt("musicPos", -1)
    }

    fun getMusicName(): String? {
        return sharedMusicPref.getString("musicName", null)
    }
}