package com.sakeena.easemovieapp

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import java.util.*

object LocaleHelper {

    private const val PREF_NAME = "app_prefs"
    private const val KEY_LANG = "language"

    fun setLocale(context: Context, langCode: String): Context {
        saveLanguage(context, langCode)
        return updateResources(context, langCode)
    }

    private fun updateResources(context: Context, langCode: String): Context {
        val locale = Locale(langCode)
        Locale.setDefault(locale)

        val res = context.resources
        val config = Configuration(res.configuration)
        
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        return context.createConfigurationContext(config)
    }

    fun getSavedLanguage(context: Context): String {
        val prefs: SharedPreferences =
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANG, "en") ?: "en"
    }

    private fun saveLanguage(context: Context, langCode: String) {
        val prefs: SharedPreferences =
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANG, langCode).apply()
    }
}
