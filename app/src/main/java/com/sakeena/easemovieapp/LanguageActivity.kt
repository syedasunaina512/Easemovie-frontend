package com.sakeena.easemovieapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class LanguageActivity : BaseActivity() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId: String
        get() = auth.currentUser?.uid ?: "guest_user"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val radioGroup = findViewById<RadioGroup>(R.id.radioGroup)

        // Load current language and check the corresponding radio button
        val currentLang = LocaleHelper.getSavedLanguage(this)
        when (currentLang) {
            "en" -> findViewById<RadioButton>(R.id.langEnglish).isChecked = true
            "ur" -> findViewById<RadioButton>(R.id.langUrdu).isChecked = true
            "hi" -> findViewById<RadioButton>(R.id.langHindi).isChecked = true
            "ar" -> findViewById<RadioButton>(R.id.langArabic).isChecked = true
        }

        btnBack.setOnClickListener {
            finish()
        }

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        )

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val langCode = when (checkedId) {
                R.id.langEnglish -> "en"
                R.id.langUrdu -> "ur"
                R.id.langHindi -> "hi"
                R.id.langArabic -> "ar"
                else -> "en"
            }

            if (langCode != LocaleHelper.getSavedLanguage(this)) {
                changeLanguage(langCode)
            }
        }
    }

    private fun changeLanguage(langCode: String) {
        // 1. Save locally and update configuration
        LocaleHelper.setLocale(this, langCode)
        
        // 2. Save to Firebase for cross-device sync
        val data = mapOf("language" to langCode)
        firestore.collection("users").document(userId)
            .collection("settings").document("language")
            .set(data, SetOptions.merge())
            .addOnFailureListener { e ->
                Log.e("LanguageActivity", "Error saving language to Firestore", e)
            }

        Toast.makeText(this, "Language updated", Toast.LENGTH_SHORT).show()

        // 3. Restart the activity and the whole app task to apply changes everywhere
        val intent = Intent(this, SplashActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }
}
