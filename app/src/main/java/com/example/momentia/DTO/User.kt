package com.example.momentia.DTO

import com.google.firebase.Timestamp

data class User(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val username: String = "",
    val phoneNumber: String = "",
    val avatarUrl: String? = null,
    val friends: List<String> = emptyList(),
    val createdAt: Timestamp = Timestamp.now()
) {
    // No-argument constructor required for Firestore deserialization
    constructor() : this("", "", "", "", "", null, emptyList(), Timestamp.now())
}