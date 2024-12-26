package com.example.momentia.Memories

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.momentia.DTO.Memory
import com.example.momentia.R

class MemoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val memoryImageView: ImageView = itemView.findViewById(R.id.memoryImageView)

    fun bind(memory: Memory) {
        Glide.with(itemView.context)
            .load(memory.mediaUrl)
            .into(memoryImageView)
    }
}
