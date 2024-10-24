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
import com.example.momentia.Authentication.BaseAuthFragment
import com.example.momentia.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class ChangeNumberFragment : BaseAuthFragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var currentEmailEditText: EditText
    private lateinit var newEmailEditText: EditText
    private lateinit var saveButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_change_email, container, false)

        // Inisialisasi Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Mendapatkan referensi UI
        currentEmailEditText = view.findViewById(R.id.current_email)
        newEmailEditText = view.findViewById(R.id.new_email)
        saveButton = view.findViewById(R.id.save_button)

        // Mengisi email pengguna yang sudah terautentikasi
        val currentUser = auth.currentUser
        currentEmailEditText.setText(currentUser?.email)

        // Menambahkan event klik pada tombol "Save"
        saveButton.setOnClickListener {
            val newEmail = newEmailEditText.text.toString().trim()

            if (newEmail.isEmpty()) {
                Toast.makeText(requireContext(), "New email field is required", Toast.LENGTH_SHORT).show()
            } else {
                changeEmail(currentUser, newEmail)
            }
        }

        return view
    }

    // Fungsi untuk mengubah email tanpa re-autentikasi
    private fun changeEmail(user: FirebaseUser?, newEmail: String) {
        if (user != null) {
            // Mengubah alamat email langsung
            user.updateEmail(newEmail).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Mengupdate email di Firestore
                    updateEmailInFirestore(user, newEmail)
                } else {
                    Toast.makeText(requireContext(), "Failed to change email: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    // Fungsi untuk memperbarui email di Firestore
    private fun updateEmailInFirestore(user: FirebaseUser, newEmail: String) {
        val firestore = FirebaseFirestore.getInstance().collection("users").document(user.uid)
        firestore.update("email", newEmail)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Email updated successfully", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp() // Navigasi kembali ke layar sebelumnya
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to update email in Firestore: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}