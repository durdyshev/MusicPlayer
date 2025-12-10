package com.justme.musicplayer.adapters

import android.content.Context
import android.media.MediaMetadataRetriever
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.justme.musicplayer.R
import com.justme.musicplayer.model.Audio
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TrackRecyclerViewAdapter(
    val context: Context,
    private var accountDataArrayList: ArrayList<Audio>,
) : RecyclerView.Adapter<TrackRecyclerViewAdapter.ViewHolder>() {
    private var setOnClickItem: ((Audio, Int) -> Unit)? = null


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.track_recycler_item, parent, false)
        return ViewHolder(v)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindView(accountDataArrayList[position])
        holder.itemView.setOnClickListener {
            setOnClickItem?.invoke(accountDataArrayList[position], position)
        }

    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tv: TextView = itemView.findViewById(R.id.track_recycler_item_text)
        private val authorTv: TextView = itemView.findViewById(R.id.track_recycler_item_author)
        private val imageView: ImageView = itemView.findViewById(R.id.track_recycler_item_imageview)
        val radiusDp = 8
        val radiusPx = (radiusDp * itemView.resources.displayMetrics.density).toInt()
        fun bindView(audio: Audio) {
            if (audio.name.length > 20) {
                tv.text = audio.name.substring(0, 21) + "..."
            } else {
                tv.text = audio.name
            }
            authorTv.text = audio.artistName ?: "Unknown"
            CoroutineScope(Dispatchers.IO).launch {
                val image = imgSource(audio.data)
                withContext(Dispatchers.Main) {
                    val glideRequest = if (image != null) {
                        Glide.with(itemView).asBitmap().load(image)
                    } else {
                        Glide.with(itemView).asBitmap().load(R.drawable.baseline_music_note_24)
                    }

                    glideRequest
                        .placeholder(R.drawable.track_drawable)
                        .transform(
                            com.bumptech.glide.load.resource.bitmap.CenterCrop(),
                            com.bumptech.glide.load.resource.bitmap.RoundedCorners(radiusPx)
                        )
                        .into(imageView)
                }
            }

        }

    }

    override fun getItemCount(): Int {
        return accountDataArrayList.size
    }

    fun setOnClickItem(callback: (Audio, Int) -> Unit) {
        this.setOnClickItem = callback
    }

    private fun imgSource(path: String): ByteArray? {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(path)
        val pic = retriever.embeddedPicture
        retriever.release()
        return pic
    }
}