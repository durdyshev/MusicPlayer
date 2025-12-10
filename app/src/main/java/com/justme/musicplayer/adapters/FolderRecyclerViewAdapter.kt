package com.justme.musicplayer.adapters

import android.annotation.SuppressLint
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
import com.justme.musicplayer.model.Bucket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FolderRecyclerViewAdapter(
    val context: Context,
    private var folderArrayList: ArrayList<Bucket>,
) : RecyclerView.Adapter<FolderRecyclerViewAdapter.ViewHolder>() {
    private var setOnClickItem: ((Bucket, Int) -> Unit)? = null


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.track_recycler_item, parent, false)
        return ViewHolder(v)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindView(folderArrayList[position])
        holder.itemView.setOnClickListener {
            setOnClickItem?.invoke(folderArrayList[position], position)
        }

    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tv: TextView = itemView.findViewById(R.id.track_recycler_item_text)
        private val tv2: TextView = itemView.findViewById(R.id.track_recycler_item_author)
        private val imageView: ImageView = itemView.findViewById(R.id.track_recycler_item_imageview)

        fun bindView(folder: Bucket) {
            if (folder.folderName.length > 20) {
                tv.text = folder.folderName.substring(0, 21) + "..."
            } else {
                tv.text = folder.folderName
            }
            if (folder.fullFolderName.length > 20) {
                tv2.text = folder.fullFolderName.substring(0, 21) + "..."
            } else {
                tv2.text = folder.fullFolderName
            }

            CoroutineScope(Dispatchers.IO).launch {
                val image =  imgSource(folder.data)
                withContext(Dispatchers.Main) {
                    if (image != null) {
                        Glide.with(itemView).asBitmap() //2
                            .load(image) //3
                            .centerCrop() //4
                            .placeholder(R.drawable.track_drawable) //5
                            .into(imageView) //8
                    } else {
                        Glide.with(itemView).asBitmap() //2
                            .load(R.drawable.baseline_music_note_24) //3
                            .centerCrop() //4
                            .placeholder(R.drawable.track_drawable) //5
                            .into(imageView) //8
                    }
                }
            }

        }

    }

    override fun getItemCount(): Int {
        return folderArrayList.size
    }

    fun setOnClickItem(callback: (Bucket, Int) -> Unit) {
        this.setOnClickItem = callback
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(tempStaffList: ArrayList<Bucket>) {
        this.folderArrayList = tempStaffList
        notifyDataSetChanged()
    }

    private fun imgSource(path: String): ByteArray? {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(path)
        val pic = retriever.embeddedPicture
        retriever.release()
        return pic
    }
}