package com.example.final_login

data class SettingsItem(
    val title: String,
    val children: List<String>? = null,
    var displayValue: String? = null
)
