package com.example.momentia.Memories

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.momentia.DTO.Memory
import com.example.momentia.R

class MemoriesAdapter(
    private val memoriesList: List<Memory>,
//    private val onMemoryClick: (Memory) -> Unit
) : RecyclerView.Adapter<MemoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoryViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_memory, parent, false)
        return MemoryViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MemoryViewHolder, position: Int) {
        val memory = memoriesList[position]
//        holder.bind(memory, onMemoryClick)
        holder.bind(memory)
    }
    override fun getItemCount(): Int {
        return memoriesList.size
    }
}