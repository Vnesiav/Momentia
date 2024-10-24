package com.example.momentia.Profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.momentia.DTO.ProfileItem
import com.example.momentia.R

class ProfileAdapter(
    private val items: List<ProfileItem>
) : RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder>() {

    inner class ProfileViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.item_title)
        val iconImageView: ImageView = view.findViewById(R.id.item_icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.profile_item_layout, parent, false)
        return ProfileViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        val item = items[position]
        holder.titleTextView.text = item.title
        holder.iconImageView.setImageResource(item.iconResId)
        holder.itemView.setOnClickListener { item.action() }
    }

    override fun getItemCount(): Int = items.size
}
