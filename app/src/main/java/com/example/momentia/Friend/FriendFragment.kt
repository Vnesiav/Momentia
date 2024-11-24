package com.example.momentia.Friend

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.momentia.Authentication.BaseAuthFragment
import com.example.momentia.Chat.ChatMessageActivity
import com.example.momentia.DTO.FriendChat
import com.example.momentia.R
import com.example.momentia.glide.GlideImageLoader
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore

class FriendFragment : BaseAuthFragment() {
    private val db = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private lateinit var addFriendButton: ImageButton
    private lateinit var profileButton: ImageButton
    private lateinit var searchView: SearchView

    private lateinit var friendChatRecyclerView: RecyclerView
    private val friendList = mutableListOf<FriendChat>()
    private val friendChatAdapter by lazy {
        FriendAdapter(layoutInflater, GlideImageLoader(requireContext()), object: FriendAdapter.OnClickListener {
            override fun onItemClick(friend: FriendChat) = navigateToChatMessage(friend)
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_friend, container, false)

        // Initialize views
        searchView = view.findViewById(R.id.search_friend)
        addFriendButton = view.findViewById(R.id.add_friend_button)
        profileButton = view.findViewById(R.id.profile_button)
        friendChatRecyclerView = view.findViewById(R.id.friend_chat_recycler)

        addFriendButton.setOnClickListener {
            navigateToAddFriend()
        }

        profileButton.setOnClickListener {
            navigateToProfile()
        }

        // Set RecyclerView adapter
        friendChatRecyclerView.adapter = friendChatAdapter
        friendChatRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        setFontSize(view)
        getFriendList(view)
        setupSearch()

        return view
    }

    private fun setupSearch() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchFriends(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { searchFriends(it) }
                return true
            }
        })
    }

    private fun searchFriends(query: String) {
        val warningText: TextView = requireView().findViewById(R.id.warning_text)
        val lowercaseQuery = query.lowercase()

        val filteredList = friendList.filter { friend ->
            friend.firstName.lowercase().contains(lowercaseQuery) ||
                    friend.lastName?.lowercase()?.contains(lowercaseQuery) == true
        }

        if (filteredList.isEmpty()) {
            warningText.visibility = View.VISIBLE
            warningText.text = "No result found"
        } else {
            warningText.visibility = View.GONE
        }

        friendChatAdapter.setData(filteredList)
    }

    private fun getFriendList(view: View) {
        if (currentUser == null) {
            findNavController().navigate(R.id.loginFragment)
            return
        }

        db.collection("users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val friendIds = documentSnapshot.get("friends") as? List<String> ?: emptyList()

                    val warningText = view.findViewById<TextView>(R.id.warning_text) // Pass view here
                    if (friendIds.isNotEmpty()) {
                        warningText.visibility = View.GONE
                        friendChatRecyclerView.visibility = View.VISIBLE

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

                                friendList.clear()
                                friendList.addAll(friends)
                                friendChatAdapter.setData(friends)
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(requireContext(), "Error getting friend list", Toast.LENGTH_SHORT).show()
                                Log.e("FriendFragment", "Error getting friend list: ", e)
                            }
                    } else {
                        warningText.visibility = View.VISIBLE
                        warningText.text = "No friends found"
                        friendChatRecyclerView.visibility = View.GONE
                        Log.d("FriendFragment", "No friends found.")
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error getting current user document", Toast.LENGTH_SHORT).show()
                Log.e("FriendFragment", "Error getting current user document: ", e)
            }
    }

    private fun navigateToProfile() {
        val navController = findNavController()

        val navOptions = NavOptions.Builder()
            .setEnterAnim(R.anim.slide_in_left)
            .setExitAnim(R.anim.slide_out_right)
            .setPopEnterAnim(R.anim.slide_in_right)
            .setPopExitAnim(R.anim.slide_out_left)
            .build()

        navController.navigate(R.id.profileFragment, null, navOptions)
    }

    private fun navigateToAddFriend() {
        val navController = findNavController()

        val navOptions = NavOptions.Builder()
            .setEnterAnim(R.anim.slide_in_right)
            .setExitAnim(R.anim.slide_out_left)
            .setPopEnterAnim(R.anim.slide_in_left)
            .setPopExitAnim(R.anim.slide_out_right)
            .build()

        navController.navigate(R.id.addFriendFragment, null, navOptions)
    }

    private fun navigateToChatMessage(friend: FriendChat) {
        val intent = Intent(requireContext(), ChatMessageActivity::class.java).apply {
            putExtra("userId", friend.userId) // Konsisten dengan "userId"
            putExtra("firstName", friend.firstName) // Konsisten dengan "firstName"
            putExtra("lastName", friend.lastName ?: "") // Konsisten dengan "lastName"
            putExtra("imageUrl", friend.avatarUrl) // Konsisten dengan "imageUrl"
        }
        startActivity(intent)
    }


    private fun setFontSize(view: View) {
        val searchView = view.findViewById<SearchView>(R.id.search_friend)
        val searchText = searchView.findViewById<TextView>(androidx.appcompat.R.id.search_src_text)
        searchText.textSize = 14f
    }
}