package com.example.momentia.Authentication

import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.momentia.R
import com.google.firebase.auth.FirebaseAuth

class PasswordFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var email: String
    private var lastClickTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        email = arguments?.getString("email") ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val passwordEditText = view.findViewById<EditText>(R.id.passwordEditText)
        val continueButton: View = view.findViewById(R.id.continueButton)
        val backButton: ImageButton = view.findViewById(R.id.back_button)

        setPasswordRequirementsText(view)

        backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        continueButton.setOnClickListener {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime < 3000) {
                return@setOnClickListener
            }
            lastClickTime = currentTime

            val password = passwordEditText.text.toString()

            if (password.isEmpty() || !isPasswordValid(password)) {
                val message = if (password.isEmpty()) {
                    "Please enter your password"
                } else {
                    "Password must be at least 8 characters and include letters and numbers."
                }
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val bundle = Bundle().apply {
                putString("email", email)
                putString("password", password)
            }
            findNavController().navigate(R.id.action_passwordFragment_to_usernameFragment, bundle)
        }
    }

    private fun isPasswordValid(password: String): Boolean {
        if (password.length < 8) {
            return false
        }

        val hasLetter = password.any { it.isLetter() }
        val hasDigit = password.any { it.isDigit() }

        return hasLetter && hasDigit
    }

    private fun setPasswordRequirementsText(view: View) {
        val requirementsText = "Your password must be at least 8 characters include a combination of letters and numbers"
        val spannableString = SpannableString(requirementsText)

        val boldPart1 = "at least 8 characters"
        val start1 = requirementsText.indexOf(boldPart1)
        val end1 = start1 + boldPart1.length

        val boldPart2 = "letters and numbers"
        val start2 = requirementsText.indexOf(boldPart2)
        val end2 = start2 + boldPart2.length

        spannableString.setSpan(StyleSpan(Typeface.BOLD), start1, end1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(StyleSpan(Typeface.BOLD), start2, end2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        val passwordRequirementsTextView = view.findViewById<TextView>(R.id.passwordRequirementsTextView)
        passwordRequirementsTextView.text = spannableString
    }
}
