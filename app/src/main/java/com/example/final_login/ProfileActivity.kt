package com.example.final_login

import android.content.Intent
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
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ProfileActivity: AppCompatActivity() {
    private val user = User()

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

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
            val userList = arrayOf(
                ProfileData("Yvonne", R.drawable.yvonne, "1951-11-30", 73, "A+",
                    "Teresa", "Daughter", "07777123456"),
            )
            updateUserProfile(userList[0])
        }
    }

    private fun updateUserProfile(userDetails: ProfileData) {
        findViewById<Button>(R.id.linkButton).visibility = View.GONE
        findViewById<TextView>(R.id.pre_link_text_1).visibility = View.GONE
        findViewById<TextView>(R.id.pre_link_text_2).visibility = View.GONE

        val linearLayout = findViewById<LinearLayout>(R.id.linearLayout)
        linearLayout.setBackgroundResource(R.drawable.border)

        Toast.makeText(this, "Successfully linked with ${userDetails.name}", Toast.LENGTH_SHORT).show()

        val dob = "Date of birth: ${userDetails.dateOfBirth}"
        val age = "Age: ${userDetails.age}"
        val bloodType = "Blood type: ${userDetails.bloodType}"
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
        findViewById<TextView>(R.id.details_text).text = details
        findViewById<TextView>(R.id.info_text).text = contactInfo
        findViewById<TextView>(R.id.emergency_contact_text).text = emergencyContact
        findViewById<TextView>(R.id.emergency_relation_text).text = emergencyRelation
        findViewById<TextView>(R.id.emergency_number_text).text = emergencyNumber
    }
}