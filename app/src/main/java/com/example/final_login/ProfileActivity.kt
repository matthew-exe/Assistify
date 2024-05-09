package com.example.final_login

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator

class ProfileActivity: AppCompatActivity() {
    private val user = User()
    private var phoneNumberToDial: String? = null

    private lateinit var bottomNavigationView: BottomNavigationView
    lateinit var adapter: ProfileAdapter
    private lateinit var viewPager: ViewPager

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
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        bottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_profile

        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, DashboardActivity::class.java)
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
    }

    fun linkUserProfile(userData: ProfileData) {
        adapter.addLinkedProfileLayout(userData)
        viewPager.adapter?.notifyDataSetChanged()
        Snackbar.make(viewPager, "Successfully linked with ${userData.name}", Snackbar.LENGTH_SHORT).show()
    }

    fun unlinkSnackBar(userDetails: ProfileData) {
        Snackbar.make(viewPager, "Unlinked from ${userDetails.name}", Snackbar.LENGTH_SHORT).show()
    }

    fun callClient() {
        phoneNumberToDial = "07450272351" // Should be connected users number in production
        if (ContextCompat.checkSelfPermission(this@ProfileActivity, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@ProfileActivity, arrayOf(Manifest.permission.CALL_PHONE), REQUEST_CALL_PHONE
            )
        } else {
            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phoneNumberToDial"))
            startActivity(intent)
        }
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

    fun showKeyEnterDialog() {
        val editText = EditText(this)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Enter Key:")
            .setView(editText)
            .setPositiveButton("OK") { _, _ ->
                val enteredKey = editText.text.toString()
                user.checkAccessIsPermittedBeforeLink(viewPager, enteredKey)
//                linkUserProfile(enteredKey)
                println("Entered string: $enteredKey")
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.show()
    }

    companion object {
        private const val REQUEST_CALL_PHONE = 1
    }
}