package com.justme.musicplayer.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.justme.musicplayer.MainActivity
import com.justme.musicplayer.adapters.FolderRecyclerViewAdapter
import com.justme.musicplayer.databinding.FragmentFolderBinding

class FolderFragment(mainActivity: MainActivity) : Fragment() {
    private lateinit var binding: FragmentFolderBinding
    private lateinit var view: View
    private lateinit var folderRecyclerViewAdapter: FolderRecyclerViewAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentFolderBinding.inflate(layoutInflater, container, false)
        view = binding.root
        initRecyclerView()
        return view
    }

    private fun initRecyclerView() {
        folderRecyclerViewAdapter =
            FolderRecyclerViewAdapter(requireContext(), MainActivity.Companion.directoryList)
        binding.fragmentFolderRecyclerview.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL, false
        )
        binding.fragmentFolderRecyclerview.adapter = folderRecyclerViewAdapter
    }


}