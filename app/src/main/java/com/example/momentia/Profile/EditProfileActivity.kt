package com.example.momentia.Profile

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.momentia.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.*

class EditProfileActivity : AppCompatActivity() {

    private lateinit var profileImageView: ImageView
    private val PICK_IMAGE_REQUEST = 1
    private val CAMERA_REQUEST_CODE = 100
    private var selectedImageUri: Uri? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var userId: String? = null
    private var capturedImage: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        profileImageView = findViewById(R.id.profile_image)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        userId = auth.currentUser?.uid

        fetchUserData()

        val takePhotoButton: ImageButton = findViewById(R.id.take_photo_button)
        takePhotoButton.setOnClickListener {
            Log.d("EditProfileActivity", "Take photo button clicked. User ID: $userId")
            openCamera()
        }

        val changePhotoButton: ImageButton = findViewById(R.id.change_photo_button)
        changePhotoButton.setOnClickListener {
            Log.d("EditProfileActivity", "Change photo button clicked. User ID: $userId")
            openGallery()
        }

        val deletePhotoButton: ImageButton = findViewById(R.id.delete_photo_button)
        deletePhotoButton.setOnClickListener {
            confirmDeletePhoto()
        }

        val backButton: ImageButton = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun openCamera() {
        if (checkCameraPermission()) {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
        } else {
            requestCameraPermission()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(this, "Camera permission is required to use the camera", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST_CODE -> {
                    capturedImage = data?.extras?.get("data") as? Bitmap
                    capturedImage?.let {
                        profileImageView.setImageBitmap(it)
                        uploadPhotoToStorage(it)
                    }
                }
                PICK_IMAGE_REQUEST -> {
                    selectedImageUri = data?.data
                    profileImageView.setImageURI(selectedImageUri)
                    selectedImageUri?.let { uri -> uploadPhotoToStorage(uri) }
                }
            }
        } else {
            Toast.makeText(this, "Image selection cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadPhotoToStorage(bitmap: Bitmap) {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val photoRef = storage.reference.child("profile_photos/${UUID.randomUUID()}.jpg")
        photoRef.putBytes(data)
            .addOnSuccessListener {
                photoRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    saveProfilePhotoToDatabase(downloadUrl.toString())
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to upload photo: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadPhotoToStorage(uri: Uri) {
        val photoRef = storage.reference.child("profile_photos/${UUID.randomUUID()}.jpg")
        photoRef.putFile(uri)
            .addOnSuccessListener {
                photoRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    saveProfilePhotoToDatabase(downloadUrl.toString())
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to upload photo: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchUserData() {
        val user = auth.currentUser

        if (user == null) {
            Log.w("EditProfileActivity", "User not logged in")
            Toast.makeText(this, "Please log in", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = user.uid
        Log.d("EditProfileActivity", "User ID: $userId")

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val avatarUrl = document.getString("avatarUrl") ?: ""

                    if (avatarUrl.isNotEmpty()) {
                        selectedImageUri = Uri.parse(avatarUrl)
                        Glide.with(this)
                            .load(selectedImageUri)
                            .placeholder(R.drawable.person)
                            .into(profileImageView)
                    }
                } else {
                    Toast.makeText(this, "Profile not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
    }

    private fun confirmDeletePhoto() {
        AlertDialog.Builder(this)
            .setTitle("Delete Profile Photo")
            .setMessage("Are you sure you want to delete your profile photo?")
            .setPositiveButton("Yes") { _, _ -> deleteProfilePhoto() }
            .setNegativeButton("No", null)
            .show()
    }

    private fun deleteProfilePhoto() {
        profileImageView.setImageResource(R.drawable.person)
        userId?.let { uid ->
            firestore.collection("users").document(uid)
                .update("avatarUrl", null)
                .addOnSuccessListener {
                    Toast.makeText(this, "Profile photo deleted", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to delete profile photo: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveProfilePhotoToDatabase(avatarUrl: String) {
        userId?.let { uid ->
            firestore.collection("users").document(uid)
                .update("avatarUrl", avatarUrl)
                .addOnSuccessListener {
                    Toast.makeText(this, "Profile photo updated successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to update profile photo: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
