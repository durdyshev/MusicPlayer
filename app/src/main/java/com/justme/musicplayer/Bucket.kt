package com.justme.musicplayer

class Bucket(val folderName: String, val fullFolderName: String, val data: String){
    override fun toString(): String {
        return folderName
    }
}