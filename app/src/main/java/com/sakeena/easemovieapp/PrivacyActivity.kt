package com.sakeena.easemovieapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PrivacyActivity : AppCompatActivity() {

    private lateinit var switchPrivate: Switch
    private lateinit var switchEmail: Switch
    private lateinit var switchPhone: Switch
    private lateinit var switchStatus: Switch

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId: String
        get() = auth.currentUser?.uid ?: "guest_user"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy)

        val prefs = getSharedPreferences("PrivacySettings", MODE_PRIVATE)

        switchPrivate = findViewById(R.id.switchPrivate)
        switchEmail = findViewById(R.id.switchEmail)
        switchPhone = findViewById(R.id.switchPhone)
        switchStatus = findViewById(R.id.switchStatus)

        // 🔥 Load saved values from Prefs first
        switchPrivate.isChecked = prefs.getBoolean("private", false)
        switchEmail.isChecked = prefs.getBoolean("email", true)
        switchPhone.isChecked = prefs.getBoolean("phone", true)
        switchStatus.isChecked = prefs.getBoolean("status", true)

        // Fetch from Firestore
        firestore.collection("users").document(userId)
            .collection("settings").document("privacy")
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val data = document.data
                    switchPrivate.isChecked = data?.get("private") as? Boolean ?: switchPrivate.isChecked
                    switchEmail.isChecked = data?.get("email") as? Boolean ?: switchEmail.isChecked
                    switchPhone.isChecked = data?.get("phone") as? Boolean ?: switchPhone.isChecked
                    switchStatus.isChecked = data?.get("status") as? Boolean ?: switchStatus.isChecked
                }
            }

        // 💾 Save changes
        switchPrivate.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("private", isChecked).apply()
            updateFirestore("private", isChecked)
        }

        switchEmail.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("email", isChecked).apply()
            updateFirestore("email", isChecked)
        }

        switchPhone.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("phone", isChecked).apply()
            updateFirestore("phone", isChecked)
        }

        switchStatus.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("status", isChecked).apply()
            updateFirestore("status", isChecked)
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

        val blockedCard = findViewById<CardView>(R.id.blockedCard)

        blockedCard.setOnClickListener {
            startActivity(Intent(this, BlockedUsersActivity::class.java))
        }
    }

    private fun updateFirestore(key: String, value: Boolean) {
        firestore.collection("users").document(userId)
            .collection("settings").document("privacy")
            .update(key, value)
            .addOnFailureListener {
                // If update fails (doc might not exist), use set
                val data = mapOf(key to value)
                firestore.collection("users").document(userId)
                    .collection("settings").document("privacy")
                    .set(data, com.google.firebase.firestore.SetOptions.merge())
            }
    }
}