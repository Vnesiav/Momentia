package com.example.momentia

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
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileFragment : Fragment() {

    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var profileImageView: ImageView
    private val PICK_IMAGE_REQUEST = 1
    private var selectedImageUri: Uri? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

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

        // Button untuk mengambil foto
        val changePhotoButton: ImageButton = view.findViewById(R.id.change_photo_button)
        changePhotoButton.setOnClickListener {
            openGallery()
        }

        // Button untuk menghapus foto
        val deletePhotoButton: ImageButton = view.findViewById(R.id.delete_photo_button)
        deletePhotoButton.setOnClickListener {
            deleteProfilePhoto()
        }

        val backButton: ImageButton = view.findViewById(R.id.back_button)
        backButton.setOnClickListener {
            findNavController().navigate(R.id.action_editProfileFragment_to_friendFragment)
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == AppCompatActivity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            profileImageView.setImageURI(selectedImageUri)
            // Simpan URI foto di database di sini
            saveProfilePhotoToDatabase(selectedImageUri)
        }
    }

    private fun deleteProfilePhoto() {
        // Logika untuk menghapus foto dari database
        profileImageView.setImageResource(R.drawable.person) // Ganti dengan gambar default
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId)
                .update("profilePhotoUri", null)
                .addOnSuccessListener {
                    Toast.makeText(context, "Profile photo deleted", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Failed to delete profile photo: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveProfilePhotoToDatabase(uri: Uri?) {
        val userId = auth.currentUser?.uid
        if (userId != null && uri != null) {
            firestore.collection("users").document(userId)
                .update("profilePhotoUri", uri.toString())
                .addOnSuccessListener {
                    Toast.makeText(context, "Profile photo saved", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Failed to save profile photo: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun hideBottomNavigation() {
        bottomNavigation.visibility = View.GONE
    }
}