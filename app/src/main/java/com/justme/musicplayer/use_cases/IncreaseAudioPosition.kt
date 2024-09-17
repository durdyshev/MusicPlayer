package com.justme.musicplayer.use_cases

import com.justme.musicplayer.MainActivity.Companion.audio
import com.justme.musicplayer.MainActivity.Companion.audioList
import com.justme.musicplayer.MainActivity.Companion.position

object IncreaseAudioPosition {
    fun increaseAudioPosition() {
        if (audioList.isEmpty()) return
        if (position.value == audioList.size - 1) {
            position.value = 0
            audio.value = audioList[position.value!!]
        } else {
            position.value = position.value!! + 1
            audio.value = audioList[position.value!!]
        }
    }
}