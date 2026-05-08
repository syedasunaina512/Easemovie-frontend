package com.sakeena.easemovieapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val itemWebsite = findViewById<LinearLayout>(R.id.itemWebsite)
        val itemPrivacy = findViewById<LinearLayout>(R.id.itemPrivacyPolicy)
        val itemTerms = findViewById<LinearLayout>(R.id.itemTerms)
        val itemShare = findViewById<LinearLayout>(R.id.itemShare)

        // 🌐 Website
        itemWebsite.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW,
                Uri.parse("https://yourwebsite.com"))
            startActivity(intent)
        }

        // 🔐 Privacy Policy
        itemPrivacy.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW,
                Uri.parse("https://yourwebsite.com/privacy"))
            startActivity(intent)
        }

        // 📜 Terms
        itemTerms.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW,
                Uri.parse("https://yourwebsite.com/terms"))
            startActivity(intent)
        }

        // 📤 Share App
        itemShare.setOnClickListener {
            val share = Intent(Intent.ACTION_SEND)
            share.type = "text/plain"
            share.putExtra(Intent.EXTRA_TEXT,
                "Check out this app: https://play.google.com/store/apps/details?id=$packageName")
            startActivity(Intent.createChooser(share, "Share via"))
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