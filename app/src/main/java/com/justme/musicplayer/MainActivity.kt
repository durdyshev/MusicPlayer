package com.justme.musicplayer

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayoutMediator
import com.justme.musicplayer.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var serviceIntent: Intent
    private lateinit var adapter: FragmentStateAdapter
    private lateinit var binding: ActivityMainBinding
    lateinit var mainViewModel: MainViewModel

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
        setContentView(binding.root)
        initVariables()
        initTabLayout()
        initThis()
        initClickListeners()
    }

    private fun initVariables() {
        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        serviceIntent = Intent(this, MusicPlayerService::class.java)
        audioList = mainViewModel.getAllMusic()
    }

    private fun initThis() {
        if (!MusicPlayerService.isServiceRunning) {
            isPlaying.value = false
        } else {
            isPlaying.value = MusicPlayerService.isMusicPlaying()
        }
        mainViewModel.checkSharedPrefAndSetMusicValue()

        audio.observe(this) {
            setAudioFileDetails(audio.value!!)
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
        binding.pager.registerOnPageChangeCallback(object : OnPageChangeCallback() {})
    }

    private fun initClickListeners() {
        binding.prevImg.setOnClickListener {
            prevClicked()
        }
        binding.playImg.setOnClickListener {
            playClicked()
        }
        binding.nextImg.setOnClickListener {
            nextClicked()
        }
    }


    private fun setAudioFileDetails(audio: Audio, pauseIcon: Boolean? = false) {
        val image = mainViewModel.imgSource(audio.data)
        binding.trackRecyclerItemText.text = audio.name
        if (pauseIcon == true) {
            binding.playImg.setImageResource(R.drawable.baseline_pause_24)
        }

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


    fun getAudioList(): ArrayList<Audio> {
        return audioList
    }

    override fun onResume() {
        mainViewModel.checkSharedPrefAndSetMusicValue()
        super.onResume()
    }

    private fun nextClicked() {
        mainViewModel.increaseAudioPosition()
        prevOrNextClick()
    }

    private fun prevClicked() {
        mainViewModel.decreaseAudioPosition()
        prevOrNextClick()
    }

    private fun playClicked() {
        if (MusicPlayerService.isServiceRunning) {
            if (isPlaying.value==true) {
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
            serviceIntent.action = Constants.ACTION.START_FOREGROUND_ACTION
            startService(serviceIntent)

        }
    }

    fun prevOrNextClick() {
        if (MusicPlayerService.isServiceRunning) {
            if (isPlaying.value == true) {
                isPlaying.value = false
                serviceIntent.action = Constants.ACTION.STOP_MUSIC
                startService(serviceIntent)
            }
            isPlaying.value = true
            serviceIntent.action = Constants.ACTION.PLAY_MUSIC
            startService(serviceIntent)
        } else {
            isPlaying.value = true
            serviceIntent.action = Constants.ACTION.START_FOREGROUND_ACTION
            startService(serviceIntent)
        }
        mainViewModel.saveShared()
    }
}