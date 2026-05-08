package com.sakeena.easemovieapp

import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var etCurrent: TextInputEditText
    private lateinit var etNew: TextInputEditText
    private lateinit var etConfirm: TextInputEditText
    private lateinit var btnUpdate: MaterialButton
    private lateinit var tvStrength: TextView
    private lateinit var tvMatch: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        etCurrent = findViewById(R.id.etCurrentPassword)
        etNew = findViewById(R.id.etNewPassword)
        etConfirm = findViewById(R.id.etConfirmPassword)
        btnUpdate = findViewById(R.id.btnUpdatePassword)
        tvStrength = findViewById(R.id.tvStrength)
        tvMatch = findViewById(R.id.tvMatch)

        // 🔥 NEW: TextInputLayouts (for eye toggle)
        val tilCurrent = findViewById<TextInputLayout>(R.id.tilCurrentPassword)
        val tilNew = findViewById<TextInputLayout>(R.id.tilNewPassword)
        val tilConfirm = findViewById<TextInputLayout>(R.id.tilConfirmPassword)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        val prefs = getSharedPreferences("UserProfile", MODE_PRIVATE)

        // 👁 TOGGLE FUNCTION (UPDATED)
        fun togglePassword(layout: TextInputLayout, editText: TextInputEditText) {

            val isVisible =
                editText.inputType == (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)

            if (isVisible) {
                editText.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                layout.endIconDrawable = getDrawable(R.drawable.ic_eye_closed_24)
            } else {
                editText.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                layout.endIconDrawable = getDrawable(R.drawable.ic_eye_24)
            }

            editText.setSelection(editText.text?.length ?: 0)
        }

        // 👁 CLICK LISTENERS
        tilCurrent.setEndIconOnClickListener {
            togglePassword(tilCurrent, etCurrent)
        }

        tilNew.setEndIconOnClickListener {
            togglePassword(tilNew, etNew)
        }

        tilConfirm.setEndIconOnClickListener {
            togglePassword(tilConfirm, etConfirm)
        }

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        )

        // 🔐 PASSWORD STRENGTH
        etNew.addTextChangedListener {

            val password = it.toString()

            if (password.isEmpty()) {
                tvStrength.visibility = View.GONE
            } else {

                if (tvStrength.visibility == View.GONE) {
                    tvStrength.visibility = View.VISIBLE
                    val anim = AnimationUtils.loadAnimation(this, R.anim.fade_in)
                    tvStrength.startAnimation(anim)
                }

                when {
                    password.length < 6 -> {
                        tvStrength.text = "Strength: Weak"
                        tvStrength.setTextColor(Color.RED)
                    }
                    password.matches(Regex("^(?=.*[A-Z])(?=.*[0-9]).+$")) -> {
                        tvStrength.text = "Strength: Strong"
                        tvStrength.setTextColor(Color.GREEN)
                    }
                    else -> {
                        tvStrength.text = "Strength: Medium"
                        tvStrength.setTextColor(Color.parseColor("#FFA500"))
                    }
                }
            }
        }

        // 🔐 PASSWORD MATCH
        etConfirm.addTextChangedListener {

            val confirm = it.toString()
            val newPass = etNew.text.toString()

            if (confirm.isEmpty()) {
                tvMatch.visibility = View.GONE
            } else if (confirm == newPass) {

                if (tvMatch.visibility == View.GONE) {
                    tvMatch.visibility = View.VISIBLE
                    val anim = AnimationUtils.loadAnimation(this, R.anim.fade_in)
                    tvMatch.startAnimation(anim)
                }

                tvMatch.text = "✔ Passwords Match"
                tvMatch.setTextColor(Color.parseColor("#4CAF50"))

            } else {
                tvMatch.visibility = View.VISIBLE
                tvMatch.text = "✖ Passwords do not match"
                tvMatch.setTextColor(Color.RED)
            }
        }

        // 🔥 SHAKE
        fun shake(view: View) {
            val anim = AnimationUtils.loadAnimation(this, R.anim.shake)
            view.startAnimation(anim)
        }

        // 🔘 BUTTON
        btnUpdate.setOnClickListener {

            val current = etCurrent.text.toString().trim()
            val newPass = etNew.text.toString().trim()
            val confirm = etConfirm.text.toString().trim()

            if (current.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "All fields required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPass.length < 6) {
                etNew.error = "Minimum 6 characters"
                shake(etNew)
                return@setOnClickListener
            }

            if (newPass != confirm) {
                etConfirm.error = "Password not match"
                shake(etConfirm)
                return@setOnClickListener
            }

            val user = FirebaseAuth.getInstance().currentUser
            if (user != null && user.email != null) {
                // Re-authenticate user before changing password (Firebase requirement)
                val credential = EmailAuthProvider.getCredential(user.email!!, current)
                
                btnUpdate.isEnabled = false
                btnUpdate.text = "Updating..."

                user.reauthenticate(credential).addOnCompleteListener { reAuthTask ->
                    if (reAuthTask.isSuccessful) {
                        // Success re-auth, now update password
                        user.updatePassword(newPass).addOnCompleteListener { updateTask ->
                            btnUpdate.isEnabled = true
                            btnUpdate.text = "Update Password"
                            
                            if (updateTask.isSuccessful) {
                                // Sync local storage if needed
                                prefs.edit().putString("password", newPass).apply()
                                Toast.makeText(this, "Password Updated Successfully", Toast.LENGTH_SHORT).show()
                                finish()
                            } else {
                                Toast.makeText(this, "Update Failed: ${updateTask.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    } else {
                        btnUpdate.isEnabled = true
                        btnUpdate.text = "Update Password"
                        etCurrent.error = "Incorrect current password"
                        shake(etCurrent)
                        Toast.makeText(this, "Authentication Failed", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "User session expired. Please login again.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 🔥 HIDE KEYBOARD
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        currentFocus?.let {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, 0)
            it.clearFocus()
        }
        return super.dispatchTouchEvent(ev)
    }
}