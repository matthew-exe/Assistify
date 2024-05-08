package com.example.final_login

import android.app.Service
import android.content.Context
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

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        runShecdule()
        return START_STICKY
    }

    private fun runShecdule() {
        val context = this
        timer.schedule(object : TimerTask() {
            override fun run() {
                Handler(mainLooper).post {
                    val scope1 = CoroutineScope(Dispatchers.Main)
                    scope1.launch {
                        try {
                            val healthConnectManager = HealthConnectManager(context)
                            healthConnectManager.readHeartRate()
                            println("Health Connect Ran And Retrieved The Current Heart Rate")
                        } catch (e: Exception) {
                            println("Health Connect Failed To Retrieve The Current Heart Rate")
                            println(e)
                        }
                    }
                    val scope2 = CoroutineScope(Dispatchers.Main)
                    scope2.launch {
                        try {
                            val healthConnectManager = HealthConnectManager(context)
                            healthConnectManager.aggregateHeartRate()
                            println("Health Connect Ran And Retrieved The Aggregate Heart Rate")
                        } catch (e: Exception) {
                            println("Health Connect Failed to Retrieve The Aggregate Heart Rate")
                            println(e)
                        }
                    }
                    val scope3 = CoroutineScope(Dispatchers.Main)
                    scope3.launch {
                        try {
                            val healthConnectManager = HealthConnectManager(context)
                            healthConnectManager.readStepsLast24()
                            println("Health Connect Ran And Retrieved The Step Count")
                        } catch (e: Exception) {
                            println("Health Connect Failed To Retrieve Step Count")
                            println(e)
                        }
                    }
                }
                runShecdule()
            }
        }, delay)
    }

    override fun onDestroy() {
        timer.cancel()
        super.onDestroy()
    }
}
