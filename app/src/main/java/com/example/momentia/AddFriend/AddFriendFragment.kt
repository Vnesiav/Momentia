package com.example.momentia.AddFriend

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.momentia.Authentication.BaseAuthFragment
import com.example.momentia.DTO.Friend
import com.example.momentia.DTO.FriendRequest
import com.example.momentia.R
import com.example.momentia.glide.GlideImageLoader
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.w3c.dom.Text

class AddFriendFragment : BaseAuthFragment() {
    private lateinit var backButton: ImageButton
    private lateinit var searchView: SearchView
    private lateinit var addFriendRecyclerView: RecyclerView
    private lateinit var friendRequestRecyclerView: RecyclerView
    private var currentUserFriends = mutableListOf<String>()

    private var friendList = mutableListOf<Friend>()
    private var filteredList = mutableListOf<Friend>()

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private val requestAdapter by lazy {
        FriendRequestAdapter(
            layoutInflater,
            GlideImageLoader(requireContext())
        )
    }

    private val friendAdapter by lazy {
        AddFriendAdapter(
            layoutInflater,
            GlideImageLoader(requireContext()),
            { friend -> sendFriendRequest(friend) }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_friend, container, false)
        setFontSize(view)

        backButton = view.findViewById(R.id.back_button)
        searchView = view.findViewById(R.id.search_friend)
        addFriendRecyclerView = view.findViewById(R.id.add_friend_recycler)
        friendRequestRecyclerView = view.findViewById(R.id.friend_request_recycler)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Initialize RecyclerView and Adapter
        addFriendRecyclerView.adapter = friendAdapter
        addFriendRecyclerView.layoutManager = LinearLayoutManager(context)

        friendRequestRecyclerView.adapter = requestAdapter
        friendRequestRecyclerView.layoutManager = LinearLayoutManager(context)

        backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        showRandomUsers()

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchFriends(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchFriends(newText)
                return true
            }
        })

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showFriendRequests()
    }

    private fun showFriendRequests() {
        Log.d("ShowFriendRequests", "showFriendRequests called")
        val currentUser = auth.currentUser

        if (currentUser != null) {
            Log.d("ShowFriendRequests", "User is not null")
            db.collection("users")
                .document(currentUser.uid)
                .collection("friendRequests")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val friendRequests = mutableListOf<FriendRequest>()

                    if (!querySnapshot.isEmpty) {
                        Log.d("ShowFriendRequests", "Snapshot is empty, hiding views")
                        val addedTitle = view?.findViewById<TextView>(R.id.added_me)
                        val underline1 = view?.findViewById<View>(R.id.view1)

                        addedTitle?.visibility = View.VISIBLE
                        underline1?.visibility = View.VISIBLE
                    }

                    for (request in querySnapshot.documents) {
                        val avatarUrl = request.getString("avatarUrl") ?: ""
                        val senderId = request.getString("senderId") ?: ""
                        val username = request.getString("username") ?: ""
                        val sentAt = request.getTimestamp("sentAt")

                        Log.d("ShowFriendRequests", "Avatar URL: $avatarUrl" + "Sender ID: $senderId" + "Username: $username" + "Sent At: $sentAt")

                        // Fetch first name from the sender's document
                        if (senderId.isNotEmpty()) {
                            db.collection("users").document(senderId).get().addOnSuccessListener { document ->
                                val firstName = document.getString("firstName") ?: ""

                                // Create FriendRequest object if sentAt is not null
                                sentAt?.let {
                                    val friendRequest = FriendRequest(senderId, username, avatarUrl, firstName, it)
                                    friendRequests.add(friendRequest)

                                    // Update the adapter with the friend requests once all requests are processed
                                    if (friendRequests.size == querySnapshot.size()) {
                                        // Assuming you have an instance of your FriendRequestAdapter
                                        requestAdapter.setData(friendRequests) // Update this line accordingly
                                    }
                                }
                            }
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w("ShowFriendRequests", "Error getting documents: ", exception)
                }
        } else {
            Log.d("ShowFriendRequests", "User is null")
        }
    }


    private fun sendFriendRequest(friend: Friend) {
        val currentUser = auth.currentUser
        val context = requireContext()

        if (currentUser != null) {
            db.collection("users")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    // Log the document to verify user data
                    Log.d("SendFriendRequest", "User document: ${document.data}")

                    val friendRequestData = FriendRequest(
                        senderId = currentUser.uid,
                        username = document.getString("username").toString(),
                        avatarUrl = document.getString("avatarUrl").toString(),
                        firstName = document.getString("firstName").toString(),
                        sentAt = Timestamp.now()
                    )
                    db.collection("users")
                        .document(friend.userId) // Sending request to this user
                        .collection("friendRequests")
                        .add(friendRequestData)
                        .addOnSuccessListener {
                            Log.d("SendFriendRequest", "Friend request sent successfully to ${friend.userId}")
                            Toast.makeText(context, "Friend request sent", Toast.LENGTH_SHORT).show()

                            // Update friend list after sending the request
                            updateFriendListAfterRequest(friend.userId)
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(context, "Failed to send friend request", Toast.LENGTH_SHORT).show()
                            exception.printStackTrace()
                        }
                }.addOnFailureListener { exception ->
                    Toast.makeText(context, "Failed to send friend request", Toast.LENGTH_SHORT).show()
                    exception.printStackTrace()
                }
        } else {
            Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateFriendListAfterRequest(friendUserId: String) {
        // Add the friend's user ID to the current user's friends list (locally)
        currentUserFriends.add(friendUserId)

        // Notify the adapter to refresh the data
        friendAdapter.setData(filteredList, currentUserFriends)
    }

    private fun searchFriends(query: String?) {
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (!TextUtils.isEmpty(query)) {
            val searchText = query!!.lowercase()

            db.collection("users")
                .orderBy("username")
                .startAt(searchText)
                .endAt(searchText + "\uf8ff")
                .get()
                .addOnSuccessListener { result ->
                    friendList.clear()
                    for (document in result) {
                        val userId = document.id
                        if (userId == currentUser?.uid) {
                            continue
                        }

                        val username = document.getString("username") ?: ""
                        val avatarUrl = document.getString("avatarUrl")

                        val friend = Friend(userId, username, avatarUrl, null, null)
                        friendList.add(friend)
                    }

                    filteredList.clear()
                    filteredList.addAll(friendList)
                    friendAdapter.setData(filteredList, null)
                }
                .addOnFailureListener { exception ->
                    exception.printStackTrace()
                }
        } else {
            showRandomUsers()
        }
    }

    private fun showRandomUsers() {
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            db.collection("users")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    val friends = document.get("friends") as? List<String> ?: emptyList()

                    db.collection("users")
                        .limit(10)
                        .get()
                        .addOnSuccessListener { result ->
                            filteredList.clear()
                            for (document in result) {
                                val userId = document.id
                                if (userId == currentUser.uid || friends.contains(userId)) {
                                    continue
                                }

                                val username = document.getString("username") ?: ""
                                val avatarUrl = document.getString("avatarUrl") ?: ""

                                val friend = Friend(userId, username, avatarUrl, null, null)
                                filteredList.add(friend)
                            }
                            friendAdapter.setData(filteredList, friends)
                        }
                        .addOnFailureListener { exception ->
                            exception.printStackTrace()
                        }
                }
                .addOnFailureListener { exception ->
                    exception.printStackTrace()
                }
        }
    }

    private fun setFontSize(view: View) {
        val searchView = view.findViewById<SearchView>(R.id.search_friend)
        val searchText = searchView.findViewById<TextView>(androidx.appcompat.R.id.search_src_text)
        searchText.textSize = 14f
    }
}
