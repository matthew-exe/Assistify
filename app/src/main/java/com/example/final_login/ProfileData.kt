package com.example.final_login

import java.sql.Time

data class ProfileData (
    var name: String,
    val profilePicture: Int,
    val dateOfBirth: String,
    val age: Int,
    val bloodType: String,
    val nhsNumber: String,

    val medConditions: List<String>,

    val emergencyContact: String,
    val emergencyRelation: String,
    val emergencyNumber: String,

    var currentBPM: String,
    var minBPM: String,
    var maxBPM: String,
    var avgBPM: String,

    var steps24hTotal: String,
    var stepsLastDetected: String,
    var stepsFirstDetected: String,

    var caloriesTotalSpent: String,

    var sleepTotal: String
)