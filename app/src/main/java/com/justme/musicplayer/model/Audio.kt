package com.justme.musicplayer.model

import android.net.Uri

data class Audio(
    val uri: Uri,
    val name: String,
    val duration: Int,
    val size: Int,
    val data: String,
)