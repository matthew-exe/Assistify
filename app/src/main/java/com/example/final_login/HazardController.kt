package com.example.final_login

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.time.Duration
import java.time.Instant

class HazardController {

    private val firebaseDatabase = FirebaseDatabase.getInstance()

    /** This is a controller that checks for potentially hazardous situations, and starts
     * the notification process of informing a guardian.
     */
//    avgBPM: Int,
//        mostRecent: Long,
//
//        lastTaken: Instant

    fun heartHazardCheck(age:Int, name:String, avgBpm:Int, mostRecent:Long, lastTaken: Instant): Pair<Boolean, String>{
        val maxRate = calcMaxRate(age)
        if(mostRecent >= maxRate){
            return Pair(true, "$name: Critically High Level Heart Rate Recorded - ${avgBpm}bpm (${returnTimeInHours(lastTaken)} ago)")
        }
        return Pair(false, "")
    }

    fun isHeartLow(){

    }

    fun isHeartHigh(){

    }

    fun hasntMoved(){

    }

    fun hasMovedMinimal(totalSteps: Int){

    }

    fun hasntWoke(){}

    private fun returnTimeInHours(lastTaken: Instant): String {
        val currentTime = Instant.now()
        val duration = Duration.between(lastTaken, currentTime)
        val hours = duration.toHours()
        val minutes = duration.toMinutes() % 60
        return "${hours}h ${minutes}m"
    }

    private fun returnMaxRate(age:Int):Int{
        //    https://www.heart.org/en/healthy-living/fitness/fitness-basics/target-heart-rates
        return when (age) {
            in 0..25 -> 200
            in 26..32 -> 190
            in 33..35 -> 185
            in 36..42 -> 180
            in 43..48 -> 175
            in 49..52 -> 170
            in 53..58 -> 165
            in 59..62 -> 160
            in 63..68 -> 155
            else ->  200
        }
    }

    private fun calcMaxRate(age:Int): Int {
        // https://www.cdc.gov/physicalactivity/basics/measuring/heartrate.htm#:~:text=To%20estimate%20your%20maximum%20age,x%200.64%20%3D%20109%20bpm%2C%20and
        // To estimate your maximum age-related heart rate, subtract your age from 220. For example, for a 50-year-old person, the estimated maximum age-related heart rate would be calculated as 220 – 50 years = 170 beats per minute (bpm).
        return 220-age
    }


}