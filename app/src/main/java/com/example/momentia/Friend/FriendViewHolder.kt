package com.example.momentia.Friend

import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.momentia.DTO.FriendChat
import com.example.momentia.R
import com.example.momentia.glide.ImageLoader

class FriendViewHolder(
    private val containerView: View,
    private val imageLoader: ImageLoader,
    private val onClickListener: OnClickListener
) : RecyclerView.ViewHolder(containerView) {
    private val profilePicture: ImageView by lazy {
        containerView.findViewById(R.id.profile_picture)
    }

    private val name: TextView by lazy {
        containerView.findViewById(R.id.name)
    }

    private val deleteButton: ImageButton by lazy {
        containerView.findViewById(R.id.delete_friend_button)
    }

    fun bindData(friend: FriendChat) {
        containerView.setOnClickListener {
            onClickListener.onItemClick(friend)
        }

        deleteButton.setOnClickListener {
            onClickListener.onDeleteClick(position)
        }

        if (!friend.avatarUrl.isNullOrEmpty()) {
            imageLoader.loadImage(friend.avatarUrl, profilePicture)
        } else {
            profilePicture.setImageResource(R.drawable.account_circle)
        }

        name.text = "${friend.firstName} ${friend.lastName ?: ""}"
    }

    interface OnClickListener {
        fun onItemClick(friend: FriendChat)
        fun onDeleteClick(position: Int)
    }
}