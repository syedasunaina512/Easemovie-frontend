package com.sakeena.easemovieapp

import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class FullScreenImageActivity : AppCompatActivity() {

    private var startY = 0f
    private var isDragging = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN

        setContentView(R.layout.activity_full_screen_image)

        val imageView = findViewById<com.github.chrisbanes.photoview.PhotoView>(R.id.fullImage)
        val closeBtn = findViewById<ImageView>(R.id.btnClose)

        val uri = intent.getStringExtra("imageUri")

        if (uri != null) {
            imageView.setImageURI(Uri.parse(uri))
        }

        // ❌ Close button
        closeBtn.setOnClickListener {
            finish()
        }

        // 🔥 SWIPE DOWN ON IMAGE (WORKING)
        imageView.setOnTouchListener { v, event ->

            when (event.action) {

                MotionEvent.ACTION_DOWN -> {
                    startY = event.rawY
                    isDragging = false
                }

                MotionEvent.ACTION_MOVE -> {
                    val diffY = event.rawY - startY

                    // Only detect downward swipe
                    if (diffY > 50) {
                        isDragging = true
                        v.translationY = diffY
                        v.alpha = 1 - (diffY / 1000)
                    }
                }

                MotionEvent.ACTION_UP -> {
                    val diffY = event.rawY - startY

                    if (isDragging && diffY > 300) {
                        finish() // 🔥 close screen
                    } else {
                        v.animate()
                            .translationY(0f)
                            .alpha(1f)
                            .setDuration(200)
                            .start()
                    }

                    v.performClick() // ✅ FIX WARNING
                }
            }

            true
        }
    }
}