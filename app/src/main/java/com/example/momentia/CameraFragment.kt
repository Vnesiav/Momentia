package com.example.momentia

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class CameraFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_camera, container, false)
        val view = inflater.inflate(R.layout.fragment_camera, container, false)

        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        capturedImageView = view.findViewById(R.id.capturedImageView)
        cameraButton = view.findViewById(R.id.cameraButton)
        retakeButton = view.findViewById(R.id.retakeButton)
        galleryButton = view.findViewById(R.id.galleryButton)
        saveButton = view.findViewById(R.id.saveButton)
        sendToFriendButton = view.findViewById(R.id.sendToFriendButton)
        closeButton = view.findViewById(R.id.closeButton)

        retakeButton.isEnabled = false

        cameraButton.setOnClickListener {
            Log.d("CameraFragment", "Camera button clicked")
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

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Additional setup if needed
    }

//    override fun onResume() {
//        super.onResume()
////        (activity as MainActivity).hideBottomNavigation()
//    }
//
//    override fun onPause() {
//        super.onPause()
//        (activity as MainActivity).showBottomNavigation()
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AppCompatActivity.RESULT_OK) {
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
                        Toast.makeText(requireContext(), "Gagal mengambil gambar", Toast.LENGTH_SHORT).show()
                    }
                }
                GALLERY_REQUEST_CODE -> {
                    val imageUri: Uri? = data?.data
                    if (imageUri != null) {
                        val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, imageUri)
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
                        Toast.makeText(requireContext(), "Gagal memilih gambar", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    companion object {

    }
}