package com.example.momentia.Memories

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.momentia.DTO.Memory
import com.example.momentia.R
import java.text.SimpleDateFormat
import java.util.*

class MemoriesAdapter(
    private val memories: List<Memory>,
    private val onSendToFriendClick: (Memory) -> Unit
) : RecyclerView.Adapter<MemoriesAdapter.MemoryViewHolder>() {

    class MemoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val imageView: ImageView = view.findViewById(R.id.memoryImageView)
        private val dateTextView: TextView = view.findViewById(R.id.memoryDateTextView)

        fun bind(memory: Memory, onSendToFriendClick: (Memory) -> Unit) {
            Glide.with(imageView.context)
                .load(memory.mediaUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(imageView)

            val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
            dateTextView.text = dateFormat.toString()

            itemView.setOnClickListener {
                onSendToFriendClick(memory)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_memory, parent, false)
        return MemoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemoryViewHolder, position: Int) {
        val memory = memories[position]
        holder.bind(memory, onSendToFriendClick)
    }

    override fun getItemCount(): Int = memories.size
}
