package com.sakeena.easemovieapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SecurityActivity : AppCompatActivity() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId: String
        get() = auth.currentUser?.uid ?: "guest_user"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_security)

        val changePassword = findViewById<LinearLayout>(R.id.itemChangePassword)
        val switch2FA = findViewById<Switch>(R.id.switch2FA)
        val switchLogin = findViewById<Switch>(R.id.switchLogin)

        // Load initial state from Firestore
        firestore.collection("users").document(userId)
            .collection("settings").document("security")
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    switch2FA.isChecked = document.getBoolean("twoFactor") ?: false
                    switchLogin.isChecked = document.getBoolean("loginAlerts") ?: false
                }
            }


        changePassword.setOnClickListener {
            startActivity(Intent(this, ChangePasswordActivity::class.java))
            finish()
        }

        switch2FA.setOnCheckedChangeListener { _, isChecked ->
            updateFirestore("twoFactor", isChecked)
            if (isChecked) {
                startActivity(Intent(this, OtpVerificationActivity::class.java))
            }
        }

        switchLogin.setOnCheckedChangeListener { _, isChecked ->
            updateFirestore("loginAlerts", isChecked)
            Toast.makeText(this, "Login Alerts: $isChecked", Toast.LENGTH_SHORT).show()
        }

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        )
    }

    private fun updateFirestore(key: String, value: Boolean) {
        val data = mapOf(key to value)
        firestore.collection("users").document(userId)
            .collection("settings").document("security")
            .set(data, com.google.firebase.firestore.SetOptions.merge())
            .addOnFailureListener { e ->
                Log.e("SecurityActivity", "Error updating security settings", e)
            }
    }
}