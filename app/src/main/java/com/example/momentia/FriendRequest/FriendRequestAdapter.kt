package com.example.momentia.AddFriend

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.momentia.DTO.FriendRequest
import com.example.momentia.R
import com.example.momentia.glide.ImageLoader

class FriendRequestAdapter(
//    private var friendRequests: List<FriendRequest>,
    private val layoutInflater: LayoutInflater,
    private val imageLoader: ImageLoader,
//    private val onAcceptClick: (FriendRequest) -> Unit,
//    private val onDeclineClick: (FriendRequest) -> Unit
) : RecyclerView.Adapter<FriendRequestViewHolder>() {
    private val friendRequests = mutableListOf<FriendRequest>()

    fun setData(newFriendRequests: List<FriendRequest>) {
        friendRequests.clear()
        friendRequests.addAll(newFriendRequests)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendRequestViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.friend_request_list, parent, false)
        return FriendRequestViewHolder(view, imageLoader)
    }

    override fun onBindViewHolder(holder: FriendRequestViewHolder, position: Int) {
        val friendRequest = friendRequests[position]
        holder.bindData(friendRequest, imageLoader,
//            {
//            onAcceptClick(friendRequest) // Callback for accept action
//        }, {
//            onDeclineClick(friendRequest) // Callback for decline action
//        }
        )
    }

    override fun getItemCount(): Int = friendRequests.size
}
