package com.example.momentia.Memories

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.momentia.DTO.MemorySection
import com.example.momentia.R

class MemoriesAdapter(private val sections: List<MemorySection>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_HEADER = 0
        const val VIEW_TYPE_ITEM = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (sections[position]) {
            is MemorySection.Header -> VIEW_TYPE_HEADER
            is MemorySection.Item -> VIEW_TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val view = inflater.inflate(R.layout.item_header, parent, false)
                HeaderViewHolder(view)
            }
            VIEW_TYPE_ITEM -> {
                val view = inflater.inflate(R.layout.item_memory, parent, false)
                MemoryViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val section = sections[position]) {
            is MemorySection.Header -> {
                (holder as HeaderViewHolder).bind(section.memory.formattedDate ?: "Unknown Date")
            }
            is MemorySection.Item -> (holder as MemoryViewHolder).bind(section.memory)
        }
    }

    override fun getItemCount(): Int = sections.size
}
