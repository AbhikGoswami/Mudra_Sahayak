package com.abhik.mudrasahayak

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlin.math.pow

class LoanOfferActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.loanofferscreenlayout)

        val isApproved = intent.getBooleanExtra("LOAN_APPROVED", false)
        val loanAmount = intent.getIntExtra("LOAN_AMOUNT", 0)

        val offerIcon = findViewById<ImageView>(R.id.offerStatusIcon)
        val offerTitle = findViewById<TextView>(R.id.offerTitleTextView)
        val offerSubtitle = findViewById<TextView>(R.id.offerSubtitleTextView)
        val offerCard = findViewById<View>(R.id.offerDetailsCard)
        val acceptButton = findViewById<Button>(R.id.acceptButton)
        val rejectButton = findViewById<Button>(R.id.rejectButton)

        if (isApproved) {
            offerIcon.setImageResource(android.R.drawable.ic_dialog_info)
            offerIcon.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_green_dark))
            offerTitle.text = getString(R.string.congratulations)
            offerSubtitle.text = getString(R.string.loan_offer_subtitle)
            offerCard.visibility = View.VISIBLE
            acceptButton.visibility = View.VISIBLE

            val interestRate = 18.0
            val tenureMonths = 12
            val monthlyRate = interestRate / (12 * 100)
            val emi = (loanAmount * monthlyRate * (1 + monthlyRate).pow(tenureMonths)) / ((1 + monthlyRate).pow(tenureMonths) - 1)

            findViewById<TextView>(R.id.loanAmountTextView).text = "₹ ${"%,d".format(loanAmount)}"
            findViewById<TextView>(R.id.interestRateTextView).text = "$interestRate% p.a."
            findViewById<TextView>(R.id.emiTextView).text = "₹ ${"%,d".format(emi.toInt())}"

        } else {
            offerIcon.setImageResource(android.R.drawable.ic_dialog_alert)
            offerIcon.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_red_dark))
            offerTitle.text = getString(R.string.loan_rejected_title)
            offerSubtitle.text = getString(R.string.loan_rejected_subtitle)
            offerCard.visibility = View.GONE
            acceptButton.visibility = View.GONE
            rejectButton.text = getString(R.string.go_to_home) // Using string resource for better practice
        }

        // --- MODIFIED CLICK LISTENER ---
        acceptButton.setOnClickListener {
            // Create an intent to open the BankDetailsActivity
            val intent = Intent(this, BankDetailsActivity::class.java)
            // Pass the approved loan amount to the next screen
            intent.putExtra("LOAN_AMOUNT", loanAmount)
            startActivity(intent)
        }

        rejectButton.setOnClickListener {
            // Closes all activities and returns to the launcher
            finishAffinity()
        }
    }
}

