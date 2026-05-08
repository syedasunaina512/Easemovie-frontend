package com.sakeena.easemovieapp

import android.os.Bundle
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar


class OtpVerificationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp_verification)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val btnVerify = findViewById<Button>(R.id.btnVerify)
        val tvResend = findViewById<TextView>(R.id.tvResend)

        val otpFields = listOf(
            findViewById<EditText>(R.id.otp1),
            findViewById<EditText>(R.id.otp2),
            findViewById<EditText>(R.id.otp3),
            findViewById<EditText>(R.id.otp4),
            findViewById<EditText>(R.id.otp5),
            findViewById<EditText>(R.id.otp6)
        )

        setupOtpInputs(otpFields)

        btnBack.setOnClickListener { finish() }

        btnVerify.setOnClickListener {
            val otp = otpFields.joinToString("") { it.text.toString() }

            if (otp.length < 6) {
                Toast.makeText(this, "Enter complete OTP", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "OTP Verified ✅", Toast.LENGTH_SHORT).show()
            }
        }

        tvResend.setOnClickListener {
            Toast.makeText(this, "OTP Resent", Toast.LENGTH_SHORT).show()
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

    private fun setupOtpInputs(otpFields: List<EditText>) {

        for (i in otpFields.indices) {

            val current = otpFields[i]

            // 👉 Move forward when typing
            current.addTextChangedListener(object : android.text.TextWatcher {
                override fun afterTextChanged(s: android.text.Editable?) {
                    if (s?.length == 1 && i < otpFields.size - 1) {
                        otpFields[i + 1].requestFocus()
                    }
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            // 👉 Move backward on delete
            current.setOnKeyListener { _, keyCode, event ->
                if (keyCode == android.view.KeyEvent.KEYCODE_DEL &&
                    event.action == android.view.KeyEvent.ACTION_DOWN &&
                    current.text.isEmpty() &&
                    i > 0
                ) {
                    otpFields[i - 1].requestFocus()
                    otpFields[i - 1].setSelection(otpFields[i - 1].text.length)
                }
                false
            }
        }
    }
}