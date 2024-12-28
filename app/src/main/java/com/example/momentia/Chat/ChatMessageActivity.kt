package com.example.momentia.Chat

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
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
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class ChatMessageActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val storage = FirebaseStorage.getInstance()
    private val PICK_IMAGE_REQUEST = 1

    private lateinit var userId: String
    private lateinit var firstName: String
    private lateinit var lastName: String
    private lateinit var imageUrl: String

    private lateinit var friendPicture: ImageView
    private lateinit var friendName: TextView

    private lateinit var backButton: ImageButton
    private lateinit var sendButton: ImageButton
    private lateinit var galleryButton: ImageButton

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var chatAdapter: ChatMessageAdapter
    private val messages = mutableListOf<Chat>()
    private var messageListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat_message)

        val intent = intent
        intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT

        // Get data from Intent
        userId = intent.getStringExtra("userId") ?: ""
        firstName = intent.getStringExtra("firstName") ?: ""
        lastName = intent.getStringExtra("lastName") ?: ""
        imageUrl = intent.getStringExtra("imageUrl") ?: "none"

        chatRecyclerView = findViewById(R.id.message_container)
        chatAdapter = ChatMessageAdapter(messages, imageUrl)

        chatRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatMessageActivity)
            adapter = chatAdapter
        }

        fetchMessages()

        // Update adapter
        chatAdapter.notifyDataSetChanged()

        // Initialize views
        friendPicture = findViewById(R.id.friend_profile_picture)
        friendName = findViewById(R.id.friend_name)

        // Set data to views
        setIdentity(firstName, lastName, imageUrl)

        backButton = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            finish()
        }

        sendButton = findViewById(R.id.send_button)
        sendButton.setOnClickListener {
            sendMessage(userId)
        }

        galleryButton = findViewById(R.id.gallery_button)
        galleryButton.setOnClickListener {
            openGallery()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        messageListener?.remove()
    }

    private fun setIdentity(firstName: String, lastName: String, imageUrl: String) {
        friendName.text = "$firstName $lastName"
        val glideImageLoader = GlideImageLoaderCircle(this)

        if (imageUrl == "none" || imageUrl == "") {
            friendPicture.setImageResource(R.drawable.account_circle)
        } else {
            glideImageLoader.loadImage(imageUrl, friendPicture)
        }
    }

    private fun sendMessage(userId: String) {
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

            val chatUserId = hashMapOf(
                "firstUserId" to currentUser.uid,
                "secondUserId" to friendId,
                "lastChatTime" to Timestamp.now()
            )

            val secondChatUserId = hashMapOf(
                "firstUserId" to friendId,
                "secondUserId" to currentUser.uid,
                "lastChatTime" to Timestamp.now()
            )

            val message = hashMapOf(
                "senderId" to currentUser.uid,
                "messageText" to messageInput.text.toString(),
                "timestamp" to Timestamp.now(),
                "isRead" to false
            )

            db.collection("chats")
                .document(chatId)
                .set(chatUserId)
                .addOnSuccessListener {
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
                        .set(secondChatUserId)
                        .addOnSuccessListener {
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
                        }
                        .addOnFailureListener { e ->
                            Log.e("ChatMessageActivity", "Set secondChatUserId failed")
                        }
                }
                .addOnFailureListener { e ->
                    Log.e("ChatMessageActivity", "Set chatUserId failed")
                }

        } else {
            Log.e("ChatMessageActivity", "Current user is null or message input is empty")
        }
    }

    private fun getChatId(firstId: String, secondId: String): String {
        Log.d("ChatMessageActivity", "${firstId}_${secondId}")
        return "${firstId}_${secondId}"
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
                .orderBy("timestamp")
                .addSnapshotListener { querySnapshot, error ->
                    if (error != null) {
                        Log.e("ChatMessageActivity", "Failed to listen for messages: ${error.message}")
                        return@addSnapshotListener
                    }

                    if (querySnapshot != null) {
                        messages.clear() // Clear previous messages
                        for (document in querySnapshot) {
                            val messageText = document.getString("messageText") ?: ""
                            val senderId = document.getString("senderId") ?: ""
                            val photoUrl = document.getString("photoUrl")
                            val timestamp = document.getTimestamp("timestamp")
                            val isRead = document.getBoolean("isRead") ?: false

                            if (timestamp != null) {
                                // Create Chat object based on retrieved data
                                val chatMessage = Chat(
                                    senderId = senderId,
                                    message = messageText,
                                    lastMessageTime = timestamp,
                                    isRead = isRead,
                                    isSentByCurrentUser = senderId == currentUser.uid,
                                    photoUrl = photoUrl // Include photo URL (null if not available)
                                )

                                messages.add(chatMessage)
                            }
                        }
                        chatAdapter.notifyDataSetChanged() // Notify adapter of data change
                        chatRecyclerView.scrollToPosition(messages.size - 1) // Scroll to the latest message

                        markMessageAsRead(chatId)
                    }
                }
        }
    }

    private fun markMessageAsRead(chatId: String) {
        // Fetch unread messages that were not sent by the current user
        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .whereEqualTo("isRead", false) // Only fetch unread messages
            .whereNotEqualTo("senderId", FirebaseAuth.getInstance().currentUser?.uid) // Exclude messages sent by the current user
            .get()
            .addOnSuccessListener { result ->
                // Check if there are any unread messages
                if (result.isEmpty) {
                    Log.d("ChatMessageActivity", "No unread messages")
                    return@addOnSuccessListener
                }

                // Prepare a batch for updating multiple documents at once
                val batch = db.batch()
                for (document in result) {
                    // Get the reference to the message document
                    val messageRef = document.reference

                    // Add an update operation to the batch to mark the message as read
                    batch.update(messageRef, "isRead", true)
                }

                // Commit the batch update
                batch.commit()
                    .addOnSuccessListener {
                        Log.d("ChatMessageActivity", "All messages marked as read successfully")
                    }
                    .addOnFailureListener { e ->
                        Log.e("ChatMessageActivity", "Error marking messages as read", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("ChatMessageActivity", "Error fetching unread messages", e)
            }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val imageUri: Uri? = data.data
            if (imageUri != null) {
                showConfirmationDialog(imageUri)
            }
        }
    }

    private fun showConfirmationDialog(imageUri: Uri) {
        val dialogView = layoutInflater.inflate(R.layout.confirmation_dialog_send_photo, null)
        val imageView = dialogView.findViewById<ImageView>(R.id.dialog_image)
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
        imageView.setImageBitmap(bitmap)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialogView.findViewById<ImageButton>(R.id.button_send).setOnClickListener {
            uploadImageToFirebase(imageUri)
            dialog.dismiss()
        }

        dialogView.findViewById<ImageButton>(R.id.button_cancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun uploadImageToFirebase(imageUri: Uri) {
        val chatId = getChatId(currentUser!!.uid, userId)
        val messageId = db.collection("chats")
            .document(chatId)
            .collection("messages")
            .document()
            .id

        val imageRef = storage.reference.child("chat_images/$chatId/$messageId.jpg")
        val baos = ByteArrayOutputStream()
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageData = baos.toByteArray()

        imageRef.putBytes(imageData)
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    throw task.exception ?: Exception("Image upload failed")
                }
                imageRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUrl = task.result.toString()
                    val message = mapOf(
                        "senderId" to currentUser.uid,
                        "photoUrl" to downloadUrl,
                        "timestamp" to Timestamp.now(),
                        "isRead" to false
                    )

                    db.collection("chats")
                        .document(chatId)
                        .collection("messages")
                        .document(messageId)
                        .set(message)
                        .addOnSuccessListener {
                            Log.d("ChatMessageActivity", "Photo sent successfully")
                        }
                        .addOnFailureListener { e ->
                            Log.e("ChatMessageActivity", "Failed to send photo: ${e.message}")
                        }
                } else {
                    Log.e("ChatMessageActivity", "Failed to upload photo")
                }
            }
    }
}