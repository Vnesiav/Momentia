package com.example.momentia.Authentication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.momentia.R
import com.google.firebase.firestore.FirebaseFirestore

class UsernameFragment : Fragment() {
    private lateinit var usernameEditText: EditText
    private lateinit var email: String
    private lateinit var password: String
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        email = arguments?.getString("email") ?: ""
        password = arguments?.getString("password") ?: ""

        firestore = FirebaseFirestore.getInstance()
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
            findNavController().navigate(R.id.action_usernameFragment_to_passwordFragment)
        }

        continueButton.setOnClickListener {
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

        checkIfUsernameExists(username) { usernameExists ->
            if (usernameExists) {
                Toast.makeText(requireContext(), "Username is already taken. Please choose another.", Toast.LENGTH_SHORT).show()
            } else {
                val bundle = Bundle().apply {
                    putString("email", email)
                    putString("password", password)
                    putString("username", username)
                }
                findNavController().navigate(R.id.action_usernameFragment_to_nameFragment, bundle)
            }
        }
    }

    private fun checkIfUsernameExists(username: String, callback: (Boolean) -> Unit) {
        firestore.collection("users")
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
}
