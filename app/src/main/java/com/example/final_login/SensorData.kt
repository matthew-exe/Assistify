package com.example.final_login

import androidx.annotation.DrawableRes

data class SensorData(
    val name: String,
    @DrawableRes val image: Int,
    var stat: String
)