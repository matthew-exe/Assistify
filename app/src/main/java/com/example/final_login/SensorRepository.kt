package com.example.final_login
object SensorRepository {
    val sensorName = mapOf(
        SensorID.PULSE to "Pulse",
        SensorID.STEPS to "Steps",
        SensorID.CALORIES to "Calories"
    )

    val structures = mapOf(
        SensorID.PULSE to R.drawable.pulse,
        SensorID.STEPS to R.drawable.steps,
        SensorID.CALORIES to R.drawable.calories
    )
}