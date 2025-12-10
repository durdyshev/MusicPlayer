package com.justme.musicplayer.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.justme.musicplayer.MainActivity
import com.justme.musicplayer.adapters.TrackRecyclerViewAdapter
import com.justme.musicplayer.databinding.FragmentTrackBinding
import com.justme.musicplayer.viewmodel.TrackViewModel

class TrackFragment(private val mainActivity: MainActivity) : Fragment() {
    private lateinit var binding: FragmentTrackBinding
    private lateinit var view: View
    private lateinit var trackRecyclerViewAdapter: TrackRecyclerViewAdapter
    private lateinit var trackViewModel: TrackViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTrackBinding.inflate(layoutInflater, container, false)
        trackViewModel = ViewModelProvider(this)[TrackViewModel::class.java]
        view = binding.root
        initRecyclerView()
        return view
    }

    private fun initRecyclerView() {
        trackRecyclerViewAdapter =
            TrackRecyclerViewAdapter(requireContext(), trackViewModel.getAllMusic())
        binding.trackRecyclerview.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        val layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.trackRecyclerview.layoutManager = layoutManager
        binding.trackRecyclerview.adapter = trackRecyclerViewAdapter

        trackRecyclerViewAdapter.setOnClickItem { it, pos ->
            MainActivity.Companion.audio.value = it
            MainActivity.Companion.position.value = pos
            mainActivity.prevOrNextClick()
        }
    }
}