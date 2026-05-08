package com.sakeena.easemovieapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar

class HelpCenterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help_center)

        val faq = findViewById<LinearLayout>(R.id.itemFAQ)
        val contact = findViewById<LinearLayout>(R.id.itemContact)
        val report = findViewById<LinearLayout>(R.id.itemReport)

        // 🔹 FAQ Screen
        faq.setOnClickListener {
            startActivity(Intent(this, FaqActivity::class.java))
        }

        // 🔹 Contact Support (Email + WhatsApp)
        contact.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:support@easemovie.com")
            intent.putExtra(Intent.EXTRA_SUBJECT, "Support Request")
            startActivity(intent)
        }

        // 🔹 Report Problem Screen
        report.setOnClickListener {
            startActivity(Intent(this, ReportProblemActivity::class.java))
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
}