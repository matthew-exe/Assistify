package com.example.final_login

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.RespiratoryRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ConfigHealthConnectActivity : AppCompatActivity() {
    private lateinit var  healthConnectManager: HealthConnectManager

    private lateinit var tvInstall: TextView
    private lateinit var btnInstall: Button

    private val requestPermissionActivityContract = PermissionController.createRequestPermissionResultContract()

    // TODO("Check For Permissions")

    private val requestPermissions = registerForActivityResult(requestPermissionActivityContract) { granted ->
        if (granted.containsAll(healthConnectManager.PERMISSIONS)) {
            val intent = Intent(this@ConfigHealthConnectActivity, DashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } else {
            //TODO("Dsplay Toast Instead of println")
            println("PLEASE GRANT HEALTH CONNECT WITH THE NECESSARY PERMISSIONS")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_config_health_connect)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.configHealthConnectView)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        tvInstall = findViewById(R.id.tvInstall)
        btnInstall = findViewById(R.id.btnInstall)
        healthConnectManager = HealthConnectManager(this)

        if(healthConnectManager.availability == HealthConnectAvailability.INSTALLED){
            val scope = CoroutineScope(Dispatchers.Main)
            scope.launch {
                try {
                    if(healthConnectManager.hasAllPermissions(healthConnectManager.PERMISSIONS)){
                        requestPermissions.launch(healthConnectManager.PERMISSIONS)
                    } else {
                        addSyncHealthConnect()
                    }
                } catch (e: Exception) {
                    println(e)
                }
            }
        } else {
            if(healthConnectManager.availability == HealthConnectAvailability.NOT_SUPPORTED){
                addNotSupportedMessage()
            } else if(healthConnectManager.availability == HealthConnectAvailability.NOT_INSTALLED){
                addPleaseInstallButton()
            }
        }
    }

    private fun addSyncHealthConnect(){
        tvInstall.text = "Please Sync Health Connect"
        btnInstall.text = "Sync"
        btnInstall.setOnClickListener {
            requestPermissions.launch(healthConnectManager.PERMISSIONS)
        }

    }

    private fun addNotSupportedMessage(){
        tvInstall.text = "The Current Minimum Version is Android 33, Please Upgrade Your Device"
        btnInstall.isEnabled = false
    }

    private fun addPleaseInstallButton(){
        // TODO("CREATE A NICER PAGE JUST NEEDS TEXT AND BUTTON")
        btnInstall.setOnClickListener {
            val uriString = ("market://details?id=com.google.android.apps.healthdata")
            this.startActivity(
                Intent(Intent.ACTION_VIEW).apply {
                    setPackage("com.android.vending")
                    data = Uri.parse(uriString)
                    putExtra("overlay", true)
                    putExtra("callerId", "com.example.final_login")
                })
            //TODO("Return and Refresh So It can Request Permissions Instead of Having To Force Close App and Restart")
        }
    }
}