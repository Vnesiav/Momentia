package com.example.momentia.Chat

import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.momentia.DTO.Chat
import com.example.momentia.R
import com.example.momentia.glide.GlideImageLoaderCircle
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class ChatMessageActivity : AppCompatActivity() {
    private lateinit var userId: String
    private lateinit var firstName: String
    private lateinit var lastName: String
    private lateinit var imageUrl: String

    private lateinit var friendPicture: ImageView
    private lateinit var friendName: TextView

    private lateinit var backButton: ImageButton
    private lateinit var sendButton: ImageButton

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<Chat>()
    private var messageListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat_message)

        // Mendapatkan data dari Intent
        userId = intent.getStringExtra("userId") ?: ""
        firstName = intent.getStringExtra("firstName") ?: ""
        lastName = intent.getStringExtra("lastName") ?: ""
        imageUrl = intent.getStringExtra("imageUrl") ?: "none"

        Log.d("ChatMessageActivity", "UserId: $userId")
        Log.d("ChatMessageActivity", "ImageUrl: $imageUrl")


        chatRecyclerView = findViewById(R.id.message_container)
        chatAdapter = ChatAdapter(messages, imageUrl)

        chatRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatMessageActivity)
            adapter = chatAdapter
        }

        fetchMessages()

        // Update adapter
        chatAdapter.notifyDataSetChanged()

        // Inisialisasi tampilan
        friendPicture = findViewById(R.id.friend_profile_picture)
        friendName = findViewById(R.id.friend_name)

        // Mengatur data pada tampilan
        setIdentity(firstName, lastName, imageUrl)

        backButton = findViewById(R.id.back_button)
        backButton.setOnClickListener {

        }

        sendButton = findViewById(R.id.send_button)
        sendButton.setOnClickListener {
            sendMessage(userId)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        messageListener?.remove() // Hentikan listener
    }

    private fun setIdentity(firstName: String, lastName: String, imageUrl: String) {
        friendName.text = "$firstName $lastName"
        val glideImageLoader = GlideImageLoaderCircle(this)
        glideImageLoader.loadImage(imageUrl, friendPicture)
    }

    private fun sendMessage(userId: String) {
        val db = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser
        val friendId = userId
        val messageInput = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.message_input)

        if (currentUser != null && !messageInput.text.isNullOrEmpty()) {
            val chatId = getChatId(currentUser.uid, friendId)
            val senderChatId = getChatId(friendId, currentUser.uid)
            val messageId = db.collection("chats")
                .document(chatId)
                .collection("messages")
                .document()
                .id
            val senderMessageId = db.collection("chats")
                .document(senderChatId)
                .collection("messages")
                .document()
                .id
            val message = hashMapOf(
                "senderId" to currentUser.uid,
                "messageText" to messageInput.text.toString(),
                "timestamp" to Timestamp.now(),
                "isRead" to false
            )

            db.collection("chats")
                .document(chatId)
                .collection("messages")
                .document(messageId)
                .set(message)
                .addOnSuccessListener {
                    Log.d("ChatMessageActivity", "Message sent successfully")
                    messageInput.text?.clear() // Clear the input field after sending
                }
                .addOnFailureListener { e ->
                    Log.e("ChatMessageActivity", "Failed to send message: ${e.message}")
                }

            db.collection("chats")
                .document(senderChatId)
                .collection("messages")
                .document(senderMessageId)
                .set(message)
                .addOnSuccessListener {
                    Log.d("ChatMessageActivity", "Message received successfully")
                    messageInput.text?.clear() // Clear the input field after sending
                    chatRecyclerView.scrollToPosition(messages.size - 1)
                }
                .addOnFailureListener { e ->
                    Log.e("ChatMessageActivity", "Failed to receive message: ${e.message}")
                }
        } else {
            Log.e("ChatMessageActivity", "Current user is null or message input is empty")
        }
    }

    private fun getChatId(firstId: String, secondId: String): String {
       return "${firstId}_$secondId"
    }

    private fun fetchMessages() {
        val db = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser
        val friendId = userId

        if (currentUser != null) {
            val chatId = getChatId(currentUser.uid, friendId)

            db.collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp") // Order by timestamp to display messages in correct order
                .addSnapshotListener { querySnapshot, error ->
                    if (error != null) {
                        Log.e("ChatMessageActivity", "Failed to listen for messages: ${error.message}")
                        return@addSnapshotListener
                    }

                    if (querySnapshot != null) {
                        messages.clear() // Clear previous messages
                        for (document in querySnapshot) {
                            val message = document.getString("messageText").toString()
                            val senderId = document.getString("senderId").toString()
                            val timestamp = document.getTimestamp("timestamp")
                            val isRead = document.getBoolean("isRead") ?: false
                            timestamp?.let { Chat(senderId, message, it, isRead, senderId == currentUser.uid) }
                                ?.let { messages.add(it) }
                        }
                        chatAdapter.notifyDataSetChanged() // Notify adapter of data change
                        chatRecyclerView.scrollToPosition(messages.size - 1) // Scroll to the latest message
                    }
                }
        }
    }

}

