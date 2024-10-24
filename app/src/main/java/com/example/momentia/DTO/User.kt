package com.example.momentia.DTO

import com.google.firebase.Timestamp

data class User(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String,
    val createdAt: Timestamp
)