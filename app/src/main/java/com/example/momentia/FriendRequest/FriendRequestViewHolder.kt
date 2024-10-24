package com.example.momentia.AddFriend

import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.momentia.DTO.FriendRequest
import com.example.momentia.R
import com.example.momentia.glide.ImageLoader

class FriendRequestViewHolder(containerView: View, private val imageLoader: ImageLoader) : RecyclerView.ViewHolder(containerView) {
    private val profilePicture: ImageView = containerView.findViewById(R.id.profile_picture)
    private val username: TextView = containerView.findViewById(R.id.username)
    private val name: TextView = containerView.findViewById(R.id.name)
    private val acceptButton: Button = containerView.findViewById(R.id.accept_button)
    private val declineButton: ImageButton = containerView.findViewById(R.id.decline_button)

    fun bindData(friendRequest: FriendRequest,
                 imageLoader: ImageLoader,
//                 onAcceptClick: () -> Unit,
//                 onDeclineClick: () -> Unit\
    ) {
        if (!friendRequest.avatarUrl.isNullOrEmpty()) {
            imageLoader.loadImage(friendRequest.avatarUrl, profilePicture)
        } else {
            profilePicture.setImageResource(R.drawable.account_circle)
        }

        username.text = friendRequest.username
        name.text = friendRequest.firstName


//        acceptButton.setOnClickListener { onAcceptClick() }
//        declineButton.setOnClickListener { onDeclineClick() }
    }
}
