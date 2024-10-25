package com.example.momentia.Friend

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.momentia.DTO.FriendChat
import com.example.momentia.R
import com.example.momentia.glide.ImageLoader

class FriendAdapter(private val layoutInflater: LayoutInflater, private val imageLoader: ImageLoader, private val onClickListener: OnClickListener) : RecyclerView.Adapter<FriendViewHolder>() {
    private val friends = mutableListOf<FriendChat>()

    fun setData(newFriend: List<FriendChat>) {
        friends.clear()
        friends.addAll(newFriend)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = layoutInflater.inflate(R.layout.friend_list, parent, false)
        return FriendViewHolder(view, imageLoader, object: FriendViewHolder.OnClickListener {
            override fun onClick(friend: FriendChat) = onClickListener.onItemClick(friend)
        })
    }

    override fun getItemCount(): Int {
        return friends.size
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        holder.bindData(friends[position])
    }

    interface OnClickListener {
        fun onItemClick(friend: FriendChat)
    }
}