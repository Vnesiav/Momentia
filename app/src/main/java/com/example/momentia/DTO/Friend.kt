package com.example.momentia.DTO

import com.google.firebase.Timestamp

data class Friend (
    val userId: String,
    val username: String,
    val avatarUrl: String?,
    val senderId: String?,
    val sentAt: Timestamp?
)