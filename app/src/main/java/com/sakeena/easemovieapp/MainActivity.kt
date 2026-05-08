package com.sakeena.easemovieapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Redirect to HomeActivity as Login/Onboarding is already handled
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}
