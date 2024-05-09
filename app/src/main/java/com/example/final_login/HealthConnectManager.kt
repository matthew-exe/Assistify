package com.example.final_login

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.HealthConnectClient.Companion.SDK_AVAILABLE
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.RespiratoryRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import kotlin.time.toKotlinDuration

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


    suspend fun readStepsLast24HC(){
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

    suspend fun readAggregateHeartRateHC() {
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

    suspend fun readCurrentHeartRateHC(){
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

    suspend fun readCaloriesLast24(){
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
                println("NOOOOOOOOOOOOOO")
            }
        } catch (e: Exception) {
            // Run error handling here
        }
    }

    suspend fun readSleepLastNight(){
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
                println("SLEEP OKAY")
                println(sleepRecordTotal.toKotlinDuration())
//                println(sleepRecordTotal.toKotlinDuration().toComponents { hours, minutes, seconds, nanoseconds ->  })
            } else {
                println("NOOOOOOOOOOOOOO Sleeep")
            }
        } catch (e: Exception) {
            // Run error handling here
        }
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