package com.example.momentia.AddFriend

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.momentia.Authentication.BaseAuthFragment
import com.example.momentia.DTO.Friend
import com.example.momentia.DTO.FriendRequest
import com.example.momentia.R
import com.example.momentia.glide.GlideImageLoader
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class AddFriendFragment : BaseAuthFragment() {
    private lateinit var backButton: ImageButton
    private lateinit var searchView: SearchView
    private lateinit var searchText: TextView

    private lateinit var addFriendRecyclerView: RecyclerView
    private lateinit var friendRequestRecyclerView: RecyclerView
    private var currentUserFriends = mutableListOf<String>()

    private lateinit var addedTitle: TextView
    private lateinit var addedUnderline: View

    private lateinit var quickAddTitle: TextView
    private lateinit var quickAddUnderline: View

    private lateinit var warningText: TextView

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var updateTimeRunnable: Runnable

    private var friendList = mutableListOf<Friend>()
    private var filteredList = mutableListOf<Friend>()

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser

    private val requestAdapter by lazy {
        FriendRequestAdapter(
            layoutInflater,
            GlideImageLoader(requireContext()),
            { friendRequest ->
                // Accept friend request logic
                val currentUser = FirebaseAuth.getInstance().currentUser

                if (currentUser != null) {
                    val userDocRef = db.collection("users").document(currentUser.uid)

                    // Tambahkan senderId ke daftar teman currentUser
                    userDocRef.update("friends", FieldValue.arrayUnion(friendRequest.senderId))
                        .addOnSuccessListener {
                            // Tambahkan currentUser.uid ke daftar teman sender
                            db.collection("users").document(friendRequest.senderId)
                                .update("friends", FieldValue.arrayUnion(currentUser.uid))
                                .addOnSuccessListener {
                                    // Hapus permintaan pertemanan setelah diterima
                                    db.collection("users")
                                        .document(currentUser.uid)
                                        .collection("friendRequests")
                                        .whereEqualTo("senderId", friendRequest.senderId)
                                        .get()
                                        .addOnSuccessListener { querySnapshot ->
                                            for (document in querySnapshot) {
                                                db.collection("users")
                                                    .document(currentUser.uid)
                                                    .collection("friendRequests")
                                                    .document(document.id)
                                                    .delete()
                                                    .addOnSuccessListener {
                                                        Log.d("FriendRequest", "Friend request accepted, both users are now friends.")
                                                        Toast.makeText(context, "Friend request accepted", Toast.LENGTH_SHORT).show()
                                                    }
                                            }
                                        }
                                }
                                .addOnFailureListener { e ->
                                    Log.w("FriendRequest", "Failed to add current user as friend to sender", e)
                                }
                        }
                        .addOnFailureListener { e ->
                            Log.w("FriendRequest", "Failed to add sender as friend to current user", e)
                        }
                }
            },
            { friendRequest ->
                // Decline friend request logic
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null) {
                    db.collection("users")
                        .document(currentUser.uid)
                        .collection("friendRequests")
                        .whereEqualTo("senderId", friendRequest.senderId)
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            for (document in querySnapshot) {
                                db.collection("users")
                                    .document(currentUser.uid)
                                    .collection("friendRequests")
                                    .document(document.id)
                                    .delete()
                                    .addOnSuccessListener {
                                        Log.d("FriendRequest", "Friend request accepted and removed.")
                                        Toast.makeText(context, "Friend request declined", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                }
            }
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

        backButton = view.findViewById(R.id.back_button)
        searchView = view.findViewById(R.id.search_friend)

        addFriendRecyclerView = view.findViewById(R.id.add_friend_recycler)
        friendRequestRecyclerView = view.findViewById(R.id.friend_request_recycler)

        addedTitle = view.findViewById(R.id.added_me)
        addedUnderline = view.findViewById(R.id.added_me_underline)
        quickAddTitle = view.findViewById(R.id.quick_add)
        quickAddUnderline = view.findViewById(R.id.quick_add_underline)

        warningText = view.findViewById(R.id.warning_text)

        // Initialize RecyclerView and Adapter
        addFriendRecyclerView.adapter = friendAdapter
        addFriendRecyclerView.layoutManager = LinearLayoutManager(context)

        friendRequestRecyclerView.adapter = requestAdapter
        friendRequestRecyclerView.layoutManager = LinearLayoutManager(context)

        backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        setFontSize()

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchFriends(query)
                stopUpdatingTime()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    startUpdatingTime()
                } else {
                    stopUpdatingTime()
                }
                searchFriends(newText)
                return true
            }
        })

        searchView.setOnCloseListener {
            showRandomUsers()
            false
        }

        return view
    }

    override fun onResume() {
        super.onResume()

        if (searchView.query.isEmpty()) {
            showFriendRequests()
            startUpdatingTime()
        }
    }

    override fun onPause() {
        super.onPause()
        stopUpdatingTime()
    }

    private fun startUpdatingTime() {
        updateTimeRunnable = object : Runnable {
            override fun run() {
                showFriendRequests()
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(updateTimeRunnable)
    }

    private fun stopUpdatingTime() {
        handler.removeCallbacks(updateTimeRunnable)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showFriendRequests()
        showRandomUsers()
    }

    private fun showFriendRequests() {
        if (currentUser != null) {
            db.collection("users")
                .document(currentUser.uid)
                .collection("friendRequests")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val friendRequests = mutableListOf<FriendRequest>()

                    if (!querySnapshot.isEmpty) {
                        addedTitle.visibility = View.VISIBLE
                        addedUnderline.visibility = View.VISIBLE
                        friendRequestRecyclerView.visibility = View.VISIBLE

                        for (request in querySnapshot.documents) {
                            val avatarUrl = request.getString("avatarUrl")
                            val senderId = request.getString("senderId") ?: ""
                            val username = request.getString("username") ?: ""
                            val sentAt = request.getTimestamp("sentAt")

                            if (senderId.isNotEmpty()) {
                                db.collection("users").document(senderId).get().addOnSuccessListener { document ->
                                    val firstName = document.getString("firstName") ?: ""

                                    sentAt?.let {
                                        val friendRequest = FriendRequest(senderId, username, avatarUrl, firstName, it)
                                        friendRequests.add(friendRequest)

                                        if (friendRequests.size == querySnapshot.size()) {
                                            requestAdapter.setData(friendRequests)
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        addedTitle.visibility = View.GONE
                        addedUnderline.visibility = View.GONE
                        friendRequestRecyclerView.visibility = View.GONE
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w("ShowFriendRequests", "Error getting documents: ", exception)
                    Toast.makeText(context, "Failed to fetch friend requests", Toast.LENGTH_SHORT).show()
                }
        } else {
            findNavController().navigate(R.id.loginFragment)
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
        if (!TextUtils.isEmpty(query)) {
            val searchText = query!!.lowercase()

            friendRequestRecyclerView.visibility = View.GONE
            quickAddTitle.visibility = View.GONE
            quickAddUnderline.visibility = View.GONE
            addedTitle.visibility = View.GONE
            addedUnderline.visibility = View.GONE

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

                    if (filteredList.isEmpty()) {
                        warningText.visibility = View.VISIBLE
                        warningText.text = "No users found"
                        addFriendRecyclerView.visibility = View.GONE
                    } else {
                        addFriendRecyclerView.visibility = View.VISIBLE
                        warningText.visibility = View.GONE
                    }

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

                            if (filteredList.isEmpty()) {
                                warningText.visibility = View.VISIBLE
                                warningText.text = "No users found"
                                addFriendRecyclerView.visibility = View.GONE
                            } else {
                                warningText.visibility = View.GONE
                                quickAddTitle.visibility = View.VISIBLE
                                quickAddUnderline.visibility = View.VISIBLE
                                addFriendRecyclerView.visibility = View.VISIBLE

                                filteredList.shuffle()
                                Log.d("RandomUsers", "Filtered list size: ${filteredList.size}")

                                friendAdapter.setData(filteredList, friends)
                            }

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

    private fun setFontSize() {
        searchText = searchView.findViewById(androidx.appcompat.R.id.search_src_text)

        searchText.textSize = 14f
    }
}
