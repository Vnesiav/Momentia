package com.example.momentia.DTO

import com.google.firebase.Timestamp

data class Chat(
    val chatId: String,
    val userIds: List<String>,
    val lastMessage: String,
    val lastMessageTime: Timestamp
)
