//package com.example.momentia.Profile
//
//import android.Manifest
//import android.app.Activity
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.graphics.Bitmap
//import android.net.Uri
//import android.os.Bundle
//import android.provider.MediaStore
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageButton
//import android.widget.ImageView
//import android.widget.Toast
//import androidx.appcompat.app.AlertDialog
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import androidx.fragment.app.Fragment
//import androidx.navigation.fragment.findNavController
//import com.bumptech.glide.Glide
//import com.google.android.material.bottomnavigation.BottomNavigationView
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//import com.google.firebase.storage.FirebaseStorage
//import com.example.momentia.R
//import java.io.ByteArrayOutputStream
//import java.util.*
//
//class EditProfileFragment : Fragment() {
//
//    private lateinit var bottomNavigation: BottomNavigationView
//    private lateinit var profileImageView: ImageView
//    private val PICK_IMAGE_REQUEST = 1
//    private val CAMERA_REQUEST_CODE = 100
//    private var selectedImageUri: Uri? = null
//    private lateinit var auth: FirebaseAuth
//    private lateinit var firestore: FirebaseFirestore
//    private lateinit var storage: FirebaseStorage
//    private var userId: String? = null
//    private var capturedImage: Bitmap? = null
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        bottomNavigation = requireActivity().findViewById(R.id.bottom_nav)
//        hideBottomNavigation()
//
//        profileImageView = view.findViewById(R.id.profile_image)
//        auth = FirebaseAuth.getInstance()
//        firestore = FirebaseFirestore.getInstance()
//        storage = FirebaseStorage.getInstance()
//
//        userId = auth.currentUser?.uid
//
//        // Load current profile data using fetchUserData()
//        fetchUserData()
//
//        val takePhotoButton: ImageButton = view.findViewById(R.id.take_photo_button)
//        takePhotoButton.setOnClickListener {
//            Log.d("EditProfileFragment", "Take photo button clicked. User ID: $userId")
//            openCamera() // Directly open the camera
//        }
//
//        val changePhotoButton: ImageButton = view.findViewById(R.id.change_photo_button)
//        changePhotoButton.setOnClickListener {
//            Log.d("EditProfileFragment", "Change photo button clicked. User ID: $userId")
//            openGallery() // Directly open the gallery
//        }
//
//        val deletePhotoButton: ImageButton = view.findViewById(R.id.delete_photo_button)
//        deletePhotoButton.setOnClickListener {
//            confirmDeletePhoto()
//        }
//
//        val backButton: ImageButton = view.findViewById(R.id.back_button)
//        backButton.setOnClickListener {
//            findNavController().popBackStack()
//        }
//    }
//
//    private fun openCamera() {
//        if (checkCameraPermission()) {
//            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
//        } else {
//            requestCameraPermission()
//        }
//    }
//
//    private fun openGallery() {
//        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//        startActivityForResult(intent, PICK_IMAGE_REQUEST)
//    }
//
//    private fun checkCameraPermission(): Boolean {
//        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
//    }
//
//    private fun requestCameraPermission() {
//        ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
//    }
//
//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == CAMERA_REQUEST_CODE) {
//            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                openCamera()
//            } else {
//                Toast.makeText(requireContext(), "Camera permission is required to use the camera", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if (resultCode == Activity.RESULT_OK) {
//            when (requestCode) {
//                CAMERA_REQUEST_CODE -> {
//                    capturedImage = data?.extras?.get("data") as? Bitmap
//                    capturedImage?.let {
//                        profileImageView.setImageBitmap(it)
//                        uploadPhotoToStorage(it)
//                    }
//                }
//                PICK_IMAGE_REQUEST -> {
//                    selectedImageUri = data?.data
//                    profileImageView.setImageURI(selectedImageUri)
//                    selectedImageUri?.let { uri -> uploadPhotoToStorage(uri) }
//                }
//            }
//        } else {
//            Toast.makeText(context, "Image selection cancelled", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun uploadPhotoToStorage(bitmap: Bitmap) {
//        val baos = ByteArrayOutputStream()
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
//        val data = baos.toByteArray()
//
//        val photoRef = storage.reference.child("profile_photos/${UUID.randomUUID()}.jpg")
//        photoRef.putBytes(data)
//            .addOnSuccessListener {
//                photoRef.downloadUrl.addOnSuccessListener { downloadUrl ->
//                    saveProfilePhotoToDatabase(downloadUrl.toString())
//                }
//            }
//            .addOnFailureListener { e ->
//                Toast.makeText(context, "Failed to upload photo: ${e.message}", Toast.LENGTH_SHORT).show()
//            }
//    }
//
//    private fun uploadPhotoToStorage(uri: Uri) {
//        val photoRef = storage.reference.child("profile_photos/${UUID.randomUUID()}.jpg")
//        photoRef.putFile(uri)
//            .addOnSuccessListener {
//                photoRef.downloadUrl.addOnSuccessListener { downloadUrl ->
//                    saveProfilePhotoToDatabase(downloadUrl.toString())
//                }
//            }
//            .addOnFailureListener { e ->
//                Toast.makeText(context, "Failed to upload photo: ${e.message}", Toast.LENGTH_SHORT).show()
//            }
//    }
//
//    private fun fetchUserData() {
//        val user = auth.currentUser
//
//        if (user == null) {
//            Log.w("EditProfileFragment", "User not logged in")
//            Toast.makeText(requireContext(), "Please log in", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        val userId = user.uid
//        Log.d("EditProfileFragment", "User ID: $userId")
//
//        firestore.collection("users").document(userId).get()
//            .addOnSuccessListener { document ->
//                if (document != null && document.exists()) {
//                    Log.d("EditProfileFragment", "Document data: ${document.data}")
//                    val avatarUrl = document.getString("avatarUrl") ?: ""
//
//                    if (avatarUrl.isNotEmpty()) {
//                        selectedImageUri = Uri.parse(avatarUrl)
//                        Glide.with(this)
//                            .load(selectedImageUri)
//                            .placeholder(R.drawable.person)
//                            .into(profileImageView)
//                    }
//                    Log.d("EditProfileFragment", "User data loaded successfully")
//                } else {
//                    Log.d("EditProfileFragment", "No user profile found for user ID: $userId")
//                    Toast.makeText(requireContext(), "Profile not found", Toast.LENGTH_SHORT).show()
//                }
//            }
//            .addOnFailureListener { e ->
//                Log.e("EditProfileFragment", "Error loading user data", e)
//                Toast.makeText(requireContext(), "Failed to load profile", Toast.LENGTH_SHORT).show()
//            }
//    }
//
//    private fun confirmDeletePhoto() {
//        AlertDialog.Builder(requireContext())
//            .setTitle("Delete Profile Photo")
//            .setMessage("Are you sure you want to delete your profile photo?")
//            .setPositiveButton("Yes") { _, _ -> deleteProfilePhoto() }
//            .setNegativeButton("No", null)
//            .show()
//    }
//
//    private fun deleteProfilePhoto() {
//        profileImageView.setImageResource(R.drawable.person) // Replace with default image
//        userId?.let { uid ->
//            firestore.collection("users").document(uid)
//                .update("avatarUrl", null)
//                .addOnSuccessListener {
//                    Toast.makeText(context, "Profile photo deleted", Toast.LENGTH_SHORT).show()
//                }
//                .addOnFailureListener { e ->
//                    Toast.makeText(context, "Failed to delete profile photo: ${e.message}", Toast.LENGTH_SHORT).show()
//                }
//        }
//    }
//
//    private fun saveProfilePhotoToDatabase(avatarUrl: String) {
//        userId?.let { uid ->
//            firestore.collection("users").document(uid)
//                .update("avatarUrl", avatarUrl)
//                .addOnSuccessListener {
//                    Toast.makeText(context, "Profile photo updated successfully", Toast.LENGTH_SHORT).show()
//                }
//                .addOnFailureListener { e ->
//                    Toast.makeText(context, "Failed to update profile photo: ${e.message}", Toast.LENGTH_SHORT).show()
//                }
//        }
//    }
//
//    private fun hideBottomNavigation() {
//        bottomNavigation.visibility = View.GONE
//    }
//}
