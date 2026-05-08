package com.sakeena.easemovieapp

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar

class FaqActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_faq)

        val btnBack = findViewById<ImageView>(R.id.btnBack)

        setupFAQ(R.id.faq1, R.id.answer1)
        setupFAQ(R.id.faq2, R.id.answer2)
        setupFAQ(R.id.faq3, R.id.answer3)

        btnBack.setOnClickListener { finish() }
    }

    private fun setupFAQ(layoutId: Int, answerId: Int) {
        val layout = findViewById<LinearLayout>(layoutId)
        val answer = findViewById<TextView>(answerId)

        layout.setOnClickListener {
            answer.visibility =
                if (answer.visibility == View.VISIBLE) View.GONE else View.VISIBLE
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