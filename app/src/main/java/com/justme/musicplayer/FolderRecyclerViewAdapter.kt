package com.justme.musicplayer

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
        private val tv2: TextView = itemView.findViewById(R.id.track_recycler_item_text2)
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

            val image = imgSource(folder.data)
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