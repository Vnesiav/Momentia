package com.example.momentia.Profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.example.momentia.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DeleteAccountDialogFragment : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_delete_account_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val displayMetrics = resources.displayMetrics
        val width = (displayMetrics.widthPixels * 0.85).toInt()

        dialog?.window?.setLayout(
            width,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        dialog?.window?.setBackgroundDrawableResource(R.drawable.dialog_background)

        val confirmDeleteButton: Button = view.findViewById(R.id.confirm_delete_button)
        val cancelDeleteButton: Button = view.findViewById(R.id.cancel_delete_button)

        confirmDeleteButton.setOnClickListener {
            deleteAccount()
        }

        cancelDeleteButton.setOnClickListener {
            dismiss()
        }
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
                            showToast("Account deleted successfully")
                            navigateToLogin()
                        } else {
                            showToast("Failed to delete account from authentication: ${authTask.exception?.message}")
                        }
                    }
                } else {
                    showToast("Failed to delete account from Firestore: ${task.exception?.message}")
                }
            }
        }
    }

    private fun showToast(message: String) {
        activity?.runOnUiThread {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToLogin() {
        dismissAllowingStateLoss()
        if (isAdded && findNavController().currentDestination?.id == R.id.profileFragment) {
            findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
        }
    }
}
