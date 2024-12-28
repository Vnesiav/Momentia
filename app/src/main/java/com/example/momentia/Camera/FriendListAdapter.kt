package com.example.momentia.Camera

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.momentia.DTO.FriendChat
import com.example.momentia.R
import com.example.momentia.glide.GlideImageLoader
import com.google.android.material.imageview.ShapeableImageView

class FriendListAdapter(
    private val inflater: LayoutInflater,
    private val imageLoader: GlideImageLoader,
    private val onClickListener: OnClickListener
) : RecyclerView.Adapter<FriendListAdapter.FriendViewHolder>() {

    private val friends = mutableListOf<FriendChat>()
    private val tickStatus = mutableMapOf<Int, Boolean>() // Menyimpan status untuk setiap item

    interface OnClickListener {
        fun onItemClick(friend: FriendChat)
    }

    // Set new data to the adapter
    fun setData(newFriends: List<FriendChat>) {
        friends.clear()
        friends.addAll(newFriends)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = inflater.inflate(R.layout.friend_item_photo, parent, false)
        return FriendViewHolder(view)
    }

    override fun getItemCount(): Int = friends.size

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        holder.bindData(friends[position], position)
    }

    // Inner class FriendViewHolder
    inner class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val buttonTick: ShapeableImageView = itemView.findViewById(R.id.button_tick)
        private val profilePicture: ShapeableImageView = itemView.findViewById(R.id.profile_picture)
        private val nameTextView: TextView = itemView.findViewById(R.id.name)

        init {
            // Handle click events for each item
            itemView.setOnClickListener {
                val friend = friends[adapterPosition]
                // Toggle the tick status when clicked
                val isTicked = tickStatus[adapterPosition] ?: false
                tickStatus[adapterPosition] = !isTicked
                // Update the icon based on the tick status
                buttonTick.setImageResource(if (isTicked) R.drawable.send_unticked else R.drawable.send_ticked)
                onClickListener.onItemClick(friend)
            }
        }

        // Bind the data to the view
        fun bindData(friend: FriendChat, position: Int) {
            val fullName = "${friend.firstName} ${friend.lastName ?: ""}".trim()
            nameTextView.text = fullName
            friend.avatarUrl?.let { imageLoader.loadImage(it, profilePicture) }

            // Set the button tick icon based on the saved tick status
            val isTicked = tickStatus[position] ?: false
            buttonTick.setImageResource(if (isTicked) R.drawable.send_ticked else R.drawable.send_unticked)
        }
    }
}