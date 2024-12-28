package com.example.momentia.Camera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.momentia.DTO.FriendChat
import com.example.momentia.DTO.Memory
import com.example.momentia.R
import com.example.momentia.glide.GlideImageLoader
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class SendPhotoActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val storage = FirebaseStorage.getInstance()
    private lateinit var friendListRecyclerView: RecyclerView
    private lateinit var searchBar: SearchView
    private lateinit var sendButton: ImageButton
    private val friendList = mutableListOf<FriendChat>()
    private var filteredFriendList = mutableListOf<FriendChat>()
    private var selectedFriend: FriendChat? = null
    private var capturedImage: Bitmap? = null
    private var isSending = false

    private val friendAdapter by lazy {
        FriendListAdapter(
            layoutInflater,
            GlideImageLoader(this),
            object : FriendListAdapter.OnClickListener {
                override fun onItemClick(friend: FriendChat) {
                    selectedFriend = friend
                    Toast.makeText(
                        this@SendPhotoActivity,
                        "Selected: ${friend.firstName}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.send_to_friend)

        val imageUriString = intent.getStringExtra("capturedImageUri")
        if (imageUriString != null) {
            val imageUri = Uri.parse(imageUriString)
            capturedImage = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
        }

        friendListRecyclerView = findViewById(R.id.friend_list_recycler)
        friendListRecyclerView.layoutManager = LinearLayoutManager(this)
        friendListRecyclerView.adapter = friendAdapter

        searchBar = findViewById(R.id.search_bar)
        setupSearchBar()

        sendButton = findViewById(R.id.send_button)
        sendButton.setOnClickListener {
            if (isSending) {
                Toast.makeText(this, "Sending in progress, please wait...", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedFriend == null) {
                Toast.makeText(this, "Please select a friend to send the photo", Toast.LENGTH_SHORT).show()
            } else if (capturedImage == null) {
                Toast.makeText(this, "No image to send", Toast.LENGTH_SHORT).show()
            } else {
                isSending = true
                sendPhotoToFriend(selectedFriend!!, capturedImage!!)
            }
        }


        val byteArray = intent.getByteArrayExtra("capturedImage")
        if (capturedImage == null && byteArray != null) {
            capturedImage = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        }

        getFriendList()
    }

    private fun setupSearchBar() {
        searchBar = findViewById(R.id.search_bar)
        searchBar.queryHint = getString(R.string.search)
        searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val query = newText?.trim() ?: ""
                filterFriendList(query)
                return true
            }
        })
    }

    private fun filterFriendList(query: String) {
        filteredFriendList.clear()
        if (query.isEmpty()) {
            filteredFriendList.addAll(friendList)
        } else {
            filteredFriendList.addAll(
                friendList.filter { friend ->
                    friend.firstName.contains(query, ignoreCase = true) ||
                            (friend.lastName?.contains(query, ignoreCase = true) ?: false)
                }
            )
        }
        friendAdapter.setData(filteredFriendList)
    }

    private fun getFriendList() {
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val friendIds = documentSnapshot.get("friends") as? List<String> ?: emptyList()

                    if (friendIds.isNotEmpty()) {
                        db.collection("users")
                            .whereIn(FieldPath.documentId(), friendIds)
                            .get()
                            .addOnSuccessListener { friendsSnapshot ->
                                val friends = friendsSnapshot.documents.mapNotNull { friendDoc ->
                                    FriendChat(
                                        userId = friendDoc.id,
                                        firstName = friendDoc.getString("firstName") ?: "Unknown",
                                        lastName = friendDoc.getString("lastName"),
                                        avatarUrl = friendDoc.getString("avatarUrl") ?: "",
                                        timestamp = null,
                                        lastMessage = null,
                                        photoUrl = null,
                                        counter = null
                                    )
                                }

                                friendList.clear()
                                friendList.addAll(friends)
                                filterFriendList("")
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error loading friends", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(this, "No friends found", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error fetching user data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendPhotoToFriend(friend: FriendChat, image: Bitmap) {
        val chatId = "${currentUser!!.uid}_${friend.userId}"
        val receiverChatId = "${friend.userId}_${currentUser.uid}"

        val messageId = db.collection("chats")
            .document(chatId)
            .collection("messages")
            .document()
            .id

        val receiverMessageId = db.collection("chats")
            .document(receiverChatId)
            .collection("messages")
            .document()
            .id

        val imageRef = storage.reference.child("chat_images/$chatId/$messageId.jpg")
        val baos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageData = baos.toByteArray()

        imageRef.putBytes(imageData)
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    throw task.exception ?: Exception("Image upload failed")
                }
                imageRef.downloadUrl
            }.addOnCompleteListener { task ->
                isSending = false
                sendButton.isEnabled = true

                if (task.isSuccessful) {
                    val downloadUrl = task.result.toString()
                    saveMessageAndMemory(friend, chatId, receiverChatId, messageId, receiverMessageId, downloadUrl)
                } else {
                    showToast("Failed to upload photo")
                }
            }
    }

    private fun saveMessageAndMemory(
        friend: FriendChat,
        chatId: String,
        receiverChatId: String,
        messageId: String,
        receiverMessageId: String,
        downloadUrl: String
    ) {
        val message = mapOf(
            "senderId" to currentUser!!.uid,
            "photoUrl" to downloadUrl,
            "timestamp" to Timestamp.now(),
            "isRead" to false
        )

        val chat = mapOf(
            "firstUserId" to currentUser!!.uid,
            "secondUserId" to friend.userId,
            "lastChatTime" to Timestamp.now()
        )

        val receiverChat = mapOf(
            "firstUserId" to friend.userId,
            "secondUserId" to currentUser.uid,
            "lastChatTime" to Timestamp.now()
        )

        db.collection("chats").document(chatId).set(chat, SetOptions.merge())
            .addOnSuccessListener {
                db.collection("chats").document(receiverChatId).set(receiverChat, SetOptions.merge())
                    .addOnSuccessListener {
                        db.collection("chats").document(chatId).collection("messages").document(messageId).set(message)
                            .addOnSuccessListener {
                                db.collection("chats").document(receiverChatId).collection("messages").document(receiverMessageId).set(message)
                                    .addOnSuccessListener {
                                        val memory = Memory(
                                            location = null,
                                            mediaUrl = downloadUrl,
                                            senderId = currentUser!!.uid,
                                            receiverId = friend.userId,
                                            sentAt = Timestamp.now(),
                                            viewed = false
                                        )

                                        db.collection("memories").add(memory)
                                            .addOnSuccessListener {
                                                showToast("Photo sent and saved to memories")
                                                finish()
                                            }
                                            .addOnFailureListener { showToast("Failed to save photo to memories") }
                                    }
                                    .addOnFailureListener { showToast("Failed to send photo") }
                            }
                            .addOnFailureListener { showToast("Failed to send photo") }
                    }
                    .addOnFailureListener { showToast("Failed to update chat") }
            }
            .addOnFailureListener { showToast("Failed to update chat") }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}