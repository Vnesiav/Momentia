package com.example.momentia.Memories

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.momentia.R
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Environment
import android.provider.MediaStore
import android.widget.ImageButton
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import java.io.OutputStream
import java.io.IOException

class PhotoActivity : AppCompatActivity() {
    private lateinit var photoImageView: ImageView
    private lateinit var backButton: ImageButton
    private lateinit var saveButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo)

        photoImageView = findViewById(R.id.photoImageView)
        backButton = findViewById(R.id.back_button)
        saveButton = findViewById(R.id.save_button)

        val imageUrl = intent.getStringExtra("IMAGE_URL")

        if (imageUrl != null) {
            Glide.with(this)
                .load(imageUrl)
                .into(photoImageView)
        }

        backButton.setOnClickListener {
            finish()
        }

        saveButton.setOnClickListener {
            if (imageUrl != null) {
                saveImage(imageUrl)
            }
        }
    }

    private fun saveImage(imageUrl: String) {
        Glide.with(this)
            .asBitmap()
            .load(imageUrl)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    val saved = saveBitmapToGallery(resource)
                    if (saved) {
                        Toast.makeText(this@PhotoActivity, "Image Saved!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@PhotoActivity, "Failed to Save Image", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }

    private fun saveBitmapToGallery(bitmap: Bitmap): Boolean {
        return try {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "Momentia_${System.currentTimeMillis()}.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }

            val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            val outputStream: OutputStream? = uri?.let { contentResolver.openOutputStream(it) }

            outputStream?.use { bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it) }
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }
}
