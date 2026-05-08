package com.sakeena.easemovieapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar

class HelpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help_support)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val btnHelp = findViewById<LinearLayout>(R.id.btnHelpCenter)
        val btnFAQ = findViewById<LinearLayout>(R.id.btnFAQ)
        val btnContact = findViewById<LinearLayout>(R.id.btnContact)
        val btnReport = findViewById<LinearLayout>(R.id.btnReport)

        btnBack.setOnClickListener { finish() }

        btnHelp.setOnClickListener {
            startActivity(Intent(this, HelpCenterActivity::class.java))
        }

        btnFAQ.setOnClickListener {
            startActivity(Intent(this, FaqActivity::class.java))
        }

        btnContact.setOnClickListener {
            showContactOptions()
        }

        btnReport.setOnClickListener {
            startActivity(Intent(this, ReportProblemActivity::class.java))
        }

        btnFAQ.setOnClickListener {
            startActivity(Intent(this, FaqActivity::class.java))
        }


        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //soft ui
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        )

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
    private fun showContactOptions() {
        val options = arrayOf("Email Support", "WhatsApp Support")

        AlertDialog.Builder(this)
            .setTitle("Contact Support")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openEmail()
                    1 -> openWhatsApp()
                }
            }
            .show()
    }

    // 📧 EMAIL
    private fun openEmail() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:haiders9099@gmail.com")
            putExtra(Intent.EXTRA_SUBJECT, "Support Request")
        }

        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show()
        }
    }

    // 💬 WHATSAPP
    private fun openWhatsApp() {
        val phone = "923001234567" // 🔥 apna number yahan daalo (without +)
        val message = "Hello, I need help with your app."

        val url = "https://wa.me/$phone?text=${Uri.encode(message)}"

        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)

        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
        }
    }
}

