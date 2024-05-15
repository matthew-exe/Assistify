package com.example.final_login

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.ncorti.slidetoact.SlideToActView
import android.Manifest
import android.app.Dialog
import android.content.res.ColorStateList
import android.content.res.Resources.Theme
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.toColor
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class DashboardActivity : AppCompatActivity() {
    private lateinit var adapter: MyAdapter
    private lateinit var editTextText: AppCompatEditText
    private lateinit var emergencyCallSlider: SlideToActView
    private lateinit var btnCallUser: ImageButton
    private lateinit var btnNotifications: ImageButton
    private lateinit var tvNotificationCount: TextView
    private lateinit var tvFirstName: TextView
    private lateinit var bottomNavigationView: BottomNavigationView

    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser

    private lateinit var  healthConnectManager: HealthConnectManager
    private val user = User()
    private val security = Security()

    private lateinit var wholePage: ConstraintLayout

    private var phoneNumberToDial: String? = null
    private var notificationCount: Int = 0
    private var notifications = mutableListOf<Pair<String, String>>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)

        if(!user.isUserLoggedIn()){
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.reference.child("users")
        firebaseAuth = FirebaseAuth.getInstance()
        currentUser = firebaseAuth.currentUser!!

        healthConnectManager = HealthConnectManager(this)
        healthConnectManager.syncHealthConnect()

        //TODO("Uncomment to run background service")
//        val serviceIntent = Intent(this, BackgroundWorker::class.java)
//        startService(serviceIntent)

        val recyclerView: RecyclerView = findViewById(R.id.rvSensors)

        val isUserDashboard = if (security.dec(user.getUserUidToLoad()) == currentUser.uid) true else false

        adapter = MyAdapter(this, ::generateDummySensorData, isUserDashboard)
        user.getDashboard(adapter)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        user.populateDashboard(adapter, user.getUserUidToLoad())

        editTextText = findViewById(R.id.editTextText)
        editTextText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filterData(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        emergencyCallSlider = findViewById(R.id.emergencyCallSlider)

        emergencyCallSlider.onSlideCompleteListener = object : SlideToActView.OnSlideCompleteListener {
            override fun onSlideComplete(view: SlideToActView) {
                phoneNumberToDial = "07450272350" // Should be 999 in production
                if (ContextCompat.checkSelfPermission(this@DashboardActivity, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this@DashboardActivity, arrayOf(Manifest.permission.CALL_PHONE), REQUEST_CALL_PHONE)
                    emergencyCallSlider.resetSlider()
                } else {
                    val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phoneNumberToDial"))
                    startActivity(intent)
                    emergencyCallSlider.resetSlider()
                }
            }
        }

        btnCallUser = findViewById(R.id.btnCallUser)

        btnCallUser.setOnClickListener {
            phoneNumberToDial = "07450272351" // Should be connected users number in production
            if (ContextCompat.checkSelfPermission(this@DashboardActivity, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this@DashboardActivity, arrayOf(Manifest.permission.CALL_PHONE), REQUEST_CALL_PHONE)
            } else {
                val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phoneNumberToDial"))
                startActivity(intent)
            }
        }

        bottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_home

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

        tvNotificationCount = findViewById(R.id.tvNotificationCount)

        user.getUserNotifications { retrievedNotifications ->
            notifications = retrievedNotifications.toMutableList()
            updateNotificationCount()
            setupNotificationsListener()
        }

        btnNotifications = findViewById(R.id.btnNotifications)
        btnNotifications.setOnClickListener {
            showNotificationsDialog()
            updateNotificationCount()
        }

        tvFirstName = findViewById(R.id.tvFirstName)
        loadUsersName()

        wholePage = findViewById(R.id.wholePage)
        setTheme()
    }

    private fun generateDummySensorData(count: Int): List<SensorData> {
        require(count >= 0) { "Count must be non-negative" }
        val dummyDataList = mutableListOf<SensorData>()
        repeat(count) {
            SensorRepository.sensorName.forEach { (sensorId, sensorName) ->
                val imageRes = SensorRepository.structures[sensorId]
                    ?: throw IllegalArgumentException("Image resource not found for sensor ID: $sensorId")
                dummyDataList.add(SensorData(sensorName, imageRes, "0"))
            }
        }
        // user.readStepsFromDatabase() // TODO("")Pass In whatever object here
        return dummyDataList
        // TODO Need to implement the logic to get the sensor data from the database / from the API
    }

    private fun showNotificationsDialog() {
        val dialog = if (!ThemeSharedPref.getThemeState(this)) {
            Dialog(this, R.style.MyDialogTheme)
        } else {
            Dialog(this)
        }
        dialog.setContentView(R.layout.notification_dialog)

        val lvNotifications = dialog.findViewById<ListView>(R.id.lvNotifications)
        val currentNotifications = notifications.map { it.second }.toMutableList()
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, currentNotifications)
        lvNotifications.adapter = adapter

        lvNotifications.setOnItemClickListener { _, _, position, _ ->
            val notificationToDeleteId = notifications[position].first
            user.deleteUserNotification(notificationToDeleteId) { success ->
                if (success) {
                    currentNotifications.removeAt(position)
                    adapter.notifyDataSetChanged()
                }
            }
            true
        }

        dialog.show()
    }


    private fun updateNotificationCount() {
        notificationCount = notifications.size
        tvNotificationCount.text = notificationCount.toString()

        if (notificationCount == 0) {
            tvNotificationCount.visibility = View.GONE
        } else {
            tvNotificationCount.visibility = View.VISIBLE
        }
    }

    private fun setupNotificationsListener() {
        val userRef = databaseReference.child(security.enc(currentUser.uid!!))
        val notificationsRef = userRef.child("notifications")

        // Add a listener to listen for changes in notifications
        notificationsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                user.getUserNotifications { retrievedNotifications ->
                    notifications = retrievedNotifications.toMutableList()
                    updateNotificationCount()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("Error getting notifications: ${databaseError.message}")
            }
        })
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

    private fun loadUsersName(){
        databaseReference.child(user.getUserUidToLoad()).get()
            .addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                    tvFirstName.text = security.dec(dataSnapshot.child("firstname").value.toString())
                } else {
                    println("firebase Error: Data not found or empty")
                }
            }.addOnFailureListener { exception ->
                println("firebase Error getting data: $exception")
            }
    }

    private fun setTheme() {
        if (!ThemeSharedPref.getThemeState(this)) {
            wholePage.setBackgroundColor(resources.getColor(R.color.accessiblePurple, null))

            editTextText.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.accessibleYellow, null))
            editTextText.setTextColor(resources.getColor(R.color.black, null))
            editTextText.setHintTextColor(resources.getColor(R.color.black, null))
            val blackMagGlass = ColorStateList.valueOf(resources.getColor(R.color.black, null))
            editTextText.setCompoundDrawableTintList(blackMagGlass)

            bottomNavigationView.setBackgroundColor(resources.getColor(R.color.accessiblePurple, null))
            bottomNavigationView.itemRippleColor = ColorStateList.valueOf(resources.getColor(R.color.accessibleYellow, null))
            bottomNavigationView.itemActiveIndicatorColor = ColorStateList.valueOf(resources.getColor(R.color.accessibleYellow, null))
            bottomNavigationView.itemTextColor = ColorStateList.valueOf(resources.getColor(R.color.black, null))
            bottomNavigationView.itemIconTintList = ColorStateList.valueOf(resources.getColor(R.color.black, null))

            emergencyCallSlider.outerColor = resources.getColor(R.color.accessibleYellow, null)
            emergencyCallSlider.textColor = resources.getColor(R.color.black, null)
            emergencyCallSlider.iconColor = resources.getColor(R.color.black, null)
        }
    }
}