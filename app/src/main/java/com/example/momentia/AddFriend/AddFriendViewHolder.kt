package com.example.momentia.AddFriend

import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.momentia.DTO.Friend
import com.example.momentia.R
import com.example.momentia.glide.ImageLoader

class AddFriendViewHolder(
    containerView: View,
    private val imageLoader: ImageLoader,
    private val onFriendRequestSent: (Friend) -> Unit
) : RecyclerView.ViewHolder(containerView) {

    private val profilePicture: ImageView = containerView.findViewById(R.id.profile_picture)
    private val username: TextView = containerView.findViewById(R.id.username)
    private val addButton: ImageButton = containerView.findViewById(R.id.add_button)

    fun bindData(friend: Friend, currentUserFriends: List<String>) {
        // Set profile picture
        if (!friend.avatarUrl.isNullOrEmpty()) {
            imageLoader.loadImage(friend.avatarUrl, profilePicture)
        } else {
            profilePicture.setImageResource(R.drawable.account_circle)
        }

        // Set username
        username.text = friend.username

        // Check if the user is already a friend
        if (currentUserFriends.contains(friend.userId)) {
            addButton.setImageResource(R.drawable.check_add_friend)
            addButton.isEnabled = false
        } else {
            addButton.setImageResource(R.drawable.add_friend_white)
            addButton.isEnabled = true

            addButton.setOnClickListener {
                onFriendRequestSent(friend)
            }
        }
    }
}
