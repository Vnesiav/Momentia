package com.example.momentia.DTO

import com.google.firebase.Timestamp

data class Chat(
//    val chatId: String,
    val senderId: String,
    val message: String,
    val lastMessageTime: Timestamp,
    val isRead: Boolean,
    val isSentByCurrentUser: Boolean
)
