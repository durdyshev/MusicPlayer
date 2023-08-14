package com.justme.musicplayer

import android.app.ActivityManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.justme.musicplayer.databinding.FragmentTrackBinding


class TrackFragment(private val mainActivity: MainActivity) : Fragment() {
    private lateinit var binding: FragmentTrackBinding
    private lateinit var view: View

    private lateinit var trackRecyclerViewAdapter: TrackRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTrackBinding.inflate(layoutInflater, container, false)
        view = binding.root
        initRecyclerView()
        return view
    }

    private fun initRecyclerView() {
        trackRecyclerViewAdapter =
            TrackRecyclerViewAdapter(requireContext(), mainActivity.getAudioList())
        binding.trackRecyclerview.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL, false
        )
        val layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.trackRecyclerview.layoutManager=layoutManager
        binding.trackRecyclerview.adapter = trackRecyclerViewAdapter

        Log.e("recyclerview",trackRecyclerViewAdapter.itemCount.toString())
        trackRecyclerViewAdapter.setOnClickItem { it, pos ->
            MainActivity.audio.value = it
            MainActivity.position.value = pos
            if (isMyServiceRunning(MusicPlayerService::class.java)) {
                val serviceIntent = Intent(requireContext(), MusicPlayerService::class.java)
                serviceIntent.action = Constants.ACTION.STOP_MUSIC
                requireContext().startService(serviceIntent)
                Log.e("service", "running")
                serviceIntent.action = Constants.ACTION.PLAY_MUSIC
                requireContext().startService(serviceIntent)
                mainActivity.setAudioFileDetails(it)

            } else {
                Log.e("service", "notRunning")
                val serviceIntent = Intent(requireContext(), MusicPlayerService::class.java)
                serviceIntent.action = Constants.ACTION.START_FOREGROUND_ACTION
                requireContext().startService(serviceIntent)
                mainActivity.setAudioFileDetails(it)
            }
            mainActivity.saveShared()
        }
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = requireContext().getSystemService() as ActivityManager?
        for (service in manager!!.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }


}