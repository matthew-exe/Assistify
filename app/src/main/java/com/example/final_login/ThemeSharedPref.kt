package com.example.final_login

import android.content.Context
import android.content.SharedPreferences

object ThemeSharedPref {
    private const val PREF_NAME = "MyPreferences"
    private const val PREF_THEME_KEY = "theme"

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun getThemeState(context: Context): Boolean {
        return getSharedPreferences(context).getBoolean(PREF_THEME_KEY, true)
    }

    fun setThemeState(context: Context, state: Boolean) {
        getSharedPreferences(context).edit().putBoolean(PREF_THEME_KEY, state).apply()
    }
}