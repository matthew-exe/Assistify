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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class Dashboard : AppCompatActivity() {
    private lateinit var adapter: MyAdapter
    private lateinit var editTextText: AppCompatEditText
    private lateinit var emergencyCallSlider: SlideToActView
    private lateinit var btnCallUser: ImageButton
    private lateinit var btnNotifications: ImageButton
    private lateinit var tvNotificationCount: TextView
    private lateinit var tvFirstName: TextView

    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser
    private val user = User()
    private val security = Security()


    private var phoneNumberToDial: String? = null
    private var notificationCount: Int = 0
    val notifications = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)

        if(!user.isUserLoggedIn()){
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }

        val recyclerView: RecyclerView = findViewById(R.id.rvSensors)
        adapter = MyAdapter(this, ::generateDummySensorData)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.reference.child("users")
        firebaseAuth = FirebaseAuth.getInstance()

        currentUser = firebaseAuth.currentUser!!

        editTextText = findViewById(R.id.editTextText)
        editTextText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filterData(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        emergencyCallSlider = findViewById(R.id.emergencyCallSlider);


        emergencyCallSlider.onSlideCompleteListener = object : SlideToActView.OnSlideCompleteListener {
            override fun onSlideComplete(view: SlideToActView) {
                phoneNumberToDial = "07450272350" // Should be 999 in production
                if (ContextCompat.checkSelfPermission(this@Dashboard, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this@Dashboard, arrayOf(Manifest.permission.CALL_PHONE), REQUEST_CALL_PHONE)
                } else {
                    val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumberToDial))
                    startActivity(intent)
                    emergencyCallSlider.resetSlider()
                }
            }
        }

        btnCallUser = findViewById(R.id.btnCallUser)

        btnCallUser.setOnClickListener {
            phoneNumberToDial = "07450272351" // Should be connected users number in production
            if (ContextCompat.checkSelfPermission(this@Dashboard, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this@Dashboard, arrayOf(Manifest.permission.CALL_PHONE), REQUEST_CALL_PHONE)
            } else {
                val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumberToDial))
                startActivity(intent)
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
        loadUserProfile()

    }

    private fun generateDummySensorData(count: Int): List<SensorData> {
        require(count >= 0) { "Count must be non-negative" }
        val dummyDataList = mutableListOf<SensorData>()
        repeat(count) {
            SensorRepository.sensorName.forEach { (sensorId, sensorName) ->
                val imageRes = SensorRepository.structures[sensorId]
                    ?: throw IllegalArgumentException("Image resource not found for sensor ID: $sensorId")
                dummyDataList.add(SensorData(sensorName, imageRes))
            }
        }
        return dummyDataList
    }

    private fun showNotificationsDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.notification_dialog)

        val lvNotifications = dialog.findViewById<ListView>(R.id.lvNotifications)
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, notifications)
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
                    val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumberToDial))
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

    private fun loadUserProfile(){
        databaseReference.child(security.enc(currentUser.email!!)).get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                val id = dataSnapshot.child("id").getValue(String::class.java)!!
                val firstname = security.dec(dataSnapshot.child("firstname").getValue(String::class.java))
                val surname = security.dec(dataSnapshot.child("surname").getValue(String::class.java))
                setProfileData(UserData(id, firstname, surname))
            } else {
                println("firebase Error: Data not found or empty")
            }
        }.addOnFailureListener { exception ->
            println("firebase Error getting data: $exception")
        }
    }

    private fun setProfileData(usersData:UserData){
        tvFirstName.text = user.splitName(usersData.firstname).first
//        textEmailAddress.text = user.id
//        textSurname.text = user.surname
    }

}