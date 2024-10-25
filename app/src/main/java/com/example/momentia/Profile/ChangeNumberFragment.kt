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

class ChangeNumberFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var currentNumberEditText: EditText
    private lateinit var newNumberEditText: EditText
    private var currentUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        currentUser = auth.currentUser
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_change_number, container, false)

        currentNumberEditText = view.findViewById(R.id.current_number)
        newNumberEditText = view.findViewById(R.id.new_number)
        val backButton: ImageButton = view.findViewById(R.id.back_button)
        val saveButton: View = view.findViewById(R.id.save_button)

        loadCurrentNumber()

        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        saveButton.setOnClickListener {
            handleSaveButtonClick()
        }

        return view
    }

    private fun loadCurrentNumber() {
        currentUser?.let { user ->
            firestore.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.contains("phoneNumber")) {
                        currentNumberEditText.setText(document.getString("phoneNumber"))
                    } else {
                        Toast.makeText(requireContext(), "Failed to load current phone number", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun handleSaveButtonClick() {
        val newPhoneNumber = newNumberEditText.text.toString().trim()

        if (newPhoneNumber.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter your new phone number", Toast.LENGTH_SHORT).show()
            return
        }

        val phonePattern = Regex("^08[0-9]{8,12}$")
        if (!newPhoneNumber.matches(phonePattern)) {
            Toast.makeText(requireContext(), "Invalid phone number. Please enter a valid Indonesian phone number.", Toast.LENGTH_SHORT).show()
            return
        }

        currentUser?.let { user ->
            firestore.collection("users").document(user.uid)
                .update("phoneNumber", newPhoneNumber)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Phone number updated successfully", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Failed to update phone number in Firestore: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
