package com.abhik.mudrasahayak

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class LanguageSelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.languageselectionactivity)

        findViewById<Button>(R.id.btnEnglish).setOnClickListener {
            setLocaleAndStart("en")
        }

        findViewById<Button>(R.id.btnHindi).setOnClickListener {
            setLocaleAndStart("hi")
        }
    }

    private fun setLocaleAndStart(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration()
        @Suppress("DEPRECATION")
        config.locale = locale
        @Suppress("DEPRECATION")
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
