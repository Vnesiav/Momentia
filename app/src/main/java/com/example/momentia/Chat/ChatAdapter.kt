package com.example.momentia.Chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.momentia.DTO.Friend
import com.example.momentia.DTO.FriendChat
import com.example.momentia.R
import com.example.momentia.glide.ImageLoader

class ChatAdapter(
    private val layoutInflater: LayoutInflater,
    private val imageLoader: ImageLoader,
    private val onClickListener: OnClickListener
) : RecyclerView.Adapter<ChatViewHolder>() {
    private val chats = mutableListOf<FriendChat>()

    fun setData(newChat: List<FriendChat>) {
        chats.clear()
        chats.addAll(newChat)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChatViewHolder {
        val view = layoutInflater.inflate(R.layout.chat_list, parent, false)
        return ChatViewHolder(view, imageLoader, object: ChatViewHolder.OnClickListener {
            override fun onClick(chat: FriendChat) = onClickListener.onItemClick(chat)
        })
    }

    override fun getItemCount(): Int {
        return chats.size
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bindData(chats[position])
    }

    interface OnClickListener {
        fun onItemClick(chat: FriendChat)
    }
}