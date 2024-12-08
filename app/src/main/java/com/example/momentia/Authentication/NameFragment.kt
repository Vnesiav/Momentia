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
import com.google.firebase.auth.FirebaseAuth

class NameFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var email: String
    private lateinit var password: String
    private lateinit var username: String
    private lateinit var firstNameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private var lastClickTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        email = arguments?.getString("email") ?: ""
        password = arguments?.getString("password") ?: ""
        username = arguments?.getString("username") ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_name, container, false)

        firstNameEditText = view.findViewById(R.id.first_name)
        lastNameEditText = view.findViewById(R.id.last_name)
        val backButton: ImageButton = view.findViewById(R.id.back_button)
        val continueButton: View = view.findViewById(R.id.continue_button)

        auth = FirebaseAuth.getInstance()

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

        val email = arguments?.getString("email") ?: ""
        val password = arguments?.getString("password") ?: ""

        val bundle = Bundle().apply {
            putString("email", email)
            putString("password", password)
            putString("username", username)
            putString("firstName", firstName)
            putString("lastName", lastName)
        }
        findNavController().navigate(R.id.action_nameFragment_to_phoneFragment, bundle)
    }
}
