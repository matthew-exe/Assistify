package com.example.final_login

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator

class ProfileActivity: AppCompatActivity() {
    private val user = User()
    private var phoneNumberToDial: String? = null

    private lateinit var adapter: ProfileAdapter
    private lateinit var dotsIndicator: WormDotsIndicator
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        val viewPager = findViewById<ViewPager>(R.id.viewPager)

        adapter = ProfileAdapter(this)
        viewPager.adapter = adapter

        if (!user.isUserLoggedIn()) {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }

        bottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_profile

        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, Dashboard::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_profile -> {
                    val intent = Intent( this, ProfileActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_settings -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        dotsIndicator = findViewById(R.id.dots_indicator)
        dotsIndicator.visibility = View.VISIBLE
        dotsIndicator.attachTo(viewPager)
    }

    private fun linkClient() {
        val userDetails = ProfileData(
            "John Doe",
            R.drawable.yvonne,
            "30/11/1951",
            73,
            "A+",
            "4857773456",
            listOf("Hip replacement", "Arthritis"),
            "Teresa",
            "Daughter",
            "07777123456"
        )

        adapter.addLinkedProfile(userDetails)
        updateLinkedProfile(userDetails)
    }

    private fun updateLinkedProfile(userDetails: ProfileData) {
        val linearLayout = findViewById<LinearLayout>(R.id.linearLayout)
        linearLayout.setBackgroundResource(R.drawable.border)

        Toast.makeText(this, "Successfully linked with ${userDetails.name}", Toast.LENGTH_SHORT).show()

        val dob = "Date of birth: ${userDetails.dateOfBirth}"
        val age = "Age: ${userDetails.age}"
        val bloodType = "Blood type: ${userDetails.bloodType}"
        val nhsNumber = "NHS number: ${userDetails.nhsNumber}"
        var medicalConditions = "Medical conditions: "
        if (userDetails.medConditions.isNotEmpty()) {
            for (medCon in userDetails.medConditions) {
                medicalConditions += "\n \u2022 $medCon"
            }
        }
        val details = "Details:"
        val contactInfo = "Contact Information:"
        val emergencyContact = "Emergency contact: ${userDetails.emergencyContact}"
        val emergencyRelation = "Relation to client: ${userDetails.emergencyRelation}"
        val emergencyNumber = "Emergency contact number: ${userDetails.emergencyNumber}"

        // Update the UI with the user's details
        findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.profile_picture).setImageResource(userDetails.profilePicture)
        findViewById<TextView>(R.id.name_text).text = userDetails.name
        findViewById<TextView>(R.id.dob_text).text = dob
        findViewById<TextView>(R.id.age_text).text = age
        findViewById<TextView>(R.id.blood_type_text).text = bloodType
        findViewById<TextView>(R.id.nhs_number_text).text = nhsNumber
        findViewById<TextView>(R.id.medical_conditions_text).text = medicalConditions
        findViewById<TextView>(R.id.details_text).text = details
        findViewById<TextView>(R.id.info_text).text = contactInfo
        findViewById<TextView>(R.id.emergency_contact_text).text = emergencyContact
        findViewById<TextView>(R.id.emergency_relation_text).text = emergencyRelation
        findViewById<TextView>(R.id.emergency_number_text).text = emergencyNumber
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CALL_PHONE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phoneNumberToDial"))
                    startActivity(intent)
                } else {
                    val rootView = findViewById<View>(android.R.id.content)
                    Snackbar.make(rootView, "You must provide permission to make calls!", Snackbar.LENGTH_SHORT).show()
                }
                return
            }
            else -> {
                // Ignore all other requests
            }
        }
    }

    companion object {
        private const val REQUEST_CALL_PHONE = 1
    }
}