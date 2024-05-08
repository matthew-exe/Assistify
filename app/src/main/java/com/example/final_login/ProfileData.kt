package com.example.final_login

import java.sql.Time

data class ProfileData (
    val name: String,
    val profilePicture: Int,
    val dateOfBirth: String,
    val age: Int,
    val bloodType: String,
    val nhsNumber: String,

    val medConditions: List<String>,

    val emergencyContact: String,
    val emergencyRelation: String,
    val emergencyNumber: String,

    val currentBPM: Int,
    val minBPM: Int,
    val maxBPM: Int,
    val avgBPM: Int,

    val steps24hTotal: Int,
    val stepsLastDetected: String,
    val stepsFirstDetected: String,

    val caloriesTotalSpent: Int
)