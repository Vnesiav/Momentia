package com.example.momentia.Chat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Visibility
import com.example.momentia.Authentication.BaseAuthFragment
import com.example.momentia.DTO.FriendChat
import com.example.momentia.Friend.FriendAdapter
import com.example.momentia.R
import com.example.momentia.glide.GlideImageLoader
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ChatFragment : BaseAuthFragment() {
    private val db = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private lateinit var searchView: SearchView
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var  loadingText: TextView
    private val friendList = mutableListOf<FriendChat>()
    private val friendChatAdapter by lazy {
        ChatAdapter(layoutInflater, GlideImageLoader(requireContext()), object: ChatAdapter.OnClickListener {
            override fun onItemClick(chat: FriendChat) = navigateToChatMessage(chat)
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        searchView = view.findViewById(R.id.search_friend)
        chatRecyclerView = view.findViewById(R.id.chatRecyclerView)
        loadingText = view.findViewById(R.id.loading)

        chatRecyclerView.adapter = friendChatAdapter
        chatRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        setFontSize(view)
        getChatList(loadingText)
//        setupSearch()

        return view
    }

    private fun getChatList(loadingText: TextView) {
        if (currentUser == null) {
            findNavController().navigate(R.id.loginFragment)
            return
        }

        Log.d("ChatFragment", "Current user UID: ${currentUser.uid}")
        db.collection("chats")
            .orderBy("lastChatTime", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    loadingText.text = "No recent chat"
                } else {
                    result.forEach { document ->
                        Log.d("ChatFragment", "Document ID: ${document.id}, Data: ${document.data}")
                    }

                    // Pastikan menambahkan data friendChat pada thread utama
                    for (document in result) {
                        val docId = document.id
                        val lastChatTime = document.getTimestamp("lastChatTime")
                        val ids = docId.split("_")

                        if (ids.size == 2) {
                            val userId = ids[0]
                            val friendId = ids[1]

                            if (currentUser.uid == userId) {
                                getFriendId(friendId) { friendChat ->
                                    if (friendChat != null) {
                                        getLastMessage(friendId) { lastMessage ->
                                            getLastMessageCount(friendId) { unreadCount ->
                                                friendChat.counter = unreadCount

                                                friendChat.timestamp = lastChatTime
                                                friendChat.lastMessage = lastMessage

                                                friendList.add(friendChat)
                                                Log.d("ChatFragment", "Added friend: $friendChat")

                                                // Pastikan pembaruan UI terjadi pada main thread
                                                activity?.runOnUiThread {
                                                    friendList.sortByDescending { it.timestamp }
                                                    friendChatAdapter.setData(friendList)
                                                    friendChatAdapter.notifyDataSetChanged()
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    loadingText.visibility = View.GONE
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error getting current user document", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
    }

    private fun getLastMessageCount(friendId: String, callback: (Int?) -> Unit) {
        db.collection("chats")
            .whereEqualTo("firstUserId", currentUser?.uid)
            .whereEqualTo("secondUserId", friendId)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val chatId = result.documents[0].id
                    db.collection("chats")
                        .document(chatId)
                        .collection("messages")
                        .whereNotEqualTo("senderId", currentUser?.uid)
                        .whereEqualTo("isRead", false)
                        .get()
                        .addOnSuccessListener { messageResult ->
                            val unreadCount = messageResult.size()
                            callback(unreadCount)
                        }
                        .addOnFailureListener { e ->
                            Log.e("ChatFragment", "Error getting unread messages", e)
                            callback(0)
                        }
                } else {
                    callback(0)
                }
            }
            .addOnFailureListener { e ->
                Log.e("ChatFragment", "Error getting chat document", e)
                callback(0)
            }
    }

    private fun getFriendId(friendId: String, callback: (FriendChat?) -> Unit) {
        db.collection("users")
            .document(friendId)
            .get()
            .addOnSuccessListener { document ->
                val friendChat = FriendChat(
                    userId = friendId,
                    firstName = document.getString("firstName") ?: "Unknown",
                    lastName = document.getString("lastName"),
                    avatarUrl = document.getString("avatarUrl") ?: "",
                    timestamp = document.getTimestamp("lastChatTime"), // Menambahkan lastChatTime
                    lastMessage = null, // Akan diisi setelah memanggil getLastMessage
                    counter = null
                )
                callback(friendChat)
            }
            .addOnFailureListener { e ->
                Log.e("ChatFragment", "Error getting friend document", e)
                callback(null)
            }
    }

    private fun getLastMessage(friendId: String, callback: (String?) -> Unit) {
        db.collection("chats")
            .whereEqualTo("firstUserId", currentUser?.uid)
            .whereEqualTo("secondUserId", friendId)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val chatId = result.documents[0].id
                    db.collection("chats")
                        .document(chatId)
                        .collection("messages")
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                        .limit(1)
                        .get()
                        .addOnSuccessListener { messageResult ->
                            if (!messageResult.isEmpty) {
                                val lastMessage = messageResult.documents[0].getString("messageText")

                                if (lastMessage != null) {
                                    callback(lastMessage)
                                } else {
                                    callback("Photo")
                                }
                            } else {
                                callback(null) // Tidak ada pesan
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("ChatFragment", "Error getting last message", e)
                            callback(null) // Gagal mengambil pesan
                        }
                } else {
                    callback(null) // Tidak ada percakapan
                }
            }
            .addOnFailureListener { e ->
                Log.e("ChatFragment", "Error getting chat document", e)
                callback(null) // Gagal mendapatkan percakapan
            }
    }

    private fun setFontSize(view: View) {
        val searchView = view.findViewById<SearchView>(R.id.search_friend)
        val searchText = searchView.findViewById<TextView>(androidx.appcompat.R.id.search_src_text)
        searchText.textSize = 14f
    }

    private fun navigateToChatMessage(friend: FriendChat) {
        val intent = Intent(requireContext(), ChatMessageActivity::class.java).apply {
            putExtra("userId", friend.userId) // Konsisten dengan "userId"
            putExtra("firstName", friend.firstName) // Konsisten dengan "firstName"
            putExtra("lastName", friend.lastName ?: "") // Konsisten dengan "lastName"
            putExtra("imageUrl", friend.avatarUrl) // Konsisten dengan "imageUrl"
            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        }
        startActivity(intent)
    }
}