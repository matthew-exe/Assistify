package com.example.final_login

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileActivity: AppCompatActivity() {
    private val user = User()
    private var phoneNumberToDial: String? = null
    private var isAccessPermitted: String = "false"
    private var guardFullName: String = ""

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var adapter: ProfileAdapter
    private lateinit var viewPager: ViewPager

    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser

    private lateinit var wholePage: ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.reference.child("users")
        firebaseAuth = FirebaseAuth.getInstance()
        currentUser = firebaseAuth.currentUser!!

        checkAccessPermitted { isPermitted ->
            isAccessPermitted = isPermitted
            loadGuardianFullName { name ->
                guardFullName = name
                initializeAdapter()
            }
        }

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

        wholePage = findViewById(R.id.wholePage)
        setTheme()
    }

    fun linkUserProfile(userData: ProfileData, displaySnackbar: Boolean) {
        adapter.addLinkedProfileLayout(userData)
        if(adapter.layouts.size < 3){
            adapter.loadNoProfile()
        }
        viewPager.adapter?.notifyDataSetChanged()
        if(displaySnackbar){
            Snackbar.make(viewPager, "Successfully linked with ${userData.name}", Snackbar.LENGTH_SHORT).show()
        }
    }

    fun monitorUnlinkSnackBar(userDetails: ProfileData) {
        Snackbar.make(viewPager, "Unlinked from ${userDetails.name}", Snackbar.LENGTH_SHORT).show()
    }

    fun clientUnlinkSnackBar() {
        Snackbar.make(viewPager, "Unlinked from $guardFullName", Snackbar.LENGTH_SHORT).show()
    }

    fun callClient() {
        CoroutineScope(Dispatchers.Main).launch {
            phoneNumberToDial = user.getPhoneNumberToDial()
            if (ContextCompat.checkSelfPermission(this@ProfileActivity, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this@ProfileActivity, arrayOf(Manifest.permission.CALL_PHONE), REQUEST_CALL_PHONE)
            } else {
                val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phoneNumberToDial"))
                startActivity(intent)
            }
        }
    }

    private fun checkAccessPermitted(callback: (String) -> Unit) {
        val firebaseDatabase = FirebaseDatabase.getInstance()
        val databaseReference = firebaseDatabase.reference.child("users")
        val firebaseAuth = FirebaseAuth.getInstance()
        val security = Security()

        databaseReference.child(security.enc(firebaseAuth.currentUser!!.uid)).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val checkAP = snapshot.child("accessPermitted").value.toString()
                callback(checkAP)
            }

            override fun onCancelled(error: DatabaseError) {
                println("Error: $error")
            }
        })
    }

    private fun loadGuardianFullName(callback: (String) -> Unit) {
        val security = Security()
        var fullName = ""

        databaseReference.child(isAccessPermitted).get()
            .addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                    fullName = security.dec(dataSnapshot.child("firstname").value.toString())
                    fullName += " "
                    fullName += security.dec(dataSnapshot.child("surname").value.toString())
                    callback(fullName)
                } else {
                    println("firebase Error: Data not found or empty")
                    callback(fullName)
                }
            }.addOnFailureListener { exception ->
                println("firebase Error getting data: $exception")
            }
    }

    private fun initializeAdapter() {
        viewPager = findViewById(R.id.viewPager)
        adapter = ProfileAdapter(this, isAccessPermitted, guardFullName)
        viewPager.adapter = adapter

        val dotsIndicator = findViewById<WormDotsIndicator>(R.id.worm_dots_indicator)
        if (!ThemeSharedPref.getThemeState(this)) {
            dotsIndicator.setStrokeDotsIndicatorColor(resources.getColor(R.color.accessibleYellow, null))
            dotsIndicator.setDotIndicatorColor(resources.getColor(R.color.accessibleYellowDarker, null))
        }
        dotsIndicator.attachTo(viewPager)
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
        val dialog = if (!ThemeSharedPref.getThemeState(this)) {
            AlertDialog.Builder(this, R.style.MyDialogTheme)
        } else {
            AlertDialog.Builder(this)
        }
            .setTitle("Enter Key:")
            .setView(editText)
            .setPositiveButton("OK") { _, _ ->
                val enteredKey = editText.text.toString()
                user.checkAccessIsPermittedBeforeLink(viewPager, enteredKey, true)
                println("Entered string: $enteredKey")
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.show()
    }

    private fun setTheme() {
        if (!ThemeSharedPref.getThemeState(this)) {
            wholePage.setBackgroundColor(resources.getColor(R.color.accessiblePurple, null))

            bottomNavigationView.setBackgroundColor(resources.getColor(R.color.accessiblePurple, null))
            bottomNavigationView.itemRippleColor = ColorStateList.valueOf(resources.getColor(R.color.accessibleYellow, null))
            bottomNavigationView.itemActiveIndicatorColor = ColorStateList.valueOf(resources.getColor(R.color.accessibleYellow, null))
            bottomNavigationView.itemTextColor = ColorStateList.valueOf(resources.getColor(R.color.black, null))
            bottomNavigationView.itemIconTintList = ColorStateList.valueOf(resources.getColor(R.color.black, null))
        }
    }

    companion object {
        private const val REQUEST_CALL_PHONE = 1
    }
}