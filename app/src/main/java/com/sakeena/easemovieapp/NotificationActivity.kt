package com.sakeena.easemovieapp

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.CompoundButton
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class NotificationActivity : AppCompatActivity() {

    // Firebase
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId: String
        get() = auth.currentUser?.uid ?: "guest_user"

    // Switches
    private lateinit var switchPush: Switch
    private lateinit var switchEmail: Switch
    private lateinit var switchComments: Switch
    private lateinit var switchUpdates: Switch
    private lateinit var switchPromo: Switch
    private lateinit var switchReminder: Switch
    private lateinit var switchDnd: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        // Toolbar
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Bind Views
        bindViews()

        // Load saved values
        loadSettings()

        // Setup listeners
        setupListeners()
    }

    private fun bindViews() {
        switchPush = findViewById(R.id.switchEmail)
        switchEmail = findViewById(R.id.switchSMS)
        switchComments = findViewById(R.id.switchComments)
        switchUpdates = findViewById(R.id.switchUpdates)
        switchPromo = findViewById(R.id.switchSound)
        switchReminder = findViewById(R.id.switchVibration)
        switchDnd = findViewById(R.id.switchDnd)

        //soft ui
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        )
    }

    private fun setupListeners() {

        val switches = listOf(
            switchPush, switchEmail, switchComments,
            switchUpdates, switchPromo, switchReminder
        )

        // Common listener
        val listener = CompoundButton.OnCheckedChangeListener { _, _ ->
            saveSettings()
        }

        switches.forEach {
            it.setOnCheckedChangeListener(listener)
        }

        // ✅ DND Special Logic (FIXED)
        switchDnd.setOnCheckedChangeListener { _, isChecked ->
            enableAllSwitches(!isChecked)
            saveSettings()
        }
    }

    private fun enableAllSwitches(enable: Boolean) {
        switchPush.isEnabled = enable
        switchEmail.isEnabled = enable
        switchComments.isEnabled = enable
        switchUpdates.isEnabled = enable
        switchPromo.isEnabled = enable
        switchReminder.isEnabled = enable
    }

    // SAVE DATA
    private fun saveSettings() {
        val pref = getSharedPreferences("NOTIFICATION_SETTINGS", MODE_PRIVATE)

        val settings = mapOf(
            "push" to switchPush.isChecked,
            "email" to switchEmail.isChecked,
            "comments" to switchComments.isChecked,
            "updates" to switchUpdates.isChecked,
            "promo" to switchPromo.isChecked,
            "reminder" to switchReminder.isChecked,
            "dnd" to switchDnd.isChecked
        )

        // Save to SharedPreferences (for offline/quick access)
        with(pref.edit()) {
            settings.forEach { (key, value) -> putBoolean(key, value) }
            apply()
        }

        // Save to Firestore
        firestore.collection("users").document(userId)
            .collection("settings").document("notifications")
            .set(settings)
            .addOnFailureListener { e ->
                Log.e("NotificationActivity", "Error saving settings", e)
            }
    }

    // LOAD DATA
    private fun loadSettings() {
        // Load from SharedPreferences first for immediate UI update
        val pref = getSharedPreferences("NOTIFICATION_SETTINGS", MODE_PRIVATE)

        switchPush.isChecked = pref.getBoolean("push", true)
        switchEmail.isChecked = pref.getBoolean("email", false)
        switchComments.isChecked = pref.getBoolean("comments", true)
        switchUpdates.isChecked = pref.getBoolean("updates", true)
        switchPromo.isChecked = pref.getBoolean("promo", false)
        switchReminder.isChecked = pref.getBoolean("reminder", true)
        switchDnd.isChecked = pref.getBoolean("dnd", false)

        enableAllSwitches(!switchDnd.isChecked)

        // Then fetch from Firestore to sync
        firestore.collection("users").document(userId)
            .collection("settings").document("notifications")
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val data = document.data
                    switchPush.isChecked = data?.get("push") as? Boolean ?: switchPush.isChecked
                    switchEmail.isChecked = data?.get("email") as? Boolean ?: switchEmail.isChecked
                    switchComments.isChecked = data?.get("comments") as? Boolean ?: switchComments.isChecked
                    switchUpdates.isChecked = data?.get("updates") as? Boolean ?: switchUpdates.isChecked
                    switchPromo.isChecked = data?.get("promo") as? Boolean ?: switchPromo.isChecked
                    switchReminder.isChecked = data?.get("reminder") as? Boolean ?: switchReminder.isChecked
                    switchDnd.isChecked = data?.get("dnd") as? Boolean ?: switchDnd.isChecked
                    
                    enableAllSwitches(!switchDnd.isChecked)
                }
            }
            .addOnFailureListener { e ->
                Log.e("NotificationActivity", "Error loading settings", e)
            }
    }
}