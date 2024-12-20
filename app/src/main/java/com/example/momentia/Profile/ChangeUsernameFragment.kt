package com.example.momentia.Profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.momentia.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChangeUsernameFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var currentUsernameEditText: EditText
    private lateinit var newUsernameEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_change_username, container, false)

        currentUsernameEditText = view.findViewById(R.id.current_username)
        newUsernameEditText = view.findViewById(R.id.new_username)
        val backButton: ImageButton = view.findViewById(R.id.back_button)
        val saveButton: Button = view.findViewById(R.id.save_button)

        loadCurrentUsername()

        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        saveButton.setOnClickListener {
            handleSaveButtonClick()
        }

        return view
    }

    private fun loadCurrentUsername() {
        val currentUser = auth.currentUser
        currentUser?.let {
            firestore.collection("users").document(it.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.contains("username")) {
                        currentUsernameEditText.setText(document.getString("username"))
                    } else {
                        Toast.makeText(requireContext(), "Failed to load current username", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun handleSaveButtonClick() {
        val newUsername = newUsernameEditText.text.toString().trim()

        if (newUsername.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter your new username", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUser = auth.currentUser
        currentUser?.let {
            firestore.collection("users").document(it.uid)
                .update("username", newUsername)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Username updated successfully", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Failed to update username: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
