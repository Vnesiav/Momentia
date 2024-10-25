package com.example.momentia.AddFriend

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.momentia.DTO.FriendRequest
import com.example.momentia.R
import com.example.momentia.glide.ImageLoader

class FriendRequestAdapter(
    private val layoutInflater: LayoutInflater,
    private val imageLoader: ImageLoader,
    private val onAcceptClick: (FriendRequest) -> Unit,
    private val onDeclineClick: (FriendRequest) -> Unit
) : RecyclerView.Adapter<FriendRequestAdapter.FriendRequestViewHolder>() {

    private val friendRequests = mutableListOf<FriendRequest>()

    // Update the data in the adapter
    fun setData(newFriendRequests: List<FriendRequest>) {
        friendRequests.clear()
        friendRequests.addAll(newFriendRequests)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendRequestViewHolder {
        val view = layoutInflater.inflate(R.layout.friend_request_list, parent, false)
        return FriendRequestViewHolder(view, imageLoader)
    }

    override fun onBindViewHolder(holder: FriendRequestViewHolder, position: Int) {
        val friendRequest = friendRequests[position]
        holder.bindData(friendRequest, onAcceptClick, onDeclineClick)
    }

    override fun getItemCount(): Int = friendRequests.size

    class FriendRequestViewHolder(itemView: View, private val imageLoader: ImageLoader) :
        RecyclerView.ViewHolder(itemView) {
        private val profilePicture: ImageView = itemView.findViewById(R.id.profile_picture)
        private val username: TextView = itemView.findViewById(R.id.username)
        private val name: TextView = itemView.findViewById(R.id.name)
        private val acceptButton: Button = itemView.findViewById(R.id.accept_button)
        private val declineButton: ImageButton = itemView.findViewById(R.id.decline_button)

        fun bindData(
            friendRequest: FriendRequest,
            onAcceptClick: (FriendRequest) -> Unit,
            onDeclineClick: (FriendRequest) -> Unit
        ) {
            // Adjusted check for avatarUrl
            val isAvatarUrlValid = !friendRequest.avatarUrl.isNullOrEmpty() && friendRequest.avatarUrl != "null"

            if (isAvatarUrlValid && friendRequest.avatarUrl != null) {
                imageLoader.loadImage(friendRequest.avatarUrl, profilePicture)
            } else {
                profilePicture.setImageResource(R.drawable.account_circle)
            }

            username.text = friendRequest.username
            name.text = friendRequest.firstName

            // Handle accept and decline button clicks
            acceptButton.setOnClickListener { onAcceptClick(friendRequest) }
            declineButton.setOnClickListener { onDeclineClick(friendRequest) }
        }
    }
}
