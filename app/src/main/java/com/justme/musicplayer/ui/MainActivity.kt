package com.justme.musicplayer.ui

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayoutMediator
import com.justme.musicplayer.R
import com.justme.musicplayer.databinding.ActivityMainBinding
import com.justme.musicplayer.model.Audio
import com.justme.musicplayer.model.Bucket
import com.justme.musicplayer.service.MusicPlayerService
import com.justme.musicplayer.utils.ActionPlaying
import com.justme.musicplayer.utils.Constants
import com.justme.musicplayer.utils.TabLayoutAdapter
import com.justme.musicplayer.viewmodel.MainViewModel
import kotlinx.coroutines.runBlocking


class MainActivity : AppCompatActivity(), ActionPlaying {
    private lateinit var serviceIntent: Intent
    private lateinit var adapter: FragmentStateAdapter
    private lateinit var binding: ActivityMainBinding
    private val directories: ArrayList<String> = ArrayList()
    private val directories1: ArrayList<Bucket> = ArrayList()
    private lateinit var sharedMusicPref: SharedPreferences
    private lateinit var prefEditor: SharedPreferences.Editor
    private lateinit var musicPlayerService: MusicPlayerService
    private lateinit var mainViewModel: MainViewModel
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
        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        setContentView(binding.root)
        initVariables()
        initTabLayout()
        initThis()
    }

    private fun initVariables() {
        serviceIntent = Intent(this, MusicPlayerService::class.java)
        sharedMusicPref = getSharedPreferences("Music", MODE_PRIVATE)
        prefEditor = sharedMusicPref.edit()
        runBlocking { audioList = mainViewModel.getAllMusicAsync() }

        isPlaying.value = true
        audio.observe(this) {
            //Do something with the changed value -> it
            setAudioFileDetails1(audio.value!!)
        }
    }

    private fun initThis() {
        if (!mainViewModel.isMyServiceRunning(MusicPlayerService::class.java)) {
            isPlaying.value = false
        } else {
            isPlaying.value = isPlaying.value!!
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
        isPlaying.observe(this) {
            if (isPlaying.value == true) {
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


    fun setAudioFileDetails(audio: Audio) {
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



    fun saveShared() {
        prefEditor.putInt("musicPos", position.value!!)
        prefEditor.putString("musicName", audio.value!!.name)
        prefEditor.apply()
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
        if (!mainViewModel.isMyServiceRunning(MusicPlayerService::class.java)) {
            if (isPlaying.value!!) {
                isPlaying.value = false
                serviceIntent.action = Constants.ACTION.STOP_MUSIC
                startService(serviceIntent)
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

        } else {
            isPlaying.value = true
            serviceIntent.action = Constants.ACTION.START_FOREGROUND_ACTION
            startService(serviceIntent)
        }
        setAudioFileDetails(audio.value!!)
        saveShared()
    }

    override fun prevClicked() {
        if (!mainViewModel.isMyServiceRunning(MusicPlayerService::class.java)) {
            if (isPlaying.value!!) {
                isPlaying.value = false
                serviceIntent.action = Constants.ACTION.STOP_MUSIC
                startService(serviceIntent)
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

        } else {
            isPlaying.value = true
            serviceIntent.action = Constants.ACTION.START_FOREGROUND_ACTION
            startService(serviceIntent)
        }
        setAudioFileDetails(audio.value!!)
        saveShared()
    }

    override fun playClicked() {
        if (!mainViewModel.isMyServiceRunning(MusicPlayerService::class.java)) {
            if (isPlaying.value!!) {
                isPlaying.value = false
                serviceIntent.action = Constants.ACTION.PAUSE_MUSIC
                startService(serviceIntent)
            } else {
                isPlaying.value = true
                serviceIntent.action = Constants.ACTION.PLAY_MUSIC
                startService(serviceIntent)
            }
        } else {
            isPlaying.value = true
            val serviceIntent = Intent(this, MusicPlayerService::class.java)
            serviceIntent.action = Constants.ACTION.START_FOREGROUND_ACTION
            startService(serviceIntent)


        }
    }
}