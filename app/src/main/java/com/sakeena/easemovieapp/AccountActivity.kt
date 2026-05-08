package com.sakeena.easemovieapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import de.hdodenhof.circleimageview.CircleImageView

class AccountActivity : AppCompatActivity() {

    private lateinit var profileImage: CircleImageView
    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvPhone: TextView

    lateinit var etNickname: TextInputEditText
    lateinit var etExtraEmail: TextInputEditText
    lateinit var etDOB: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        profileImage = findViewById(R.id.profileImage)
        tvName = findViewById(R.id.tvName)
        tvEmail = findViewById(R.id.tvEmail)
        tvPhone = findViewById(R.id.tvPhone)
        etNickname = findViewById(R.id.etNickname)
        etExtraEmail = findViewById(R.id.etExtraEmail)
        etDOB = findViewById(R.id.etDOB)

        loadData()
    }

    private fun loadData() {
        val prefs = getSharedPreferences("UserProfile", MODE_PRIVATE)

        val name = prefs.getString("name", "No Name")
        val email = prefs.getString("email", "No Email")
        val phone = prefs.getString("phone", "No Phone")
        val image = prefs.getString("image", null)

        // 🔹 DETAILS CARD
        findViewById<TextView>(R.id.tvFullName).text = name
        findViewById<TextView>(R.id.tvEmailDetail).text = email
        findViewById<TextView>(R.id.tvPhone).text = phone


        etNickname.setText(prefs.getString("nickname", ""))
        etExtraEmail.setText(prefs.getString("extraEmail", ""))
        etDOB.setText(prefs.getString("dob", ""))


        tvName.text = name
        tvEmail.text = email
        tvPhone.text = phone

        if (!image.isNullOrEmpty()) {
            try {
                val uri = Uri.parse(image)
                profileImage.setImageURI(uri)
            } catch (e: Exception) {
                profileImage.setImageResource(R.drawable.ic_person_24)
            }
        }
        findViewById<MaterialButton>(R.id.btnEdit).setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }
        findViewById<MaterialButton>(R.id.btnSaveExtra).setOnClickListener {
            saveExtraInfo()
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
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

    private fun saveExtraInfo() {
        val prefs = getSharedPreferences("UserProfile", MODE_PRIVATE)

        prefs.edit()
            .putString("nickname", etNickname.text.toString())
            .putString("extraEmail", etExtraEmail.text.toString())
            .putString("dob", etDOB.text.toString())
            .apply()
    }

    override fun onResume() {
        super.onResume()
        loadData() // 🔥 auto refresh jab screen open ho
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
