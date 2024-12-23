package com.example.momentia.DTO

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class Memory(
    val location: GeoPoint? = null,
    val mediaUrl: String = "",
    val receiverId: String = "",
    val senderId: String = "",
    val sentAt: Timestamp? = null,
    val viewed: Boolean = false
){
    val formattedDate: String?
        get() = sentAt?.let {
            val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            dateFormat.format(it.toDate())
        }

    val monthYear: String?
        get() = sentAt?.let {
            val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            dateFormat.format(it.toDate())
        }

    val month: Int?
        get() = sentAt?.toDate()?.let {
            val calendar = Calendar.getInstance()
            calendar.time = it
            calendar.get(Calendar.MONTH)
        }

    val year: Int?
        get() = sentAt?.toDate()?.let {
            val calendar = Calendar.getInstance()
            calendar.time = it
            calendar.get(Calendar.YEAR)
        }
}