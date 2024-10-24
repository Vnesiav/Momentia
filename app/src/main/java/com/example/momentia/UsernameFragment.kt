package com.example.momentia

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class UsernameFragment : Fragment() {
    private lateinit var usernameEditText: EditText
    private lateinit var email: String
    private lateinit var password: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        email = arguments?.getString("email") ?: ""
        password = arguments?.getString("password") ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_username, container, false)

        usernameEditText = view.findViewById(R.id.username_input)
        val backButton: ImageButton = view.findViewById(R.id.back_button)
        val continueButton: View = view.findViewById(R.id.continue_button)

        backButton.setOnClickListener {
            findNavController().navigate(R.id.action_usernameFragment_to_passwordFragment)
        }

        continueButton.setOnClickListener {
            handleContinueButtonClick()
        }

        return view
    }

    private fun handleContinueButtonClick() {
        val username = usernameEditText.text.toString().trim()

        if (username.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter your username", Toast.LENGTH_SHORT).show()
            return
        }

        val bundle = Bundle().apply {
            putString("email", email)
            putString("password", password)
            putString("username", username)
        }
        findNavController().navigate(R.id.action_usernameFragment_to_nameFragment, bundle)
    }
}
