package com.example.momentia.Profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.momentia.DTO.User
import com.example.momentia.R

class EditProfileFragment : Fragment() {

    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var profileImageView: ImageView
    private val PICK_IMAGE_REQUEST = 1
    private var selectedImageUri: Uri? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var userEmail: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomNavigation = requireActivity().findViewById(R.id.bottom_nav)
        hideBottomNavigation()

        profileImageView = view.findViewById(R.id.profile_image)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        userEmail = auth.currentUser?.email

        // Load current profile data
        loadUserProfile()

        val changePhotoButton: ImageButton = view.findViewById(R.id.change_photo_button)
        changePhotoButton.setOnClickListener {
            openGallery()
        }

        val deletePhotoButton: ImageButton = view.findViewById(R.id.delete_photo_button)
        deletePhotoButton.setOnClickListener {
            confirmDeletePhoto()
        }

        val backButton: ImageButton = view.findViewById(R.id.back_button)
        backButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun loadUserProfile() {
        userEmail?.let { email ->
            firestore.collection("users").document(email).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val user = document.toObject(User::class.java)
                        if (user != null && user.avatarUrl != null) {
                            // Load avatar URL into profile image view using Glide
                            selectedImageUri = Uri.parse(user.avatarUrl)
                            Glide.with(this)
                                .load(selectedImageUri)
                                .placeholder(R.drawable.person) // Default image
                                .into(profileImageView)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Failed to load profile: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            profileImageView.setImageURI(selectedImageUri)
            saveProfilePhotoToDatabase(selectedImageUri)
        } else {
            Toast.makeText(context, "Image selection cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun confirmDeletePhoto() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Profile Photo")
            .setMessage("Are you sure you want to delete your profile photo?")
            .setPositiveButton("Yes") { _, _ -> deleteProfilePhoto() }
            .setNegativeButton("No", null)
            .show()
    }

    private fun deleteProfilePhoto() {
        profileImageView.setImageResource(R.drawable.person) // Replace with default image
        userEmail?.let { email ->
            firestore.collection("users").document(email)
                .update("avatarUrl", null)
                .addOnSuccessListener {
                    Toast.makeText(context, "Profile photo deleted", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Failed to delete profile photo: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveProfilePhotoToDatabase(uri: Uri?) {
        userEmail?.let { email ->
            if (uri != null) {
                firestore.collection("users").document(email)
                    .update("avatarUrl", uri.toString())
                    .addOnSuccessListener {
                        Toast.makeText(context, "Profile photo saved", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Failed to save profile photo: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun hideBottomNavigation() {
        bottomNavigation.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Show bottom navigation when leaving this fragment
        bottomNavigation.visibility = View.VISIBLE
    }
}