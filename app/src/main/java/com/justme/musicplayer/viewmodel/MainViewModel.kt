package com.justme.musicplayer.viewmodel

import android.app.ActivityManager
import android.app.Application
import android.content.ContentUris
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.getSystemService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.justme.musicplayer.model.Audio
import com.justme.musicplayer.model.Bucket
import com.justme.musicplayer.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.io.File

class MainViewModel(private val application: Application) : AndroidViewModel(application) {
    private val directories: ArrayList<String> = ArrayList()
    private val directories1: ArrayList<Bucket> = ArrayList()
    private val audioList = MutableLiveData<ArrayList<Audio>?>()
    val reachAudioList: MutableLiveData<ArrayList<Audio>?>
        get() = audioList


    suspend fun getAllMusicAsync(): ArrayList<Audio> = coroutineScope {
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

        val sortOrder = "${MediaStore.Audio.Media.DISPLAY_NAME} ASC"

        // Use async to perform the query in the background
        val deferredQueryResult = async(Dispatchers.IO) {
            application.contentResolver.query(
                collection,
                projection,
                null,
                null,
                sortOrder
            )
        }

        deferredQueryResult.await()?.use { cursor ->
            // Cache column indices.
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

            while (cursor.moveToNext()) {
                // Get values of columns for a given audio.
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val duration = cursor.getInt(durationColumn)
                val size = cursor.getInt(sizeColumn)
                val data = cursor.getString(dataColumn)

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
            MainActivity.directoryList = directories1

            for (hey in directories1) {
                Log.e("directory", hey.folderName)
                Log.e("directoryFull", hey.fullFolderName)
            }
        }

        return@coroutineScope audioList1
    }
    fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = application.getSystemService() as ActivityManager?
        for (service in manager!!.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    /*
        private fun getAllMusic(): ArrayList<Audio> {

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

        val query = contentResolver.query(
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

                    val bucket = Bucket(folderName.last(), photoFolderPath,data)
                    directories.add(File(photoUri).parent!!)
                    directories1.add(bucket)
                }


            }
            directoryList =directories1
            for (hey in directories1) {
                Log.e("directory", hey.folderName)
                Log.e("directoryFull", hey.fullFolderName)
            }
        }
        return audioList1
    }*/

}