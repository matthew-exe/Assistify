package com.example.final_login

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator

class ProfileActivity: AppCompatActivity() {
    private val user = User()
    private var phoneNumberToDial: String? = null

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var adapter: ProfileAdapter
    private lateinit var viewPager: ViewPager
    private lateinit var userDetails: ProfileData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        viewPager = findViewById(R.id.viewPager)
        adapter = ProfileAdapter(this)
        viewPager.adapter = adapter

        val dotsIndicator = findViewById<WormDotsIndicator>(R.id.worm_dots_indicator)
        dotsIndicator.attachTo(viewPager)

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

//        findViewById<Button>(R.id.linkButton).setOnClickListener {
//            // Check ProfileData to see the format of data
//            val userList = arrayOf(
//                ProfileData("Yvonne", R.drawable.yvonne, "1951-11-30", 73, "A+",
//                    "4857773456", listOf("Hip replacement", "Arthritis"), "Teresa",
//                    "Daughter", "07777123456"),
//            )
//            updateUserProfile(userList[0])
//        }
//
//        findViewById<Button>(R.id.unlink_button).setOnClickListener {
//            unlinkUserProfile()
//        }
//
//        findViewById<Button>(R.id.call_button).setOnClickListener {
//            val phoneNumberToDial = "07450272352"
//
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE), REQUEST_CALL_PHONE)
//            } else {
//                val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phoneNumberToDial"))
//                startActivity(intent)
//            }
//        }
    }

    fun linkUserProfile(userDetails: ProfileData) {
        adapter.addLinkedProfileLayout(userDetails)
        viewPager.adapter?.notifyDataSetChanged()
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