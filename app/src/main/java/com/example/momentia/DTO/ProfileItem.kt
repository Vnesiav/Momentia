package com.example.momentia.DTO

data class ProfileItem(
    val title: String,
    val iconResId: Int,
    val action: () -> Unit
)
