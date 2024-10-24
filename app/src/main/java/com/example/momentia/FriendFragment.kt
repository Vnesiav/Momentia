package com.example.momentia

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.navigation.fragment.findNavController
import com.example.momentia.Authentication.BaseAuthFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FriendFragment : BaseAuthFragment() {
    private val db = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private lateinit var addFriendButton: ImageButton
    private lateinit var profileButton: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout
        val view = inflater.inflate(R.layout.fragment_friend, container, false)

        addFriendButton = view.findViewById(R.id.add_friend_button)
        profileButton = view.findViewById(R.id.profile_button)

        addFriendButton.setOnClickListener {
            navigateToAddFriend()
        }

        profileButton.setOnClickListener {
            navigateToProfile()
        }

        setFontSize(view)

        return view
    }

    private fun navigateToProfile() {
        val navController = findNavController()
        navController.navigate(R.id.profileFragment)
    }

    private fun navigateToAddFriend() {
        val navController = findNavController()
        navController.navigate(R.id.addFriendFragment)
    }

    private fun setFontSize(view: View) {
        val searchView = view.findViewById<SearchView>(R.id.search_friend)
        val searchText = searchView.findViewById<TextView>(androidx.appcompat.R.id.search_src_text)
        searchText.textSize = 14f
    }
}
