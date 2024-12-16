package com.example.momentia.Chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.momentia.DTO.Chat
import com.example.momentia.R
import com.example.momentia.glide.GlideImageLoader
import com.example.momentia.glide.GlideImageLoaderCircle

class ChatMessageAdapter(
    private val messages: List<Chat>,
    private val friendImageUrl: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val SENT_TYPE = 1
    private val RECEIVED_TYPE = 2

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isSentByCurrentUser) SENT_TYPE else RECEIVED_TYPE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == SENT_TYPE) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_chat_sent, parent, false)
            SentMessageViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_chat_received, parent, false)
            ReceivedMessageViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val chatMessage = messages[position]

        if (holder is SentMessageViewHolder) {
            holder.bind(chatMessage)
        } else if (holder is ReceivedMessageViewHolder) {
            holder.bind(chatMessage, friendImageUrl)
        }
    }

    override fun getItemCount(): Int = messages.size

    class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.message_text)
        private val messageImage: ImageView = itemView.findViewById(R.id.message_image)

        fun bind(message: Chat) {
            if (!message.photoUrl.isNullOrEmpty()) {
                messageImage.visibility = View.VISIBLE
                messageText.visibility = View.GONE
                // Load image using Glide or another library
                GlideImageLoaderCircle(itemView.context).loadImage(message.photoUrl, messageImage)
            } else {
                messageText.visibility = View.VISIBLE
                messageImage.visibility = View.GONE
                messageText.text = message.message
            }
        }
    }

    class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.message_text)
        private val messageImage: ImageView = itemView.findViewById(R.id.message_image)
        private val profileImage: ImageView = itemView.findViewById(R.id.profile_image)

        fun bind(message: Chat, friendImageUrl: String) {
            if (!message.photoUrl.isNullOrEmpty()) {
                messageImage.visibility = View.VISIBLE
                messageText.visibility = View.GONE
                // Load image for the message
                GlideImageLoader(itemView.context).loadImage(message.photoUrl, messageImage)
            } else {
                messageText.visibility = View.VISIBLE
                messageImage.visibility = View.GONE
                // Ensure that text is not empty
                messageText.text = message.message ?: ""  // Ensure message is not null
            }

            // Load the friend's profile image
            GlideImageLoaderCircle(itemView.context).loadImage(friendImageUrl, profileImage)
        }
    }
}