package com.example.momentia.Camera

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.momentia.DTO.FriendChat
import com.example.momentia.R
import com.example.momentia.glide.GlideImageLoader
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore

class SendPhotoActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private lateinit var friendListRecyclerView: RecyclerView
    private val friendList = mutableListOf<FriendChat>()

    private val friendAdapter by lazy {
        FriendListAdapter(
            layoutInflater,
            GlideImageLoader(this),
            object : FriendListAdapter.OnClickListener {
                override fun onItemClick(friend: FriendChat) {
                    // Handle sending photo to selected friend
                    sendPhotoToFriend(friend)
                }
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.send_to_friend)

        // Initialize RecyclerView
        friendListRecyclerView = findViewById(R.id.friend_list_recycler)
        friendListRecyclerView.layoutManager = LinearLayoutManager(this)
        friendListRecyclerView.adapter = friendAdapter

        // Load friends
        getFriendList()
    }

    private fun getFriendList() {
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        // Fetch user's friends from Firestore
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
                                        avatarUrl = friendDoc.getString("avatarUrl") ?: ""
                                    )
                                }

                                // Update the friend list and adapter
                                friendList.clear()
                                friendList.addAll(friends)
                                friendAdapter.setData(friends)
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

    private fun sendPhotoToFriend(friend: FriendChat) {
        // Placeholder for sending photo logic
        Toast.makeText(this, "Photo sent to ${friend.firstName}", Toast.LENGTH_SHORT).show()
    }
}
