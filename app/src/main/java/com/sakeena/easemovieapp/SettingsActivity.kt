package com.sakeena.easemovieapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.sakeena.easemovieapp.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val btnBack = findViewById<ImageView>(R.id.btnBack)

        val btnAccount = findViewById<LinearLayout>(R.id.itemAccount)
        val btnNotifications = findViewById<LinearLayout>(R.id.itemNotifications)
        val btnAppearance = findViewById<LinearLayout>(R.id.itemAppearance)
        val btnPrivacy = findViewById<LinearLayout>(R.id.itemPrivacy)
        val btnHelp = findViewById<LinearLayout>(R.id.itemHelp)
        val btnAbout = findViewById<LinearLayout>(R.id.itemAbout)
        val btnRatings = findViewById<LinearLayout>(R.id.itemRatings)

        val iconSearchLeft = findViewById<ImageView>(R.id.iconSearchLeft)
        val etSearch = findViewById<EditText>(R.id.etSearch)
        val iconMic = findViewById<ImageView>(R.id.iconMic)
        val iconSearchRight = findViewById<ImageView>(R.id.iconSearchRight)
        val tvNotFound = findViewById<TextView>(R.id.tvNotFound)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        )
// 🔍 CLICK → EXPAND SEARCH
        iconSearchRight.setOnClickListener {

            iconSearchRight.visibility = View.GONE
            iconSearchLeft.visibility = View.VISIBLE
            iconMic.visibility = View.VISIBLE
            etSearch.visibility = View.VISIBLE

            etSearch.alpha = 0f
            etSearch.animate().alpha(1f).setDuration(300).start()

            etSearch.requestFocus()
        }

        iconMic.setOnClickListener {

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...")

            startActivityForResult(intent, 100)
        }

// All options list
        val options = listOf(
            "Account", "Notifications", "Appearance",
            "Privacy & Security", "Help & Support",
            "About", "Rate App"
        )


// 🔎 SEARCH LOGIC
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().lowercase()

                var found = false

                // Check each item
                if ("account".contains(query)) {
                    findViewById<LinearLayout>(R.id.itemAccount).visibility = View.VISIBLE
                    found = true
                } else findViewById<LinearLayout>(R.id.itemAccount).visibility = View.GONE

                if ("notifications".contains(query)) {
                    findViewById<LinearLayout>(R.id.itemNotifications).visibility = View.VISIBLE
                    found = true
                } else findViewById<LinearLayout>(R.id.itemNotifications).visibility = View.GONE

                if ("appearance".contains(query)) {
                    findViewById<LinearLayout>(R.id.itemAppearance).visibility = View.VISIBLE
                    found = true
                } else findViewById<LinearLayout>(R.id.itemAppearance).visibility = View.GONE

                if ("privacy & security".contains(query)) {
                    findViewById<LinearLayout>(R.id.itemPrivacy).visibility = View.VISIBLE
                    found = true
                } else findViewById<LinearLayout>(R.id.itemPrivacy).visibility = View.GONE

                if ("help & support".contains(query)) {
                    findViewById<LinearLayout>(R.id.itemHelp).visibility = View.VISIBLE
                    found = true
                } else findViewById<LinearLayout>(R.id.itemHelp).visibility = View.GONE

                if ("about".contains(query)) {
                    findViewById<LinearLayout>(R.id.itemAbout).visibility = View.VISIBLE
                    found = true
                } else findViewById<LinearLayout>(R.id.itemAbout).visibility = View.GONE

                if ("rate app".contains(query)) {
                    findViewById<LinearLayout>(R.id.itemRatings).visibility = View.VISIBLE
                    found = true
                } else findViewById<LinearLayout>(R.id.itemRatings).visibility = View.GONE

                // ❌ NOT FOUND
                tvNotFound.visibility = if (found) View.GONE else View.VISIBLE
            }


            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 🔙 BACK
        btnBack.setOnClickListener { finish() }

        // 🔗 NAVIGATION
        btnAccount.setOnClickListener {
            startActivity(Intent(this, AccountActivity::class.java))
        }

        btnNotifications.setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))
        }

        btnAppearance.setOnClickListener {
            startActivity(Intent(this, AppearanceActivity::class.java))
        }

        btnPrivacy.setOnClickListener {
            startActivity(Intent(this, PrivacyActivity::class.java))
        }

        btnHelp.setOnClickListener {
            startActivity(Intent(this, HelpActivity::class.java))
        }

        btnAbout.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }

        // ⭐ RATE APP
        btnRatings.setOnClickListener {
            val uri = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }
    }

    private lateinit var etSearch: EditText

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == RESULT_OK) {
            val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)

            if (!result.isNullOrEmpty()) {
                etSearch.setText(result[0])
            }
        }
    }
}
