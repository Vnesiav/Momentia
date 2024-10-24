package com.example.momentia.DTO

import com.google.firebase.Timestamp

data class Message (
    val messageId: String,
    val senderId: String,
    val messageText: String?,
    val mediaUrl: String?,
    val sentAt: Timestamp
)