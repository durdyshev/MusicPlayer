package com.justme.musicplayer.utils

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.justme.musicplayer.ui.TrackFragment
import com.justme.musicplayer.ui.FolderFragment
import com.justme.musicplayer.ui.MainActivity

const val NUM_TABS = 2

class TabLayoutAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle, private val mainActivity: MainActivity) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return NUM_TABS
    }

    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> return TrackFragment(mainActivity)
            1 -> return FolderFragment(mainActivity)
        }
        return TrackFragment(mainActivity)
    }
}