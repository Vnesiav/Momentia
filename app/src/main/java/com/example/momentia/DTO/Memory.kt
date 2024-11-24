package com.example.momentia.DTO

import com.google.firebase.Timestamp

data class Memory(
    val createdAt: Timestamp = Timestamp.now(),
    val mediaUrl: String = "",
    val location: String = ""
)