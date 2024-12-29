package com.example.momentia.Camera

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.momentia.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CameraActivity : AppCompatActivity() {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var capturedImageView: ImageView
    private lateinit var cameraText: TextView
    private lateinit var cameraButton: ImageButton
    private lateinit var closeButton: ImageButton
    private lateinit var backButton: ImageButton
    private lateinit var saveButton: ImageButton
    private lateinit var sendToFriendButton: TextView
    private var capturedImage: Bitmap? = null
    private val CAMERA_PERMISSION_REQUEST_CODE = 101
    private val CAMERA_REQUEST_CODE = 100
    private val GALLERY_REQUEST_CODE = 102
    private lateinit var currentPhotoPath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        capturedImageView = findViewById(R.id.capturedImageView)
        cameraText = findViewById(R.id.cameraText)
        cameraButton = findViewById(R.id.cameraButton)
        saveButton = findViewById(R.id.saveButton)
        sendToFriendButton = findViewById(R.id.sendToFriendButton)
        closeButton = findViewById(R.id.closeButton)
        backButton = findViewById(R.id.backButton)

        toggleButtons(false)

        cameraButton.setOnClickListener {
            if (checkCameraPermission()) {
                openCamera()
            } else {
                requestCameraPermission()
            }
        }

        saveButton.setOnClickListener {
            saveImageToGallery(capturedImage!!)
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
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST_CODE -> {
                    val file = File(currentPhotoPath)
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, Uri.fromFile(file))
                    capturedImage = bitmap
                    capturedImageView.setImageBitmap(bitmap)
                    toggleButtons(true)
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
            backButton.visibility = View.GONE
            closeButton.visibility = View.VISIBLE

            saveButton.visibility = View.VISIBLE
            sendToFriendButton.visibility = View.VISIBLE

            cameraText.visibility = View.GONE
            cameraButton.visibility = View.GONE
        } else {
            backButton.visibility = View.VISIBLE
            closeButton.visibility = View.GONE

            saveButton.visibility = View.GONE
            sendToFriendButton.visibility = View.GONE

            cameraText.visibility = View.VISIBLE
            cameraButton.visibility = View.VISIBLE
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
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                Toast.makeText(this, "Error occurred while creating the file", Toast.LENGTH_SHORT).show()
                null
            }
            photoFile?.also {
                val photoURI: Uri = FileProvider.getUriForFile(
                    this,
                    "com.example.momentia.fileprovider",
                    it
                )
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
            }
        } else {
            Toast.makeText(this, "No camera app available", Toast.LENGTH_SHORT).show()
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File = getExternalFilesDir(null)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
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
        val imageUri = saveImageToExternalStorage(bitmap)

        if (imageUri != null) {
            val intent = Intent(this, SendPhotoActivity::class.java)
            intent.putExtra("capturedImageUri", imageUri.toString())
            startActivity(intent)
        }
    }

    private fun saveImageToExternalStorage(bitmap: Bitmap): Uri? {
        val imageCollection = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "Momentia_Image_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }

        val imageUri: Uri? = contentResolver.insert(imageCollection, contentValues)
        imageUri?.let { uri ->
            contentResolver.openOutputStream(uri).use { outputStream ->
                if (outputStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                }
            }
        }
        return imageUri
    }

    private fun closeCapturedImage() {
        capturedImageView.setImageBitmap(null)
        capturedImage = null
        toggleButtons(false)
    }
}