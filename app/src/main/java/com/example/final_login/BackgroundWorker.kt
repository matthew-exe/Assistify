package com.example.final_login

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask

class BackgroundWorker : Service() {

    private val timer = Timer()
    private val delay: Long = 20 * 60 * 1000 // 20 minutes in milliseconds
    private val healthConnectManager: HealthConnectManager = HealthConnectManager(this)
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        runSchedule()
        return START_STICKY
    }

    private fun runSchedule() {
        timer.schedule(object : TimerTask() {
            override fun run() {
                Handler(mainLooper).post {
                    healthConnectManager.syncHealthConnect()
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
