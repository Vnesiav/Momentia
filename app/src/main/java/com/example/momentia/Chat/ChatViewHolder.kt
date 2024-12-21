package com.example.momentia.Chat

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.momentia.DTO.FriendChat
import com.example.momentia.R
import com.example.momentia.glide.ImageLoader

class ChatViewHolder(
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

    private val lastMessage: TextView by lazy {
        containerView.findViewById(R.id.last_message)
    }

    private val counter: TextView by lazy {
        containerView.findViewById(R.id.new_msg_counter)
    }

    private val readStatus: ImageView by lazy {
        containerView.findViewById(R.id.read_status)
    }

    fun bindData(chat: FriendChat) {
        containerView.setOnClickListener {
            onClickListener.onClick(chat)
        }

        if (!chat.avatarUrl.isNullOrEmpty()) {
            imageLoader.loadImage(chat.avatarUrl, profilePicture)
        } else {
            profilePicture.setImageResource(R.drawable.account_circle)
        }

        if (chat.counter!! > 9) {
            counter.text = "9+"
        } else if (chat.counter!! <= 0)  {
            counter.visibility = View.GONE
        } else {
            counter.text = chat.counter.toString()
        }

        if (chat.isRead) {
            readStatus.setImageResource(R.drawable.read_icon) // Use setImageResource here
        } else {
            readStatus.setImageResource(R.drawable.not_read) // Use setImageResource here
        }

        name.text = "${chat.firstName} ${chat.lastName ?: ""}"
        lastMessage.text = chat.lastMessage.toString()
    }

    interface OnClickListener {
        fun onClick(chat: FriendChat)
    }
}