package com.sakeena.easemovieapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText

class ReportProblemActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_problem)

        val etType = findViewById<AutoCompleteTextView>(R.id.etType)
        val etTitle = findViewById<TextInputEditText>(R.id.etTitle)
        val etDesc = findViewById<TextInputEditText>(R.id.etDesc)
        val btnSubmit = findViewById<Button>(R.id.btnSubmit)
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Dropdown options
        val types = arrayOf("Bug", "Crash", "UI Issue", "Feature Request", "Other")
        val adapter = ArrayAdapter(this, R.layout.item_dropdown, types)
        etType.setAdapter(adapter)
        etType.setTextColor(getColor(android.R.color.black))

        btnSubmit.setOnClickListener {

            val type = etType.text.toString()
            val title = etTitle.text.toString()
            val desc = etDesc.text.toString()

            if (type.isEmpty() || title.isEmpty() || desc.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            sendEmail(type, title, desc)
        }

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun sendEmail(type: String, title: String, desc: String) {

        val email = "support@yourapp.com"

        val subject = "[$type] $title"

        val message = """
            Issue Type: $type
            
            Description:
            $desc
            
            ---
            App Version: 1.0
            Device: Android
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email")
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, message)
        }

        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show()
        }

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //soft ui
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        )
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        currentFocus?.let { view ->
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
            view.clearFocus()
        }
        return super.dispatchTouchEvent(ev)
    }
}