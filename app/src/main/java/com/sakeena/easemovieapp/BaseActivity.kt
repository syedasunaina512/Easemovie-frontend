package com.sakeena.easemovieapp

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        val prefs = getSharedPreferences("AppSettings", MODE_PRIVATE)

        // 🌙 Dark Mode
        val darkMode = prefs.getBoolean("darkMode", false)
        AppCompatDelegate.setDefaultNightMode(
            if (darkMode)
                AppCompatDelegate.MODE_NIGHT_YES
            else
                AppCompatDelegate.MODE_NIGHT_NO
        )

        // 🎨 Theme Color
        when (prefs.getString("theme", "purple")) {
            "blue" -> setTheme(R.style.Theme_EaseMovieApp_Blue)
            "green" -> setTheme(R.style.Theme_EaseMovieApp_Green)
            else -> setTheme(R.style.Theme_EaseMovieApp)
        }

        super.onCreate(savedInstanceState)
    }

    override fun attachBaseContext(newBase: Context) {
        val lang = LocaleHelper.getSavedLanguage(newBase)
        val context = LocaleHelper.setLocale(newBase, lang)
        super.attachBaseContext(context)
    }
}