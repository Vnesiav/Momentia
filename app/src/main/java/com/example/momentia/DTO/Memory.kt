package com.example.momentia.DTO

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

data class Memory(
    val location: GeoPoint? = null,
    val mediaUrl: String = "",
    val receiverId: String = "",
    val senderId: String = "",
    val sentAt: Timestamp? = null,
    val viewed: Boolean = false
)