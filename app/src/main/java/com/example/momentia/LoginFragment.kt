package com.example.momentia

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : Fragment() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
// Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        (activity as MainActivity).hideBottomNavigation()
        val registerButton = view.findViewById<Button>(R.id.register_button)
        val emailInput = view.findViewById<TextInputEditText>(R.id.email_input_field)
        val passwordInput =
            view.findViewById<TextInputEditText>(R.id.password_input_field)
        auth = FirebaseAuth.getInstance()
        registerButton.setOnClickListener{
            findNavController().navigate(R.id.registerFragment)
        }

        val loginButton = view.findViewById<Button>(R.id.submit_button)
        loginButton.setOnClickListener{
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Email and Password are required",
                    Toast.LENGTH_SHORT).show()
            }else{
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(requireActivity()) { task ->
                        if (task.isSuccessful) {
                            Log.d("Login", "signInWithEmail:success")
                            findNavController().navigate(R.id.cameraFragment)
                        } else {
                            Log.w("Login", "signInWithEmail:failure", task.exception)
                            Toast.makeText(requireContext(), "Email or Password Wrong",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

//        PhoneAUthActivity https://firebase.google.com/docs/auth/android/phone-auth?hl=en&authuser=0&_gl=1*u44yny*_ga*NzA4OTgzODUxLjE3MjkwMDY0MDg.*_ga_CW55HF8NVT*MTcyOTU3MjQwMi4xMy4xLjE3Mjk1NzI5NDguNjAuMC4w#kotlin+ktx
//        val options = PhoneAuthOptions.newBuilder(auth)
//            .setPhoneNumber(phoneNumber) // Phone number to verify
//            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
//            .setActivity(this) // Activity (for callback binding)
//            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
//            .build()
//        PhoneAuthProvider.verifyPhoneNumber(options)
    }
}
