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
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import androidx.health.connect.client.HealthConnectClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit


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


    private var phoneNumberToDial: String? = null
    private var notificationCount: Int = 0
    private val notifications = mutableListOf<String>()


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


        val scope1 = CoroutineScope(Dispatchers.Main)
        scope1.launch {
            try {
                healthConnectManager.readStepsLast24()
            } catch (e: Exception) {
                println(e)
            }
        }

        val scope2 = CoroutineScope(Dispatchers.Main)
        scope2.launch {
            try {
                healthConnectManager.readHeartRate()
            } catch (e: Exception) {
                println(e)
            }
        }

        val scope3 = CoroutineScope(Dispatchers.Main)
        scope3.launch {
            try {
                healthConnectManager.aggregateHeartRate()
            } catch (e: Exception) {
                println(e)
            }
        }

        //TODO("Uncomment to run background service")
//        val serviceIntent = Intent(this, BackgroundWorker::class.java)
//        startService(serviceIntent)


        val recyclerView: RecyclerView = findViewById(R.id.rvSensors)
        adapter = MyAdapter(this, ::generateDummySensorData)
        user.getDashboard(adapter)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)


        user.readStepsFromDatabase(security.enc(firebaseAuth.currentUser!!.uid), adapter)
        user.readHeartRateFromDatabase(security.enc(firebaseAuth.currentUser!!.uid), adapter)

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

        // Add some dummy notifications
        notifications.addAll(listOf("Notification 1", "Notification 2", "Notification 3"))
        updateNotificationCount()

        btnNotifications = findViewById(R.id.btnNotifications)
        btnNotifications.setOnClickListener {
            showNotificationsDialog()
            updateNotificationCount()
        }

        tvFirstName = findViewById(R.id.tvFirstName)
        loadUsersName()

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
        // TODO Set up real notifications and remove the dummy ones
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.notification_dialog)

        val lvNotifications = dialog.findViewById<ListView>(R.id.lvNotifications)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, notifications)
        lvNotifications.adapter = adapter

        lvNotifications.setOnItemLongClickListener { _, _, position, _ ->
            notifications.removeAt(position)
            adapter.notifyDataSetChanged()
            updateNotificationCount()
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
        println(security.enc(firebaseAuth.currentUser!!.uid!!))
        databaseReference.child(security.enc(firebaseAuth.currentUser!!.uid!!)).get()
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
}