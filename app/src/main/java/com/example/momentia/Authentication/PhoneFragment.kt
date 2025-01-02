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
import com.example.momentia.DTO.User
import com.example.momentia.R
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class PhoneFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var email: String
    private lateinit var password: String
    private lateinit var username: String
    private lateinit var firstName: String
    private lateinit var lastName: String
    private lateinit var phoneNumberEditText: EditText
    private var lastClickTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        email = arguments?.getString("email") ?: ""
        password = arguments?.getString("password") ?: ""
        username = arguments?.getString("username") ?: ""
        firstName = arguments?.getString("firstName") ?: ""
        lastName = arguments?.getString("lastName") ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_phone, container, false)

        phoneNumberEditText = view.findViewById(R.id.phone_number)
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
        val phoneNumber = phoneNumberEditText.text.toString().trim()

        if (phoneNumber.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter your phone number", Toast.LENGTH_SHORT).show()
            return
        }

        val phonePattern = Regex("^08[0-9]{8,12}$")
        if (!phoneNumber.matches(phonePattern)) {
            Toast.makeText(requireContext(), "Invalid phone number. Please enter a valid phone number.", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val currentUser = auth.currentUser
                    if (currentUser != null && !currentUser.isEmailVerified) {
                        saveUserToFirestore(currentUser, phoneNumber)
                        sendEmailVerification(currentUser)
                    } else {
                        Toast.makeText(requireContext(), "User already verified or not registered!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorMessage = task.exception?.message
                    if (errorMessage?.contains("email address is already in use") == true) {
                        Toast.makeText(requireContext(), "This email is in use. Please verify your email or log in.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Registration Failed: $errorMessage", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun saveUserToFirestore(user: FirebaseUser, phoneNumber: String) {
        val currentUserId = user.uid
        val userData = User(
            firstName = firstName,
            lastName = lastName,
            email = email,
            username = username,
            phoneNumber = phoneNumber,
            avatarUrl = null,
            friends = emptyList(),
            createdAt = Timestamp.now()
        )

        db.collection("users")
            .document(currentUserId)
            .set(userData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Your information has been saved successfully.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to save your information, please try again later.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendEmailVerification(user: FirebaseUser) {
        user.sendEmailVerification()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Verification email sent to ${user.email}", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_phoneFragment_to_loginFragment)
                } else {
                    Toast.makeText(requireContext(), "Failed to send verification email.", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
