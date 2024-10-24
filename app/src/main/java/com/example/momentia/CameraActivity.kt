package com.example.momentia

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class CameraActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var capturedImageView: ImageView
    private lateinit var cameraButton: ImageButton
    private lateinit var retakeButton: ImageButton
    private lateinit var galleryButton: ImageButton
    private lateinit var closeButton: ImageButton
    private lateinit var saveButton: LinearLayout
    private lateinit var sendToFriendButton: LinearLayout
    private var capturedImage: Bitmap? = null
    private val CAMERA_PERMISSION_REQUEST_CODE = 101
    private val CAMERA_REQUEST_CODE = 100
    private val GALLERY_REQUEST_CODE = 102

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        capturedImageView = findViewById(R.id.capturedImageView)
        cameraButton = findViewById(R.id.cameraButton)
        retakeButton = findViewById(R.id.retakeButton)
        galleryButton = findViewById(R.id.galleryButton)
        saveButton = findViewById(R.id.saveButton)
        sendToFriendButton = findViewById(R.id.sendToFriendButton)
        closeButton = findViewById(R.id.closeButton)

        retakeButton.isEnabled = false

        cameraButton.setOnClickListener {
            Log.d("CameraActivity", "Camera button clicked")
            if (checkCameraPermission()) {
                openCamera()
            } else {
                requestCameraPermission()
            }
        }

        retakeButton.setOnClickListener {
            openCamera()
        }

        saveButton.setOnClickListener {
            capturedImage?.let {
                saveImageToGallery(it)
            }
        }

        sendToFriendButton.setOnClickListener {
            capturedImage?.let {
                sendImageToFriend(it)
            }
        }

        closeButton.setOnClickListener {
            closeCapturedImage()
        }

        galleryButton.setOnClickListener {
            openGallery()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST_CODE -> {
                    capturedImage = data?.extras?.get("data") as? Bitmap
                    if (capturedImage != null) {
                        capturedImageView.setImageBitmap(capturedImage)
                        retakeButton.isEnabled = true

                        cameraButton.visibility = View.GONE
                        galleryButton.visibility = View.GONE
                        retakeButton.visibility = View.GONE

                        // Show new buttons
                        saveButton.visibility = View.VISIBLE
                        sendToFriendButton.visibility = View.VISIBLE
                        closeButton.visibility = View.VISIBLE
                    } else {
                        Toast.makeText(this, "Gagal mengambil gambar", Toast.LENGTH_SHORT).show()
                    }
                }
                GALLERY_REQUEST_CODE -> {
                    val imageUri: Uri? = data?.data
                    if (imageUri != null) {
                        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                        capturedImage = bitmap
                        capturedImageView.setImageBitmap(bitmap)

                        cameraButton.visibility = View.GONE
                        galleryButton.visibility = View.GONE
                        retakeButton.visibility = View.GONE

                        // Show new buttons
                        saveButton.visibility = View.VISIBLE
                        sendToFriendButton.visibility = View.VISIBLE
                        closeButton.visibility = View.VISIBLE
                    } else {
                        Toast.makeText(this, "Gagal memilih gambar", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun checkCameraPermission(): Boolean {
        val permissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        Log.d("CameraActivity", "Camera permission granted: $permissionGranted")
        return permissionGranted
    }

    private fun requestCameraPermission() {
        Log.d("CameraActivity", "Requesting camera permission")
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Log.d("CameraActivity", "Camera permission granted by user")
                openCamera()
            } else {
                Log.d("CameraActivity", "Camera permission denied by user")
                Toast.makeText(this, "Camera permission is required to use the camera", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openCamera() {
        Log.d("CameraActivity", "Opening camera")
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (cameraIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
            Log.d("CameraActivity", "Camera intent started")
        } else {
            Log.d("CameraActivity", "No camera app available")
            Toast.makeText(this, "Tidak ada aplikasi kamera yang tersedia", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadAbsensi(bitmap: Bitmap) {
        val timestamp = System.currentTimeMillis()
        val fileName = "story_${timestamp}.jpg"
        val storageRef = storage.reference.child("story/$fileName")

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        storageRef.putBytes(data).addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                val photoUrl = uri.toString()
                Log.d("CameraActivity", "URL Foto: $photoUrl")

                val attendanceRecordId = firestore.collection("story").document().id

                val storyData = mapOf(
                    "photoUrl" to photoUrl,
                    "timestamp" to timestamp
                )

                firestore.collection("absensi").document(attendanceRecordId)
                    .set(storyData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Absensi berhasil disimpan", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Gagal menyimpan data absensi: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Gagal mengunggah foto", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveImageToGallery(bitmap: Bitmap) {
        val savedImageURL = MediaStore.Images.Media.insertImage(
            contentResolver,
            bitmap,
            "Momentia_Image_${System.currentTimeMillis()}",
            "Image captured using Momentia App"
        )

        if (savedImageURL != null) {
            Toast.makeText(this, "Image saved to gallery", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendImageToFriend(bitmap: Bitmap) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "image/jpeg"

        // Save bitmap to a temporary file to be shared
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(
            contentResolver, bitmap, "temp", null
        )

        val uri = Uri.parse(path)
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
        startActivity(Intent.createChooser(shareIntent, "Send Image"))
    }

    private fun closeCapturedImage() {
        capturedImageView.setImageBitmap(null)
        capturedImage = null

        cameraButton.visibility = View.VISIBLE
        galleryButton.visibility = View.VISIBLE
        retakeButton.visibility = View.VISIBLE

        saveButton.visibility = View.GONE
        sendToFriendButton.visibility = View.GONE
        closeButton.visibility = View.GONE
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE)
    }
}