package com.example.final_login

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract.Profile
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import android.widget.ViewFlipper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

class ProfileActivity: AppCompatActivity() {
    private val user = User()
    private var phoneNumberToDial: String? = null

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        val viewFlipper = findViewById<ViewFlipper>(R.id.viewFlipper)

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

        findViewById<Button>(R.id.linkButton).setOnClickListener {
            // TODO("This is where data about the client is retrieved")
            // Check ProfileData to see the format of data
            val userList = arrayOf(
                ProfileData("Yvonne", R.drawable.yvonne, "1951-11-30", 73, "A+",
                    "4857773456", listOf("Hip replacement", "Arthritis"), "Teresa",
                    "Daughter", "07777123456"),
            )
            updateUserProfile(userList[0])

            viewFlipper.flipInterval = 2000
            viewFlipper.isAutoStart = true
            viewFlipper.startFlipping()
        }

        findViewById<Button>(R.id.unlink_button).setOnClickListener {
            unlinkUserProfile()
            viewFlipper.stopFlipping()
        }

        findViewById<Button>(R.id.call_button).setOnClickListener {
            val phoneNumberToDial = "07450272352"

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE), REQUEST_CALL_PHONE)
            } else {
                val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phoneNumberToDial"))
                startActivity(intent)
            }
        }
    }

    private fun updateUserProfile(userDetails: ProfileData) {
        findViewById<Button>(R.id.linkButton).visibility = View.GONE
        findViewById<TextView>(R.id.pre_link_text_1).visibility = View.GONE
        findViewById<TextView>(R.id.pre_link_text_2).visibility = View.GONE

        findViewById<Button>(R.id.unlink_button).visibility = View.VISIBLE
        findViewById<Button>(R.id.call_button).visibility = View.VISIBLE

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

    private fun unlinkUserProfile() {
        finish()
        startActivity(intent)

        Toast.makeText(this, "Successfully unlinked from client", Toast.LENGTH_SHORT).show()
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