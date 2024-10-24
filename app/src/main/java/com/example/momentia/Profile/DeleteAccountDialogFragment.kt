package com.example.momentia.Profile

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.momentia.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DeleteAccountDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                .setPositiveButton("Delete") { _, _ ->
                    deleteAccount()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun deleteAccount() {
        val user = FirebaseAuth.getInstance().currentUser
        val db = FirebaseFirestore.getInstance()

        user?.let {
            val userId = it.uid
            db.collection("users").document(userId).delete().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    it.delete().addOnCompleteListener { authTask ->
                        if (authTask.isSuccessful) {
                            Toast.makeText(requireContext(), "Account deleted successfully", Toast.LENGTH_SHORT).show()
                            dismiss()
                            findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
                        } else {
                            Toast.makeText(requireContext(), "Failed to delete account from authentication", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to delete account from Firestore", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
