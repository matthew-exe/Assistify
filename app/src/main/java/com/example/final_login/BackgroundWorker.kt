package com.example.final_login

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import java.util.Timer
import java.util.TimerTask

class BackgroundWorker : Service() {

    private val timer = Timer()
    private val delay: Long = 20 * 60 * 100
    private lateinit var healthConnectManager: HealthConnectManager
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        healthConnectManager = HealthConnectManager(this, true)
        runSchedule()
        return START_STICKY
    }

    private fun runSchedule() {
        timer.schedule(object : TimerTask() {
            override fun run() {
                Handler(mainLooper).post {
                    healthConnectManager.syncHealthConnect()
                    println("Grabbing Health Data")
                }
                runSchedule()
            }
        }, delay)
    }

    override fun onDestroy() {
        timer.cancel()
        super.onDestroy()
    }
}
