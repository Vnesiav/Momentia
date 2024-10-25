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
import com.example.momentia.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.EmailAuthProvider

class ChangePasswordFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var currentPasswordEditText: EditText
    private lateinit var newPasswordEditText: EditText
    private var currentUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_change_password, container, false)

        currentPasswordEditText = view.findViewById(R.id.current_password)
        newPasswordEditText = view.findViewById(R.id.new_password)
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
        val currentPassword = currentPasswordEditText.text.toString().trim()
        val newPassword = newPasswordEditText.text.toString().trim()

        if (currentPassword.isEmpty() || newPassword.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in both password fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isPasswordValid(newPassword)) {
            Toast.makeText(requireContext(), "New password must be at least 8 characters and include letters and numbers", Toast.LENGTH_SHORT).show()
            return
        }

        currentUser?.let { user ->
            val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)

            // Autentikasi ulang dengan kredensial
            user.reauthenticate(credential).addOnCompleteListener { authTask ->
                if (authTask.isSuccessful) {
                    // Autentikasi ulang berhasil, lanjutkan dengan pembaruan password
                    user.updatePassword(newPassword)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Password updated successfully", Toast.LENGTH_SHORT).show()
                            findNavController().navigateUp()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(requireContext(), "Failed to update password: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(requireContext(), "Current password is incorrect", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length >= 8 && password.any { it.isLetter() } && password.any { it.isDigit() }
    }
}
