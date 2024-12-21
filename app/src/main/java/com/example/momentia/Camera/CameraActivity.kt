package com.example.momentia.Camera

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.momentia.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import androidx.exifinterface.media.ExifInterface
import android.util.Log
import java.io.IOException

class CameraActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var capturedImageView: ImageView
    private lateinit var cameraButton: ImageButton
    private lateinit var retakeButton: ImageButton
    private lateinit var galleryButton: ImageButton
    private lateinit var closeButton: ImageButton
    private lateinit var backButton: ImageButton
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
        backButton = findViewById(R.id.backButton)

        retakeButton.isEnabled = false

        // Initialize buttons state
        toggleButtons(false)

        // Set up button click listeners
        cameraButton.setOnClickListener {
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
            val intent = Intent(this, SendPhotoActivity::class.java)
            startActivity(intent)
        }

        sendToFriendButton.setOnClickListener {
            capturedImage?.let {
                sendImageToFriend(it)
            }
        }

        closeButton.setOnClickListener {
            closeCapturedImage()
        }

        backButton.setOnClickListener {
            finish() // Navigate back to the home fragment
        }

        galleryButton.setOnClickListener {
            openGallery()
        }
    }

    private fun getLocationFromImage(imageUri: Uri): Pair<Double, Double>? {
        try {
            val exif = ExifInterface(contentResolver.openInputStream(imageUri)!!)
            val latDegrees = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE)
            val latRef = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF)
            val lonDegrees = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE)
            val lonRef = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF)

            if (latDegrees != null && latRef != null && lonDegrees != null && lonRef != null) {
                val latitude = convertToDegrees(latDegrees, latRef)
                val longitude = convertToDegrees(lonDegrees, lonRef)
                return Pair(latitude, longitude)
            }
        } catch (e: IOException) {
            Log.e("CameraActivity", "Failed to read EXIF data", e)
        }
        return null
    }

    private fun convertToDegrees(coord: String, ref: String): Double {
        val dms = coord.split(",")
        val degrees = dms[0].toDouble()
        val minutes = dms[1].toDouble() / 60
        val seconds = dms[2].toDouble() / 3600
        var result = degrees + minutes + seconds
        if (ref == "S" || ref == "W") {
            result = -result
        }
        return result
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST_CODE -> {
                    // Periksa apakah data berisi gambar yang diambil dengan kamera
                    val imageBitmap = data?.extras?.get("data") as? Bitmap
                    if (imageBitmap != null) {
                        capturedImage = imageBitmap
                        capturedImageView.setImageBitmap(capturedImage)

                        // Ambil lokasi dari gambar
                        val imageUri = data.data
                        val location = getLocationFromImage(imageUri ?: Uri.EMPTY)
                        if (location != null) {
                            val (latitude, longitude) = location
                            Toast.makeText(this, "Location: Lat = $latitude, Lon = $longitude", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this, "No location data available", Toast.LENGTH_SHORT).show()
                        }

                        retakeButton.isEnabled = true
                        toggleButtons(true)
                    } else {
                        Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show()
                    }
                }

                GALLERY_REQUEST_CODE -> {
                    val imageUri: Uri? = data?.data
                    if (imageUri != null) {
                        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                        capturedImage = bitmap
                        capturedImageView.setImageBitmap(bitmap)
                        toggleButtons(true)
                    } else {
                        Toast.makeText(this, "Failed to select image", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }


    private fun toggleButtons(isImageCaptured: Boolean) {
        if (isImageCaptured) {
            // Show close button, hide back button
            backButton.visibility = View.GONE
            closeButton.visibility = View.VISIBLE

            // Show save and send buttons
            saveButton.visibility = View.VISIBLE
            sendToFriendButton.visibility = View.VISIBLE

            // Hide camera and gallery buttons
            cameraButton.visibility = View.GONE
            galleryButton.visibility = View.GONE
            retakeButton.visibility = View.GONE
        } else {
            // Show back button, hide close button
            backButton.visibility = View.VISIBLE
            closeButton.visibility = View.GONE

            // Hide save and send buttons
            saveButton.visibility = View.GONE
            sendToFriendButton.visibility = View.GONE

            // Show camera and gallery buttons
            cameraButton.visibility = View.VISIBLE
            galleryButton.visibility = View.VISIBLE
            retakeButton.visibility = View.VISIBLE
        }
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
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
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(this, "Camera permission is required to use the camera", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (cameraIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
        } else {
            Toast.makeText(this, "No camera app available", Toast.LENGTH_SHORT).show()
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
        val intent = Intent(this, SendPhotoActivity::class.java)

        // Konversi Bitmap menjadi ByteArray
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val byteArray = bytes.toByteArray()

        // Tambahkan gambar sebagai extra
        intent.putExtra("capturedImage", byteArray)

        // Mulai activity
        startActivity(intent)
    }

    private fun closeCapturedImage() {
        capturedImageView.setImageBitmap(null)
        capturedImage = null
        toggleButtons(false)
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE)
    }
}