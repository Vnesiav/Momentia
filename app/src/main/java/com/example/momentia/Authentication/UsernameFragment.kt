package com.example.momentia.Authentication

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.momentia.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UsernameFragment : Fragment() {
    private lateinit var db: FirebaseFirestore
    private lateinit var usernameEditText: EditText
    private lateinit var email: String
    private lateinit var password: String
    private var lastClickTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = FirebaseFirestore.getInstance()
        email = arguments?.getString("email") ?: ""
        password = arguments?.getString("password") ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_username, container, false)

        usernameEditText = view.findViewById(R.id.username_input)
        val backButton: ImageButton = view.findViewById(R.id.back_button)
        val continueButton: View = view.findViewById(R.id.continue_button)

        backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        continueButton.setOnClickListener {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime < 3000) {
                return@setOnClickListener
            }
            lastClickTime = currentTime

            handleContinueButtonClick()
        }

        return view
    }

    private fun handleContinueButtonClick() {
        val username = usernameEditText.text.toString().trim()

        if (username.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter your username", Toast.LENGTH_SHORT).show()
            return
        }

        val usernamePattern = Regex("^[a-z0-9]+$")
        if (!username.matches(usernamePattern)) {
            Toast.makeText(requireContext(), "Username can only contain lowercase letters and numbers.", Toast.LENGTH_SHORT).show()
            return
        }

        checkIfUsernameExists(username) { usernameExists ->
            if (usernameExists) {
                Toast.makeText(requireContext(), "Username is already taken. Please choose another.", Toast.LENGTH_SHORT).show()
            } else {
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId != null) {
                    db.collection("users")
                        .document(userId)
                        .get()
                        .addOnSuccessListener { document ->
                            if (document.exists() && document.getString("username") != null) {
                                saveUsernameToFirestore(userId, username)
                            } else {
                                findNavController().navigate(R.id.action_usernameFragment_to_homeFragment)
                            }
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(requireContext(), "Error checking UID: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(requireContext(), "User ID not found. Please log in again.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun checkIfUsernameExists(username: String, callback: (Boolean) -> Unit) {
        db.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    callback(true)
                } else {
                    callback(false)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Error checking username: ${exception.message}", Toast.LENGTH_SHORT).show()
                callback(false)
            }
    }

    private fun saveUsernameToFirestore(userId: String, username: String) {
        val userData = mapOf("username" to username)
        db.collection("users").document(userId)
            .update(userData)
            .addOnSuccessListener {
                findNavController().navigate(R.id.action_usernameFragment_to_homeFragment)
                Log.d("UsernameFragment", "Username updated successfully.")
            }
            .addOnFailureListener { e ->
                Log.e("UsernameFragment", "Error updating user data: ${e.message}")
            }
    }
}
