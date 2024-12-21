package com.example.momentia.Memories

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.momentia.DTO.Memory
import com.bumptech.glide.Glide
import com.example.momentia.R

class MemoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val memoryImageView: ImageView = itemView.findViewById(R.id.memoryImageView)

    fun bind(memory: Memory, onMemoryClick: (Memory) -> Unit) {
        // Use Glide to load image
        Glide.with(itemView.context)
            .load(memory.mediaUrl)
            .into(memoryImageView)

        itemView.setOnClickListener {
            onMemoryClick(memory)
        }
    }
}
