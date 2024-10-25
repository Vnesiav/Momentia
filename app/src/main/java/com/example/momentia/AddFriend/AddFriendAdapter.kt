package com.example.momentia.AddFriend

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.momentia.DTO.Friend
import com.example.momentia.R
import com.example.momentia.glide.ImageLoader

class AddFriendAdapter(
    private val layoutInflater: LayoutInflater,
    private val imageLoader: ImageLoader,
    private val onFriendRequestSent: (Friend) -> Unit,
    private var currentUserFriends: List<String> = emptyList()
) : RecyclerView.Adapter<AddFriendViewHolder>() {

    private val friends = mutableListOf<Friend>()

    fun setData(newFriends: List<Friend>, friendsList: List<String>?) {
        friends.clear()
        friends.addAll(newFriends)
        if (friendsList != null) {
            currentUserFriends = friendsList
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddFriendViewHolder {
        val view = layoutInflater.inflate(R.layout.add_friend_list, parent, false)
        return AddFriendViewHolder(view, imageLoader, onFriendRequestSent)
    }

    override fun onBindViewHolder(holder: AddFriendViewHolder, position: Int) {
        val friend = friends[position]
        holder.bindData(friend, currentUserFriends)
    }

    override fun getItemCount(): Int {
        return friends.size
    }
}