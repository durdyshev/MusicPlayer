package com.justme.musicplayer

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayoutMediator
import com.justme.musicplayer.MusicPlayerService.Companion.mediaPlayer
import com.justme.musicplayer.databinding.ActivityMainBinding
import com.justme.musicplayer.use_cases.DecreaseAudioPosition.decreaseAudioPosition
import com.justme.musicplayer.use_cases.IncreaseAudioPosition.increaseAudioPosition


class MainActivity : AppCompatActivity() {
    private lateinit var serviceIntent: Intent
    private lateinit var adapter: FragmentStateAdapter
    private lateinit var binding: ActivityMainBinding
    lateinit var mainViewModel: MainViewModel
    private var handler = Handler()

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
        var initSeekValue = MutableLiveData<Boolean>()
        var buttonClick = MutableLiveData<Int>()

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
        mainViewModel.checkSharedPrefAndSetMusicValue()

        if (!MusicPlayerService.isServiceRunning) {
            isPlaying.value = false
            serviceIntent.action = Constants.ACTION.INIT_MUSIC
            startService(serviceIntent)
        } else {
            isPlaying.value = MusicPlayerService.isMusicPlaying()
        }

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
        initSeekValue.observe(this) {
            initSeekListener()
        }
        buttonClick.observe(this) {
            when (it) {
                1 -> {
                    prevClicked()
                }

                2 -> {
                    playClicked()
                }

                3 -> {
                    nextClicked()
                }
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


    private fun setAudioFileDetails(audio: Audio) {
        val image = mainViewModel.imgSource(audio.data)
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


    fun getAudioList(): ArrayList<Audio> {
        return audioList
    }

    override fun onResume() {
        mainViewModel.checkSharedPrefAndSetMusicValue()
        super.onResume()
    }

    private fun nextClicked() {
        increaseAudioPosition()
        prevOrNextClick()
        updateSeekBar()
    }

    private fun prevClicked() {
        decreaseAudioPosition()
        prevOrNextClick()
        updateSeekBar()
    }

    private fun playClicked() {
        if (isPlaying.value == true) {
            isPlaying.value = false
            serviceIntent.action = Constants.ACTION.PAUSE_MUSIC
            startService(serviceIntent)
        } else {
            isPlaying.value = true
            serviceIntent.action = Constants.ACTION.PLAY_MUSIC
            startService(serviceIntent)
            updateSeekBar()
        }
    }

    fun prevOrNextClick() {
        isPlaying.value = true
        serviceIntent.action = Constants.ACTION.CHANGE_MUSIC
        startService(serviceIntent)
        mainViewModel.saveShared()
    }

    private fun initSeekListener() {
        binding.seekbar.max = mediaPlayer.duration
        binding.endTimeText.text = mainViewModel.formatTime(mediaPlayer.duration)

        binding.seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                mediaPlayer.pause()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                mediaPlayer.start()
            }
        })
    }

    private fun updateSeekBar() {
        binding.seekbar.max = mediaPlayer.duration
        binding.seekbar.setProgress(mediaPlayer.currentPosition, true)
        //if (mediaPlayer.isPlaying) {
            handler.postDelayed({
                updateSeekBar()
                binding.currentTimeText.text = mainViewModel.formatTime(mediaPlayer.currentPosition)
                binding.endTimeText.text = mainViewModel.formatTime(mediaPlayer.duration)
            }, 1000) // Update every second
       // }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

}