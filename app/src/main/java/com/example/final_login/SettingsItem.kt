package com.example.final_login

data class SettingsItem(
    val title: String,
    var children: List<String>? = null,
    var displayValue: String? = null,
    var personalDetails: PersonalDetails? = null
)