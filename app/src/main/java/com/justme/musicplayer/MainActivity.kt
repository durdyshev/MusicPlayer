package com.justme.musicplayer

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.ContentUris
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import androidx.lifecycle.MutableLiveData
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayoutMediator
import com.justme.musicplayer.databinding.ActivityMainBinding
import java.io.File
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity(), ActionPlaying {
    private lateinit var serviceIntent: Intent
    private lateinit var adapter: FragmentStateAdapter
    private lateinit var binding: ActivityMainBinding
    private lateinit var view: View
    private val directories: ArrayList<String> = ArrayList()
    private val directories1: ArrayList<Bucket> = ArrayList()
    private lateinit var sharedMusicPref: SharedPreferences
    private lateinit var prefEditor: SharedPreferences.Editor
    private lateinit var musicPlayerService: MusicPlayerService

    private val foldersArray = arrayOf(
        "Tracks",
        "Folder",
    )

    companion object {
        lateinit var directoryList: ArrayList<Bucket>
        var audio = MutableLiveData<Audio>()
        var position = MutableLiveData<Int>()
        var isPlaying = MutableLiveData<Boolean>()
        lateinit var audioList: ArrayList<Audio>
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        view = binding.root
        setContentView(view)
        initVariables()
        initTabLayout()
        initThis()
    }

    private fun initVariables() {
        serviceIntent = Intent(this, MusicPlayerService::class.java)
        sharedMusicPref = getSharedPreferences("Music", MODE_PRIVATE)
        prefEditor = sharedMusicPref.edit()
        audioList = getAllMusic()
        isPlaying.value=true
        audio.observe(this) {
            //Do something with the changed value -> it
            setAudioFileDetails1(audio.value!!)
        }
    }

    private fun initThis() {
        if (!isMyServiceRunning(MusicPlayerService::class.java)) {
            isPlaying.value = false
            binding.playImg.setImageResource(R.drawable.baseline_play_arrow_24)

        } else {
            if (isPlaying.value!!) {
                isPlaying.value = true
                binding.playImg.setImageResource(R.drawable.baseline_pause_24)
            } else {
                isPlaying.value = false
                binding.playImg.setImageResource(R.drawable.baseline_play_arrow_24)
            }

        }
        val musicPos = sharedMusicPref.getInt("musicPos", -1)
        val musicName = sharedMusicPref.getString("musicName", null)
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
        setAudioFileDetails1(audio.value!!)

        binding.prevImg.setOnClickListener {
            prevClicked()
        }
        binding.playImg.setOnClickListener {
            playClicked()
        }
        binding.nextImg.setOnClickListener {
            nextClicked()
        }
        Log.e("observerQwerty", isPlaying.value.toString())
        isPlaying.observe(this) {
            if (isPlaying.value==true) {
                binding.playImg.setImageResource(R.drawable.baseline_pause_24)
            } else {
                binding.playImg.setImageResource(R.drawable.baseline_play_arrow_24)
            }
        }
    }

    @SuppressLint("ResourceAsColor")
    private fun initTabLayout() {
        adapter = TabLayoutAdapter(supportFragmentManager, lifecycle, this)
        binding.pager.adapter = adapter
        binding.tabLayout.setBackgroundColor(android.R.color.transparent)

        TabLayoutMediator(binding.tabLayout, binding.pager) { tab, position ->
            tab.text = foldersArray[position]
        }.attach()
        binding.pager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
            }
        })
    }

    fun playContentUri(uri: Uri) {
        val mMediaPlayer = MediaPlayer().apply {
            setDataSource(application, uri)
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            prepare()
            start()
        }
    }

    fun setAudioFileDetails(audio: Audio) {
        val image = imgSource(audio.data)
        binding.trackRecyclerItemText.text = audio.name
        binding.playImg.setImageResource(R.drawable.baseline_pause_24)

        if (image != null) {

            Glide.with(binding.trackRecyclerItemImageview).asBitmap() //2
                .load(image) //3
                .centerCrop() //4
                .placeholder(R.drawable.track_drawable) //5
                .into(binding.trackRecyclerItemImageview) //8
        } else {
            Glide.with(binding.trackRecyclerItemImageview).asBitmap() //2
                .load(R.drawable.baseline_music_note_24) //3
                .centerCrop() //4
                .placeholder(R.drawable.track_drawable) //5
                .into(binding.trackRecyclerItemImageview) //8
        }
    }


    private fun setAudioFileDetails1(audio: Audio) {
        val image = imgSource(audio.data)
        binding.trackRecyclerItemText.text = audio.name

        if (image != null) {

            Glide.with(binding.trackRecyclerItemImageview).asBitmap() //2
                .load(image) //3
                .centerCrop() //4
                .placeholder(R.drawable.track_drawable) //5
                .into(binding.trackRecyclerItemImageview) //8
        } else {
            Glide.with(binding.trackRecyclerItemImageview).asBitmap() //2
                .load(R.drawable.baseline_music_note_24) //3
                .centerCrop() //4
                .placeholder(R.drawable.track_drawable) //5
                .into(binding.trackRecyclerItemImageview) //8
        }
    }

    private fun imgSource(path: String): ByteArray? {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(path)
        val pic = retriever.embeddedPicture
        retriever.release()
        return pic
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService() as ActivityManager?
        for (service in manager!!.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    fun saveShared() {
        prefEditor.putInt("musicPos", position.value!!)
        prefEditor.putString("musicName", audio.value!!.name)
        prefEditor.apply()
    }

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
            directoryList=directories1
            for (hey in directories1) {
                Log.e("directory", hey.folderName)
                Log.e("directoryFull", hey.fullFolderName)
            }
        }
        return audioList1
    }

    fun getAudioList(): ArrayList<Audio> {
        return audioList
    }

    override fun onResume() {
        val musicPos = sharedMusicPref.getInt("musicPos", -1)
        val musicName = sharedMusicPref.getString("musicName", null)
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
        super.onResume()
    }

    override fun nextClicked() {
        if (isMyServiceRunning(MusicPlayerService::class.java)) {
            if (isPlaying.value!!) {
                isPlaying.value = false
                serviceIntent.action = Constants.ACTION.STOP_MUSIC
                startService(serviceIntent)
                binding.playImg.setImageResource(R.drawable.baseline_play_arrow_24)
            }
            if (position.value == audioList.size - 1) {
                position.value = 0
                audio.value = audioList[position.value!!]
            } else {
                position.value = position.value!! + 1
                audio.value = audioList[position.value!!]
            }
            isPlaying.value = true
            serviceIntent.action = Constants.ACTION.PLAY_MUSIC
            startService(serviceIntent)
            binding.playImg.setImageResource(R.drawable.baseline_pause_24)

        } else {
            isPlaying.value = true
            serviceIntent.action = Constants.ACTION.START_FOREGROUND_ACTION
            startService(serviceIntent)
            binding.playImg.setImageResource(R.drawable.baseline_pause_24)
        }
        setAudioFileDetails(audio.value!!)
        saveShared()
    }

    override fun prevClicked() {
        if (isMyServiceRunning(MusicPlayerService::class.java)) {
            if (isPlaying.value!!) {
                isPlaying.value = false
                serviceIntent.action = Constants.ACTION.STOP_MUSIC
                startService(serviceIntent)
                binding.playImg.setImageResource(R.drawable.baseline_play_arrow_24)
            }
            if (position.value == 0) {
                position.value = audioList.size - 1

                audio.value = audioList[position.value!!]
            } else {
                position.value = position.value!! - 1
                audio.value = audioList[position.value!!]
            }
            isPlaying.value = true
            serviceIntent.action = Constants.ACTION.PLAY_MUSIC
            startService(serviceIntent)
            binding.playImg.setImageResource(R.drawable.baseline_pause_24)

        } else {
            isPlaying.value = true
            serviceIntent.action = Constants.ACTION.START_FOREGROUND_ACTION
            startService(serviceIntent)
            binding.playImg.setImageResource(R.drawable.baseline_pause_24)
        }
        setAudioFileDetails(audio.value!!)
        saveShared()
    }

    override fun playClicked() {
        if (isMyServiceRunning(MusicPlayerService::class.java)) {
            if (isPlaying.value!!) {
                isPlaying.value = false
                serviceIntent.action = Constants.ACTION.PAUSE_MUSIC
                startService(serviceIntent)
                binding.playImg.setImageResource(R.drawable.baseline_play_arrow_24)
            } else {
                isPlaying.value = true
                serviceIntent.action = Constants.ACTION.PLAY_MUSIC
                startService(serviceIntent)
                binding.playImg.setImageResource(R.drawable.baseline_pause_24)
            }
        } else {
            isPlaying.value = true
            val serviceIntent = Intent(this, MusicPlayerService::class.java)
            serviceIntent.action = Constants.ACTION.START_FOREGROUND_ACTION
            startService(serviceIntent)
            binding.playImg.setImageResource(R.drawable.baseline_pause_24)

        }
    }
}