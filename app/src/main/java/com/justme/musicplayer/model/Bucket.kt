package com.justme.musicplayer.model

class Bucket(val folderName: String, val fullFolderName: String, val data: String){
    override fun toString(): String {
        return folderName
    }
}