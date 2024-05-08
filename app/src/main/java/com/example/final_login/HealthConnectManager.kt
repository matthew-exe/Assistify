package com.example.final_login

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.HealthConnectClient.Companion.SDK_AVAILABLE
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.BasalMetabolicRateRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

class HealthConnectManager(private val context: Context) {

    private var healthConnectClient: HealthConnectClient = HealthConnectClient.getOrCreate(context)
    private val user = User()
    var availability = HealthConnectAvailability.NOT_SUPPORTED
        private set

    init {
        checkAvailability()
    }
    suspend fun hasAllPermissions(permissions: Set<String>): Boolean {
        return healthConnectClient.permissionController.getGrantedPermissions().containsAll(permissions)
    }


    private fun checkAvailability() {
        availability = when {
            HealthConnectClient.getSdkStatus(context) == SDK_AVAILABLE -> HealthConnectAvailability.INSTALLED
            else -> HealthConnectAvailability.NOT_INSTALLED
        }
    }


    suspend fun readStepsLast24(){
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

    suspend fun aggregateHeartRate() {
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
            println("MIN $minimumHeartRate")
            println("MAX $maximumHeartRate")
            println("AVG $avgHeartRate")
            if (minimumHeartRate != null && maximumHeartRate != null && avgHeartRate != null) {
                user.sendHeartRateAggregateToDatabase(minimumHeartRate, maximumHeartRate, avgHeartRate)
            }
        } catch (e: Exception) {
            // Run error handling here
        }
    }

    suspend fun readCaloriesLast24(){
        try {
            val timePeriod = returnTimeLast24()
            val response = healthConnectClient.readRecords(
                androidx.health.connect.client.request.ReadRecordsRequest(
                    ActiveCaloriesBurnedRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(timePeriod.first, timePeriod.second)
                )
            )
            println(response.records.size)
            for (calorie in response.records) {
                println(calorie.energy)
            }
        } catch (e: Exception) {
            println(e)
        }
    }

    suspend fun readBSM(){
        try {
            val timePeriod = returnTimeLast24()
            val response = healthConnectClient.readRecords(
                androidx.health.connect.client.request.ReadRecordsRequest(
                    BasalMetabolicRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(timePeriod.first, timePeriod.second)
                )
            )
            println(response.records.size)
            for (calorie in response.records) {
                println(calorie.basalMetabolicRate)
            }
        } catch (e: Exception) {
            println(e)
        }
    }

    suspend fun readHeartRate(){
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

}

/**
 * Health Connect requires that the underlying Health Connect APK is installed on the device.
 * [HealthConnectAvailability] represents whether this APK is indeed installed, whether it is not
 * installed but supported on the device, or whether the device is not supported (based on Android
 * version).
 */
enum class HealthConnectAvailability {
    INSTALLED,
    NOT_INSTALLED,
    NOT_SUPPORTED
}