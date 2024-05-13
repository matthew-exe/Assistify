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

    fun heartHazardCheck(age:Int, name:String, avgBpm:Int, mostRecent:Long, lastTaken: Instant): Pair<Boolean, String>{
        val maxRate = calcMaxHeartRate(age)
        if(mostRecent >= maxRate){
            return Pair(true, "$name: Critically High Level Heart Rate Recorded - ${mostRecent.toInt()}bpm (${returnTimeInHours(lastTaken)} ago)")
        } else if(mostRecent > 40){
            return Pair(true, "$name: Critically Low Level Heart Rate Recorded - ${mostRecent.toInt()}bpm (${returnTimeInHours(lastTaken)} ago)")
        }
        return Pair(false, "")
    }

    fun isHeartLow(){

    }

    fun isHeartHigh(){

    }

    fun hasntMoved(totalSteps:Int){
        if(totalSteps < 100){

        }
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


    private fun calcMaxHeartRate(age:Int): Int {
        // https://www.cdc.gov/physicalactivity/basics/measuring/heartrate.htm#:~:text=To%20estimate%20your%20maximum%20age,x%200.64%20%3D%20109%20bpm%2C%20and
        // To estimate your maximum age-related heart rate, subtract your age from 220. For example, for a 50-year-old person, the estimated maximum age-related heart rate would be calculated as 220 – 50 years = 170 beats per minute (bpm).
        return 220-age
    }


}