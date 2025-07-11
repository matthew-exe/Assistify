package com.example.final_login

data class PersonalDetails (
    val age: String? = null,
    val dateOfBirth: String? = null,
    val bloodType: String? = null,
    val nhsNumber: String? = null,
    val emergencyContactName: String? = null,
    val emergencyContactRelation: String? = null,
    val emergencyContactNumber: String? = null,
    val medicalConditions: List<String>? = null,
)
