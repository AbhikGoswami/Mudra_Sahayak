package com.abhik.mudrasahayak

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class AuthActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var actionButton: Button
    private lateinit var toggleTextView: TextView
    private lateinit var titleTextView: TextView
    private lateinit var progressBar: ProgressBar

    private var isLoginMode = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase Auth using getInstance()
        val auth = FirebaseAuth.getInstance()

        // Check if user is already signed in
        if (auth.currentUser != null) {
            navigateToLanguageSelection()
            return // Skip the rest of onCreate
        }

        // If no user, set the content view for authentication
        setContentView(R.layout.authenticationscreenlayout)

        // Find views
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        actionButton = findViewById(R.id.actionButton)
        toggleTextView = findViewById(R.id.toggleTextView)
        titleTextView = findViewById(R.id.titleTextView)
        progressBar = findViewById(R.id.progressBar)

        // Set click listeners
        actionButton.setOnClickListener {
            handleAction(auth)
        }

        toggleTextView.setOnClickListener {
            toggleMode()
        }
    }

    private fun handleAction(auth: FirebaseAuth) {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.error = "Please enter a valid email"
            emailEditText.requestFocus()
            return
        }

        if (password.length < 6) {
            passwordEditText.error = "Password must be at least 6 characters"
            passwordEditText.requestFocus()
            return
        }

        showLoading(true)

        if (isLoginMode) {
            // Login User
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    showLoading(false)
                    if (task.isSuccessful) {
                        navigateToLanguageSelection()
                    } else {
                        Toast.makeText(baseContext, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            // Register User
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    showLoading(false)
                    if (task.isSuccessful) {
                        Toast.makeText(baseContext, "Registration successful! Please login.", Toast.LENGTH_SHORT).show()
                        toggleMode() // Switch to login mode after successful registration
                    } else {
                        Toast.makeText(baseContext, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun toggleMode() {
        isLoginMode = !isLoginMode
        if (isLoginMode) {
            titleTextView.text = "Login"
            actionButton.text = "Login"
            toggleTextView.text = "Don't have an account? Register"
        } else {
            titleTextView.text = "Register"
            actionButton.text = "Register"
            toggleTextView.text = "Already have an account? Login"
        }
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        actionButton.isEnabled = !isLoading
        toggleTextView.isEnabled = !isLoading
    }

    private fun navigateToLanguageSelection() {
        val intent = Intent(this, LanguageSelectionActivity::class.java)
        startActivity(intent)
        finish() // Finish AuthActivity so user can't go back to it
    }
}

