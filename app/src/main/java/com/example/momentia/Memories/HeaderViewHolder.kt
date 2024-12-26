package com.example.momentia.Memories

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.momentia.R

class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val headerTextView: TextView = itemView.findViewById(R.id.headerTextView)

    fun bind(date: String) {
        headerTextView.text = date
    }

}
