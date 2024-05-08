package com.example.final_login

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
    val emergencyNumber: String
)