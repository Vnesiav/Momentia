package com.example.momentia.Authentication

import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.momentia.DTO.User
import com.example.momentia.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var registerTextView: TextView
    private lateinit var credentialManager: CredentialManager
    private var lastClickTime: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        credentialManager = CredentialManager.create(requireContext())

        val emailEditText = view.findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = view.findViewById<EditText>(R.id.passwordEditText)
        val loginButton = view.findViewById<Button>(R.id.loginButton)
        val forgotPasswordTextView = view.findViewById<TextView>(R.id.forgotPassword)
        val googleButton = view.findViewById<Button>(R.id.loginGoogle)
        registerTextView = view.findViewById<TextView>(R.id.registerTextView)

        setRegisterText()

        loginButton.setOnClickListener {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime < 3000) {
                return@setOnClickListener
            }

            lastClickTime = currentTime

            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter your email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter your password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginUser(email, password)
        }

        googleButton.setOnClickListener {
            logInWithGoogle()
        }

        forgotPasswordTextView.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter your email to reset password", Toast.LENGTH_SHORT).show()
            } else {
                sendPasswordResetEmail(email)
            }
        }

        registerTextView.setOnClickListener {
            findNavController().navigate(R.id.registerFragment)
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        if (user.isEmailVerified) {
                            Toast.makeText(requireContext(), "Login successful!", Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                        } else {
                            Toast.makeText(requireContext(), "Please verify your email first.", Toast.LENGTH_SHORT).show()
                            auth.signOut()
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Password reset email sent to $email", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun logInWithGoogle() {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.default_web_client_id))
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val coroutineScope = lifecycleScope

        coroutineScope.launch {
            try {
                val result: GetCredentialResponse = credentialManager.getCredential(
                    request = request,
                    context = requireContext()
                )

                onLogInWithGoogle(result.credential)
            } catch (e: GetCredentialException) {
                Log.d("LoginFragment", e.message.orEmpty())
            }
        }
    }

    private suspend fun onLogInWithGoogle(credential: Credential) {
        if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val idToken = googleIdTokenCredential.idToken

            Log.d("LoginFragment", "Google ID Token: $idToken")

            handleGoogleLogIn(idToken)
        } else {
            Log.e("LoginFragment", "Unexpected credential type")
        }
    }

    private suspend fun handleGoogleLogIn(idToken: String) {
        try {
            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(firebaseCredential).await()

            val user = authResult.user
            if (user != null) {
                val displayName = user.displayName ?: ""
                val names = displayName.split(" ", limit = 2)
                val firstName = if (names.isNotEmpty()) names[0] else ""
                val lastName = if (names.size > 1) names[1] else ""

                val baseUsername = user.email?.substringBefore("@")?.lowercase() ?: "user"
                val uniqueUsername = generateUniqueUsernameFromEmail(baseUsername)

                val userData = User(
                    firstName = firstName,
                    lastName = lastName,
                    email = user.email ?: "",
                    username = uniqueUsername,
                    phoneNumber = user.phoneNumber ?: "",
                    avatarUrl = user.photoUrl?.toString() ?: "",
                    friends = emptyList(),
                    snapsReceived = emptyList(),
                    snapsSent = emptyList(),
                    stories = emptyList(),
                    createdAt = com.google.firebase.Timestamp.now()
                )

                saveUserToFirestore(user.uid, userData)

                Log.d("LoginFragment", "Google Sign-In successful: ${user.email}")
                Toast.makeText(requireContext(), "Login successful!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
            }
        } catch (e: Exception) {
            Log.e("LoginFragment", "Google Sign-In failed: ${e.message}")
            Toast.makeText(requireContext(), "Login failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun generateUniqueUsernameFromEmail(baseUsername: String): String {
        var username = baseUsername
        var isTaken = checkIfUsernameExistsSuspend(username)

        while (isTaken) {
            val randomSuffix = (1..2)
                .map { ('a'..'z') + ('0'..'9') }
                .flatten()
                .shuffled()
                .take(2)
                .joinToString("")

            username = "$baseUsername$randomSuffix"
            isTaken = checkIfUsernameExistsSuspend(username)
        }
        return username
    }

    private suspend fun checkIfUsernameExistsSuspend(username: String): Boolean {
        return try {
            val result = db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .await()
            !result.isEmpty
        } catch (e: Exception) {
            Log.e("LoginFragment", "Error checking username: ${e.message}")
            false
        }
    }

    private fun saveUserToFirestore(userId: String, user: User) {
        db.collection("users").document(userId)
            .set(user)
            .addOnSuccessListener {
                Log.d("LoginFragment", "User data saved successfully.")
            }
            .addOnFailureListener { e ->
                Log.e("LoginFragment", "Error saving user data: ${e.message}")
            }
    }

    private fun setRegisterText() {
        val registerText = "Don't have an account? Create account"
        val spannableString = SpannableString(registerText)

        val registerStart = registerText.indexOf("Create account")
        val registerEnd = registerStart + "Create account".length

        spannableString.setSpan(
            StyleSpan(Typeface.BOLD),
            registerStart, registerEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannableString.setSpan(
            UnderlineSpan(),
            registerStart, registerEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        registerTextView.text = spannableString
    }
}
