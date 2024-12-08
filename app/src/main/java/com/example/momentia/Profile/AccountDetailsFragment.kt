package com.example.momentia.Profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.momentia.Authentication.BaseAuthFragment
import com.example.momentia.DTO.User
import com.example.momentia.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AccountDetailsFragment : BaseAuthFragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var firstNameEditText: TextInputEditText
    private lateinit var lastNameEditText: TextInputEditText
    private lateinit var usernameEditText: TextInputEditText
    private lateinit var phoneNumberEditText: TextInputEditText
    private lateinit var emailEditText: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_account_details, container, false)

        firstNameEditText = view.findViewById(R.id.first_name)
        lastNameEditText = view.findViewById(R.id.last_name)
        usernameEditText = view.findViewById(R.id.username)
        phoneNumberEditText = view.findViewById(R.id.phone_number)
        emailEditText = view.findViewById(R.id.email)

        firstNameEditText.isEnabled = false
        lastNameEditText.isEnabled = false
        usernameEditText.isEnabled = false
        phoneNumberEditText.isEnabled = false
        emailEditText.isEnabled = false

        val backButton: ImageButton = view.findViewById(R.id.back_button)

        backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        loadUserData()

        return view
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val user = document.toObject(User::class.java)
                    user?.let {
                        firstNameEditText.setText(it.firstName)
                        lastNameEditText.setText(it.lastName)
                        usernameEditText.setText(it.username)
                        phoneNumberEditText.setText(it.phoneNumber)
                        emailEditText.setText(it.email)
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error loading user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
