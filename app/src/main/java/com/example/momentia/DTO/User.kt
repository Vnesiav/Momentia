package com.example.momentia.DTO

import com.google.firebase.Timestamp

data class User(
    val firstName: String,
    val lastName: String,
    val email: String,
    val username: String,
    val phoneNumber: String,
    val avatarUrl: String? = null,
    val friends: List<String> = emptyList(),
    val snapsReceived: List<String> = emptyList(),
    val snapsSent: List<String> = emptyList(),
    val stories: List<String> = emptyList(),
    val createdAt: Timestamp
)