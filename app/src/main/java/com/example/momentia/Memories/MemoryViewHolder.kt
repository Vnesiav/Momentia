package com.example.momentia.Memories

import android.content.Intent
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

        memoryImageView.setOnClickListener {
            val context = itemView.context
            val intent = Intent(context, PhotoActivity::class.java)
            intent.putExtra("IMAGE_URL", memory.mediaUrl)
            context.startActivity(intent)
        }
    }
}
