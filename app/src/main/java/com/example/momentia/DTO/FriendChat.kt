package com.example.momentia.DTO

import com.google.firebase.Timestamp

data class FriendChat(
    val userId: String,
    val firstName: String,
    val lastName: String?,
    val avatarUrl: String?,
    var timestamp: Timestamp?,
    var lastMessage: String?,
    var counter: Int?,
    var isRead: Boolean = false
)
