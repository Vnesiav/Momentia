package com.example.momentia.DTO

import com.google.firebase.Timestamp

data class FriendRequest(
    val senderId: String,
    val username: String,
    val firstName: String?,
    val avatarUrl: String?,
    val sentAt: Timestamp
)
