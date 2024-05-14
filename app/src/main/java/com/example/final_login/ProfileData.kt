package com.example.final_login

import java.sql.Time

data class ProfileData (
    var name: String,
    val profilePicture: Int,
    var dateOfBirth: String,
    var age: String,
    var bloodType: String,
    var nhsNumber: String,

    var medConditions: List<String>,

    var emergencyContact: String,
    var emergencyRelation: String,
    var emergencyNumber: String,

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