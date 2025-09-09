package com.abhik.mudrasahayak

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BankDetailsActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bankdetailsformlayout)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val loanAmount = intent.getIntExtra("LOAN_AMOUNT", 0)

        val loanAmountTextView = findViewById<TextView>(R.id.loanAmountTextView)
        val accountHolderNameEditText = findViewById<EditText>(R.id.accountHolderNameEditText)
        val accountNumberEditText = findViewById<EditText>(R.id.accountNumberEditText)
        val ifscCodeEditText = findViewById<EditText>(R.id.ifscCodeEditText)
        val submitButton = findViewById<Button>(R.id.submitButton)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        loanAmountTextView.text = "₹ ${"%,d".format(loanAmount)}"

        submitButton.setOnClickListener {
            val name = accountHolderNameEditText.text.toString().trim()
            val accountNumber = accountNumberEditText.text.toString().trim()
            val ifsc = ifscCodeEditText.text.toString().trim()

            if (name.isEmpty() || accountNumber.isEmpty() || ifsc.isEmpty()) {
                Toast.makeText(this, "Please fill all the details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE
            submitButton.isEnabled = false

            val user = auth.currentUser
            if (user != null) {
                val bankDetails = hashMapOf(
                    "userId" to user.uid,
                    "accountHolderName" to name,
                    "accountNumber" to accountNumber,
                    "ifscCode" to ifsc,
                    "loanAmount" to loanAmount,
                    "status" to "Pending Disbursal",
                    "timestamp" to System.currentTimeMillis()
                )

                firestore.collection("loanApplications")
                    .add(bankDetails)
                    .addOnSuccessListener {
                        progressBar.visibility = View.GONE
                        showSuccessDialog()
                    }
                    .addOnFailureListener { e ->
                        progressBar.visibility = View.GONE
                        submitButton.isEnabled = true
                        Toast.makeText(this, "Failed to save details: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            } else {
                progressBar.visibility = View.GONE
                submitButton.isEnabled = true
                Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showSuccessDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.details_submitted_title))
            .setMessage(getString(R.string.details_submitted_message))
            .setPositiveButton(getString(R.string.permission_button_ok)) { dialog, _ ->
                dialog.dismiss()
                finishAffinity() // Closes all activities and exits the app
            }
            .setCancelable(false)
            .show()
    }
}
