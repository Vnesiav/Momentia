package com.example.momentia

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.example.momentia.Authentication.BaseAuthFragment
import com.example.momentia.Camera.CameraActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : BaseAuthFragment() {
    private lateinit var db: FirebaseFirestore
    private var currentUser: FirebaseUser? = null
    private lateinit var hello: TextView
    private lateinit var reminder: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        db = FirebaseFirestore.getInstance()

        hello = view.findViewById(R.id.hello)
        reminder = view.findViewById(R.id.reminder)

        currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let {
            db.collection("users").document(it.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val firstName = document.getString("firstName") ?: "User"
                        hello.text = Html.fromHtml("Hello, <b>$firstName!</b>", Html.FROM_HTML_MODE_LEGACY)
                    } else {
                        hello.text = Html.fromHtml("Hello, <b>User!</b>", Html.FROM_HTML_MODE_LEGACY)
                    }
                }
                .addOnFailureListener {
                    hello.text = Html.fromHtml("Hello, <b>User!</b>", Html.FROM_HTML_MODE_LEGACY)
                }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentUser = FirebaseAuth.getInstance().currentUser

        val cameraButton = view.findViewById<ImageView>(R.id.camera_button)
        cameraButton.setOnClickListener {
            cameraButton.animate().scaleX(1.1f).scaleY(1.1f).setDuration(100).withEndAction {
                cameraButton.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100)
            }
            val intent = Intent(requireContext(), CameraActivity::class.java)
            startActivity(intent)
        }

        val memoriesButton = view.findViewById<ImageView>(R.id.memories_button)
        memoriesButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_memoriesFragment)
        }
    }
}
