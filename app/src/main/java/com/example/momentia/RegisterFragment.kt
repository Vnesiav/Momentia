package com.example.momentia

import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var termsTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val emailEditText = view.findViewById<EditText>(R.id.emailEditText)
        val continueButton = view.findViewById<Button>(R.id.continueButton)
        termsTextView = view.findViewById(R.id.termsTextView)

        setTermsText()

        continueButton.setOnClickListener {
            val email = emailEditText.text.toString()
            if (email.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter your email", Toast.LENGTH_SHORT).show()
            } else if (!email.contains("@")) {
                Toast.makeText(requireContext(), "Please enter a valid email address", Toast.LENGTH_SHORT).show()
            } else {
                checkIfEmailExists(email) { emailExists ->
                    if (emailExists) {
                        Toast.makeText(requireContext(), "Email is already registered. Please login or use a different email.", Toast.LENGTH_SHORT).show()
                    } else {
                        val bundle = Bundle().apply {
                            putString("email", email)
                        }
                        findNavController().navigate(R.id.action_registerFragment_to_passwordFragment, bundle)
                    }
                }
            }
        }
    }

    private fun checkIfEmailExists(email: String, callback: (Boolean) -> Unit) {
        firestore.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    callback(true)
                } else {
                    callback(false)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Error checking email: ${exception.message}", Toast.LENGTH_SHORT).show()
                callback(false)
            }
    }

    private fun setTermsText() {
        val termsText = "By tapping Continue, you are agreeing to our Terms of Service and Privacy Policy"
        val spannableString = SpannableString(termsText)

        val termsStart = termsText.indexOf("Terms of Service")
        val termsEnd = termsStart + "Terms of Service".length
        val privacyStart = termsText.indexOf("Privacy Policy")
        val privacyEnd = privacyStart + "Privacy Policy".length

        spannableString.setSpan(
            StyleSpan(Typeface.BOLD),
            termsStart, termsEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannableString.setSpan(
            StyleSpan(Typeface.BOLD),
            privacyStart, privacyEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        termsTextView.text = spannableString
    }
}