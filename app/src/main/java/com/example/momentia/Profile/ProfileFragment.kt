package com.example.momentia.Profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.momentia.Authentication.BaseAuthFragment
import com.example.momentia.MainActivity
import com.example.momentia.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : BaseAuthFragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var profileNameView: TextView
    private lateinit var profileImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        profileNameView = view.findViewById(R.id.profile_name)
        profileImageView = view.findViewById(R.id.profile_image)
        (activity as MainActivity).hideBottomNavigation()


        val backButton = view.findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        loadUserData()

        view.findViewById<TextView>(R.id.edit_profile_picture).setOnClickListener {
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            startActivity(intent)
        }

        view.findViewById<TextView>(R.id.account_details).setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_accountDetailsFragment)
        }

        view.findViewById<TextView>(R.id.edit_name).setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editNameFragment)
        }

        view.findViewById<TextView>(R.id.change_username).setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_changeUsernameFragment)
        }

        view.findViewById<TextView>(R.id.change_password).setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_changeProfileFragment)
        }

        view.findViewById<TextView>(R.id.change_phone).setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_changeNumberFragment)
        }

        view.findViewById<TextView>(R.id.add_friends).setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_addFriendFragment)
        }

        view.findViewById<TextView>(R.id.my_friends).setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_friendFragment)
        }

        view.findViewById<TextView>(R.id.sign_out).setOnClickListener {
            auth.signOut()
            Toast.makeText(requireContext(), "Signed out successfully", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
        }

        view.findViewById<TextView>(R.id.delete_account).setOnClickListener {
            showDeleteAccountDialog()
        }

        return view
    }

    fun showDeleteAccountDialog() {
        val deleteAccountDialog = DeleteAccountDialogFragment()
        deleteAccountDialog.show(childFragmentManager, "DeleteAccountDialogFragment")
    }

    fun loadUserData() {
        val userId = auth.currentUser?.uid ?: return
        val userRef = db.collection("users").document(userId)

        userRef.get().addOnSuccessListener { document ->
            if (isAdded) {
                if (document != null) {
                    val firstName = document.getString("firstName") ?: ""
                    val lastName = document.getString("lastName") ?: ""
                    val avatarUrl = document.getString("avatarUrl") ?: ""
                    val fullName = "$firstName $lastName"

                    profileNameView.text = fullName

                    if (avatarUrl.isNotEmpty()) {
                        Glide.with(this@ProfileFragment)
                            .load(avatarUrl)
                            .placeholder(R.drawable.profile)
                            .error(R.drawable.profile)
                            .into(profileImageView)
                    } else {
                        profileImageView.setImageResource(R.drawable.profile)
                    }
                }
            }
        }.addOnFailureListener {
            if (isAdded) {
                Toast.makeText(requireContext(), "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
