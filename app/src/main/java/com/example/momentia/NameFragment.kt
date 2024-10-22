package com.example.momentia

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class NameFragment : Fragment() {

    private lateinit var firstNameEditText: EditText
    private lateinit var lastNameEditText: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_name, container, false)

        firstNameEditText = view.findViewById(R.id.first_name)
        lastNameEditText = view.findViewById(R.id.last_name)

        val continueButton: View = view.findViewById(R.id.continue_button)
        continueButton.setOnClickListener {
            handleContinueButtonClick()
        }

        return view
    }

    private fun handleContinueButtonClick() {
        val firstName = firstNameEditText.text.toString().trim()
        val lastName = lastNameEditText.text.toString().trim()

        if (firstName.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter your first name", Toast.LENGTH_SHORT).show()
            return
        }

        if (lastName.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter your last name", Toast.LENGTH_SHORT).show()
            return
        }

        findNavController().navigate(R.id.cameraFragment)

        Toast.makeText(requireContext(), "Welcome $firstName $lastName!", Toast.LENGTH_SHORT).show()
    }
}
