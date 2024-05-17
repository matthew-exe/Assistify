package com.example.final_login

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.health.connect.client.PermissionController
import androidx.viewpager.widget.ViewPager
import com.google.android.material.snackbar.Snackbar
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator

class ConfigHealthConnectActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager
    private val requestPermissionActivityContract = PermissionController.createRequestPermissionResultContract()
    private lateinit var healthConnectManager: HealthConnectManager
    private lateinit var adapter : InstructionsAdapter

    val requestPermissions = registerForActivityResult(requestPermissionActivityContract) { granted ->
        if (granted.containsAll(healthConnectManager.PERMISSIONS)) {
            val intent = Intent(this@ConfigHealthConnectActivity, DashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } else {
            val rootView = findViewById<View>(android.R.id.content)
            Snackbar.make(rootView, "Assistify requires the permissions to be granted in order to function", Snackbar.LENGTH_LONG).show()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_config_health_connect)

        healthConnectManager = HealthConnectManager(this, false)
        viewPager = findViewById(R.id.viewPager)
        adapter = InstructionsAdapter(this)
        viewPager.adapter = adapter


        val dotsIndicator = findViewById<WormDotsIndicator>(R.id.worm_dots_indicator)
        dotsIndicator.attachTo(viewPager)

    }

    override fun onResume() {
        super.onResume()
        adapter.setButtons()
    }
}