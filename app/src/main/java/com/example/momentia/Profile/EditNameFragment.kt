package com.example.momentia.Profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.momentia.Authentication.BaseAuthFragment
import com.example.momentia.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditNameFragment : BaseAuthFragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firstNameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_name, container, false)

        firstNameEditText = view.findViewById(R.id.first_name)
        lastNameEditText = view.findViewById(R.id.last_name)
        val backButton: ImageButton = view.findViewById(R.id.back_button)
        val saveButton: View = view.findViewById(R.id.save_button)

        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        saveButton.setOnClickListener {
            handleSaveButtonClick()
        }

        return view
    }

    private fun handleSaveButtonClick() {
        val firstName = firstNameEditText.text.toString().trim()
        val lastName = lastNameEditText.text.toString().trim()

        if (firstName.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter your first name", Toast.LENGTH_SHORT)
                .show()
            return
        }

        if (lastName.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter your last name", Toast.LENGTH_SHORT)
                .show()
            return
        }

        val user = auth.currentUser
        user?.let {
            val userId = it.uid
            val userRef = firestore.collection("users").document(userId)
            userRef.update("firstName", firstName, "lastName", lastName)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Name updated successfully", Toast.LENGTH_SHORT)
                        .show()
                    findNavController().navigateUp()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Failed to update name: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
        }
    }
}
