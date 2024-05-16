package com.example.final_login

import android.content.Context
import android.content.pm.PackageManager
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.RespiratoryRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import kotlin.time.toKotlinDuration

class HealthConnectManager(private val context: Context) {

    private lateinit var healthConnectClient: HealthConnectClient
    private val user = User()
    var availability = HealthConnectAvailability.NOT_SUPPORTED

    val PERMISSIONS =
        setOf(
            HealthPermission.getReadPermission(HeartRateRecord::class),
            HealthPermission.getReadPermission(StepsRecord::class),
            HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
            HealthPermission.getReadPermission(RespiratoryRateRecord::class),
            HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
            HealthPermission.getReadPermission(SleepSessionRecord::class)
        )

    init {
        checkAvailability()
    }
    suspend fun hasAllPermissions(permissions: Set<String>): Boolean {
        return healthConnectClient.permissionController.getGrantedPermissions().containsAll(permissions)
    }

    fun checkAvailability() {
        val packageManager = context.packageManager
        val packageName = "com.google.android.apps.healthdata"
        availability = try {
            packageManager.getPackageInfo(packageName, 0)
            HealthConnectAvailability.INSTALLED
        } catch (e: PackageManager.NameNotFoundException) {
            HealthConnectAvailability.NOT_INSTALLED
        }
        if(availability == HealthConnectAvailability.INSTALLED){
            healthConnectClient = HealthConnectClient.getOrCreate(context)
        }
    }


    private suspend fun readStepsLast24HC(){
        try {
            val timePeriod = returnTimeLast24()
            val response = healthConnectClient.readRecords(
                androidx.health.connect.client.request.ReadRecordsRequest(
                    StepsRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(timePeriod.first, timePeriod.second)
                )
            )
            var totalSteps = 0
            for (stepRecord in response.records) {
                totalSteps += stepRecord.count.toInt()
            }
            user.sendStepsToDatabase(response.records[0].startTime, totalSteps, response.records[response.records.lastIndex].endTime)
        } catch (e: Exception) {
            println(e)
        }
    }

    private suspend fun readAggregateHeartRateHC() {
        try {
            val timePeriod = returnTimeLast24()
            val response =
                healthConnectClient.aggregate(
                    AggregateRequest(
                        setOf(HeartRateRecord.BPM_MAX, HeartRateRecord.BPM_MIN, HeartRateRecord.BPM_AVG),
                        timeRangeFilter = TimeRangeFilter.between(timePeriod.first, timePeriod.second)
                    )
                )
            val minimumHeartRate = response[HeartRateRecord.BPM_MIN]
            val maximumHeartRate = response[HeartRateRecord.BPM_MAX]
            val avgHeartRate = response[HeartRateRecord.BPM_AVG]
            if (minimumHeartRate != null && maximumHeartRate != null && avgHeartRate != null) {
                user.sendHeartRateAggregateToDatabase(minimumHeartRate, maximumHeartRate, avgHeartRate)
            }
        } catch (e: Exception) {
            // Run error handling here
        }
    }

    private suspend fun readCurrentHeartRateHC(){
        try {
            val timePeriod = returnTimeLast24()
            val response = healthConnectClient.readRecords(
                androidx.health.connect.client.request.ReadRecordsRequest(
                    HeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(timePeriod.first, timePeriod.second)
                )
            )
            var totalBPM = 0
            for (heartRate in response.records) {
                totalBPM += heartRate.samples[0].beatsPerMinute.toInt()
            }
            user.sendHeartRateToDatabase((totalBPM/response.records.size),response.records[response.records.lastIndex].samples[0].beatsPerMinute, response.records[response.records.lastIndex].samples[0].time)
//            val hazard = hazardController.heartHazardCheck((totalBPM/response.records.size),response.records[response.records.lastIndex].samples[0].beatsPerMinute, response.records[response.records.lastIndex].samples[0].time)
        } catch (e: Exception) {
            println(e)
        }
    }

    private fun returnTimeLast24():Pair<Instant, Instant>{
        val currentDate = LocalDate.now()
        val startDateTime = LocalDateTime.of(currentDate, LocalTime.MIDNIGHT.plusMinutes(1))
        val endDateTime = LocalDateTime.of(currentDate, LocalTime.MAX.truncatedTo(ChronoUnit.SECONDS))
        val startTime: Instant = startDateTime.atZone(ZoneOffset.UTC).toInstant()
        val endTime: Instant = endDateTime.atZone(ZoneOffset.UTC).toInstant()
        return Pair(startTime, endTime)
    }

    private fun returnSleepTimes(): Pair<Instant, Instant> {
        val currentDate = LocalDate.now()
        val previousDate = currentDate.minusDays(1)
        val startDateTime = LocalDateTime.of(previousDate, LocalTime.of(19, 0))
        val startTime: Instant = startDateTime.atZone(ZoneOffset.UTC).toInstant()
        val endDateTime = LocalDateTime.of(currentDate, LocalTime.NOON)
        val endTime: Instant = endDateTime.atZone(ZoneOffset.UTC).toInstant()
        return Pair(startTime, endTime)
    }

    suspend fun readRespitoryRate(){
        try {
            val timePeriod = returnTimeLast24()
            val response = healthConnectClient.readRecords(
                androidx.health.connect.client.request.ReadRecordsRequest(
                    RespiratoryRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(timePeriod.first, timePeriod.second)
                )
            )
            println("RES SIZE: ${response.records.size}")
            for (record in response.records) {
                println("RES RATE: ${record.rate}")
            }
        } catch (e: Exception) {
            println(e)
        }
    }

    private suspend fun readCaloriesLast24HC(){
        try {
            val timePeriod = returnTimeLast24()
            val response =
                healthConnectClient.aggregate(
                    AggregateRequest(
                        setOf(TotalCaloriesBurnedRecord.ENERGY_TOTAL),
                        timeRangeFilter = TimeRangeFilter.between(timePeriod.first, timePeriod.second)
                    )
                )
            val energyTotal = response[TotalCaloriesBurnedRecord.ENERGY_TOTAL]
            if (energyTotal != null){
                user.sendCaloriesToDatabase(energyTotal.inKilocalories.toString())
            } else {
                println("Calories Failed To Save")
            }
        } catch (e: Exception) {
            // Run error handling here
        }
    }

    private suspend fun readSleepLastNight(){
        try {
            val timePeriod = returnSleepTimes()
            val response =
                healthConnectClient.aggregate(
                    AggregateRequest(
                        setOf(SleepSessionRecord.SLEEP_DURATION_TOTAL),
                        timeRangeFilter = TimeRangeFilter.between(timePeriod.first, timePeriod.second)
                    )
                )
            val sleepRecordTotal = response[SleepSessionRecord.SLEEP_DURATION_TOTAL]
            if (sleepRecordTotal != null){
                user.sendSleepToDatabase(sleepRecordTotal.toKotlinDuration().toString())
            } else {
                println("No Sleep Recorded")
            }
        } catch (e: Exception) {
        }
            // Run error handling here
    }

    fun syncHealthConnect(){
        val scope1 = CoroutineScope(Dispatchers.Main)
        scope1.launch {
            try {
                readStepsLast24HC()
            } catch (e: Exception) {
                println(e)
            }
        }

        val scope2 = CoroutineScope(Dispatchers.Main)
        scope2.launch {
            try {
                readCurrentHeartRateHC()
            } catch (e: Exception) {
                println(e)
            }
        }

        val scope3 = CoroutineScope(Dispatchers.Main)
        scope3.launch {
            try {
                readAggregateHeartRateHC()
            } catch (e: Exception) {
                println(e)
            }
        }

        val scope4 = CoroutineScope(Dispatchers.Main)
        scope4.launch {
            try {
                readCaloriesLast24HC()
            } catch (e: Exception) {
                println(e)
            }
        }

        val scope5 = CoroutineScope(Dispatchers.Main)
        scope5.launch {
            try {
                readSleepLastNight()
            } catch (e: Exception) {
                println(e)
            }
        }
    }
}

enum class HealthConnectAvailability {
    INSTALLED,
    NOT_INSTALLED,
    NOT_SUPPORTED
}