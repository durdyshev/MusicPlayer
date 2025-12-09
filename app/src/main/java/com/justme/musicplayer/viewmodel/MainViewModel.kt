package com.justme.musicplayer.viewmodel

import android.app.Application
import android.content.ContentUris
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.justme.musicplayer.model.Audio
import com.justme.musicplayer.model.Bucket
import com.justme.musicplayer.MainActivity.Companion.audio
import com.justme.musicplayer.MainActivity.Companion.audioList
import com.justme.musicplayer.MainActivity.Companion.directoryList
import com.justme.musicplayer.MainActivity.Companion.position
import com.justme.musicplayer.utils.MusicSharedPref
import java.io.File
import java.util.concurrent.TimeUnit

class MainViewModel(private val application: Application) : AndroidViewModel(application) {
    private val directories: ArrayList<String> = ArrayList()
    private val directories1: ArrayList<Bucket> = ArrayList()
    private val musicSharedPref = MusicSharedPref(application.applicationContext)

    fun getAllMusic(): ArrayList<Audio> {

        val audioList1 = ArrayList<Audio>()

        val collection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL
                )
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATA,
        )

        // Show only videos that are at least 5 minutes in duration.
        val selection = "${MediaStore.Audio.Media.DURATION} >= ?"
        val selectionArgs = arrayOf(
            TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES).toString()
        )

        // Display videos in alphabetical order based on their display name.
        val sortOrder = "${MediaStore.Audio.Media.DISPLAY_NAME} ASC"

        val query = application.contentResolver.query(
            collection,
            projection,
            null,
            null,
            sortOrder
        )
        query?.use { cursor ->
            // Cache column indices.
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val nameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val durationColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val data = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

            while (cursor.moveToNext()) {
                // Get values of columns for a given video.
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val duration = cursor.getInt(durationColumn)
                val size = cursor.getInt(sizeColumn)
                val data = cursor.getString(data)

                val contentUri: Uri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                )


                // Stores column values and the contentUri in a local object
                // that represents the media file.
                audioList1 += Audio(contentUri, name, duration, size, data)

                val photoUri = data
                val photoFolderPath = File(photoUri).parent!!
                if (!directories.contains(photoFolderPath)) {
                    val folderName: MutableList<String> =
                        File(photoUri).parent!!.split("/").toMutableList()

                    val bucket = Bucket(folderName.last(), photoFolderPath, data)
                    directories.add(File(photoUri).parent!!)
                    directories1.add(bucket)
                }


            }
            directoryList = directories1
            for (hey in directories1) {
                Log.e("directory", hey.folderName)
                Log.e("directoryFull", hey.fullFolderName)
            }
        }
        return audioList1
    }

    fun imgSource(path: String): ByteArray? {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(path)
        val pic = retriever.embeddedPicture
        retriever.release()
        return pic
    }


    fun checkSharedPrefAndSetMusicValue() {
        val musicPos = musicSharedPref.getMusicId()
        val musicName = musicSharedPref.getMusicName()
        if (musicPos == -1) {
            position.value = 0
            audio.value = audioList[0]
        } else {
            if (audioList[musicPos].name == musicName) {
                position.value = musicPos
                audio.value = audioList[position.value!!]
            } else {
                position.value = 0
                audio.value = audioList[0]
            }
        }
    }

    fun saveShared() {
        musicSharedPref.saveShared()
    }

    fun formatTime(milliseconds: Int): String {
        val minutes = milliseconds / 1000 / 60
        val seconds = milliseconds / 1000 % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

}