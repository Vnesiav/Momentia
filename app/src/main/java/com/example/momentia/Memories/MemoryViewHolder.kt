package com.example.momentia.Memories

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.momentia.DTO.Memory
import com.bumptech.glide.Glide
import com.example.momentia.R

class MemoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val memoryImageView: ImageView = itemView.findViewById(R.id.memoryImageView)
    val memoryDateTextView: TextView = itemView.findViewById(R.id.memoryDate)

    //    fun bind(memory: Memory, onMemoryClick: (Memory) -> Unit) {
    fun bind(memory: Memory) {
        Glide.with(itemView.context)
            .load(memory.mediaUrl)
            .into(memoryImageView)

        memoryDateTextView.text = memory.formattedDate ?: "Unknown Date"

//        itemView.setOnClickListener {
//            onMemoryClick(memory)
//        }
    }
}
