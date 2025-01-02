package com.example.momentia.DTO

import com.google.firebase.Timestamp

data class FriendChat(
    val userId: String,
    val firstName: String,
    var lastName: String?,
    val avatarUrl: String?,
    var timestamp: Timestamp?,
    var lastMessage: String?,
    var counter: Int?,
    val photoUrl: String?,
    var isRead: Boolean = false
)
