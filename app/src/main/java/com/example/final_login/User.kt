package com.example.final_login

import android.content.Context
import android.widget.ExpandableListView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.time.Instant

class User{

    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val databaseReference = firebaseDatabase.reference.child("users")
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val security = Security()

    fun updateProfileInDatabase(name:String, value:String){
        if(isUserLoggedIn()){
            val userRef = databaseReference.child(security.enc(firebaseAuth.currentUser!!.email!!))
            val sectionToUpdate = userRef.child(name)
            sectionToUpdate.setValue(security.enc(value))
                .addOnSuccessListener {
                    println("Profile Updated")
                }
                .addOnFailureListener {
                    println("Profile Update Failed")
                }
        } else {
            TODO("RETURN TO LOGIN")
        }
    }

    fun getDashboard(adapter: MyAdapter){
        if(isUserLoggedIn()){
            val userRef = databaseReference.child(security.enc(firebaseAuth.currentUser!!.uid))
            val dashboardRef = userRef.child(security.enc("dashboard"))
            dashboardRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val dashboardData = dataSnapshot.value
                    adapter.data = createDashboard(dashboardData)
                    adapter.filterData("")
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    println("Error: ${databaseError.message}")
                }
            })
        } else {
            TODO("RETURN TO LOGIN")
        }
    }

    fun createDashboard(dashboard: Any?): MutableList<SensorData> {
        val returnList = mutableListOf<SensorData>()
        if (dashboard is Iterable<*>) {
            for (item in dashboard) {
                val decryptedString = security.dec(item?.toString())
                returnList.add(SensorData(decryptedString, if(decryptedString == "Pulse") R.drawable.pulse else if (decryptedString == "Steps") R.drawable.steps else R.drawable.calories, "0"))
            }
        }
        return returnList
    }

    fun sendDashboardToDatabase(dashboard: MutableList<SensorData>){
        if(isUserLoggedIn()){
            val userRef = databaseReference.child(security.enc(firebaseAuth.currentUser!!.uid))
            val dashboardRef = userRef.child(security.enc("dashboard"))
            val hMap = HashMap<String, String>()
            for(item in dashboard){
                hMap[dashboard.indexOf(item).toString()] = security.enc(item.name)
            }
            dashboardRef.setValue(hMap)
                .addOnSuccessListener {
                    println("Dashboard Saved")
                }
                .addOnFailureListener {
                    println("Dashboard Failed")
                }
        } else {
            TODO("RETURN TO LOGIN")
        }
    }

    fun sendStepsToDatabase(timeWoke: Instant, total:Int, lastMovement:Instant){
        if(isUserLoggedIn()){
            val userRef = databaseReference.child(security.enc(firebaseAuth.currentUser!!.uid))
            val stepsRef = userRef.child("health").child("steps")
            val hMap = HashMap<String, String>()
            hMap["total"] = total.toString()
            hMap["timeWoke"] = timeWoke.toString()
            hMap["lastMovement"] = lastMovement.toString()
            stepsRef.setValue(hMap)
                .addOnSuccessListener {
                    println("Steps Saved")
                }
                .addOnFailureListener {
                    println("Steps Failed To Save")
                }
        } else {
            TODO("RETURN TO LOGIN")
        }
    }

    fun readStepsFromDatabase(user:String, myAdapter: MyAdapter){
        // Take what object in as param and then assign totalStepsData to the text value
        if(isUserLoggedIn()){
            val userRef = databaseReference.child(security.enc(firebaseAuth.currentUser!!.uid))
            val totalSteps = userRef.child("health").child("steps").child("total")
            val lastMovement = userRef.child("health").child("steps").child("lastMovement")
            totalSteps.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val totalStepsData = dataSnapshot.value
                    if(myAdapter.data.filter{it.name == "Steps"}.isNotEmpty()){
                        myAdapter.data.filter{it.name == "Steps"}[0].stat = totalStepsData.toString()
                        myAdapter.filterData("")
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    println("Error: ${databaseError.message}")
                }
            })
            lastMovement.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val lastMoved = dataSnapshot.value
                    println("Last Movement Detected From Database $lastMoved")
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    println("Error: ${databaseError.message}")
                }
            })

        } else {
            TODO("RETURN TO LOGIN")
        }
    }

    fun sendHeartRateAggregateToDatabase(min:Long, max:Long, avg:Long){
        if(isUserLoggedIn()){
            val userRef = databaseReference.child(security.enc(firebaseAuth.currentUser!!.uid))
            val stepsRef = userRef.child("health").child("heart").child("aggregate")
            val hMap = HashMap<String, String>()
            hMap["min"] = min.toString()
            hMap["max"] = max.toString()
            hMap["avg"] = avg.toString()
            stepsRef.setValue(hMap)
                .addOnSuccessListener {
                    println("Heart Rate Saved")
                }
                .addOnFailureListener {
                    println("Heart Rate Failed To Save")
                }
        } else {
            TODO("RETURN TO LOGIN")
        }
    }


    fun sendHeartRateToDatabase(avgBPM: Int, mostRecent: Long, lastTaken:Instant){
        if(isUserLoggedIn()){
            val userRef = databaseReference.child(security.enc(firebaseAuth.currentUser!!.email!!))
            val stepsRef = userRef.child("health").child("heart").child("now")
            val hMap = HashMap<String, String>()
            hMap["avg"] = avgBPM.toString()
            hMap["mostRecent"] = mostRecent.toString()
            hMap["lastTaken"] = lastTaken.toString()
            stepsRef.setValue(hMap)
                .addOnSuccessListener {
                    println("Heart Rate Saved")
                }
                .addOnFailureListener {
                    println("Heart Rate Failed To Save")
                }
        } else {
            TODO("RETURN TO LOGIN")
        }
    }

    fun readHeartRateFromDatabase(user:String, myAdapter: MyAdapter){
        // Pass in sensor
        if(isUserLoggedIn()){
            val userRef = databaseReference.child(security.enc(user))
            val avgBPM = userRef.child("health").child("heart").child("avg")
            avgBPM.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val avg = dataSnapshot.value
                    println("Average BPM From Database ${avg}bpm")
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    println("Error: ${databaseError.message}")
                }
            })

            val mostRecent = userRef.child("health").child("heart").child("now").child("mostRecent")
            mostRecent.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val mostRecentBpm = dataSnapshot.value
                    if(myAdapter.data.filter{it.name == "Pulse"}.isNotEmpty()) {
                        myAdapter.data.filter { it.name == "Pulse" }[0].stat = "${mostRecentBpm}bpm"
                        println("Most Recent BPM From Database ${mostRecentBpm}bpm")
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    println("Error: ${databaseError.message}")
                }
            })
        } else {
            TODO("RETURN TO LOGIN")
        }
    }


    fun checkUserExists(email: String, callback: (Boolean) -> Unit) {
        // You have to add the callback function such as
        //  checkUserExists(emailAddress) { userExists ->
        //      if(userExists){ }
        //          else{ }
        //  }
        databaseReference.child(security.enc(email)).get().addOnSuccessListener { dataSnapshot ->
            val exists = dataSnapshot.exists() && dataSnapshot.hasChildren()
            callback(exists)
        }.addOnFailureListener { exception ->
            println("Firebase Error getting data: $exception")
            callback(false)
        }
    }

    fun isUserLoggedIn(): Boolean {
        val currentUser = firebaseAuth.currentUser
        return currentUser != null
    }

    fun sendResetPasswordEmail(context: Context, emailAddress:String){
        val firebaseAuth = FirebaseAuth.getInstance()
        checkUserExists(emailAddress) { userExists ->
            if(userExists){
                firebaseAuth.sendPasswordResetEmail(emailAddress)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(context, "Email Sent.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { exception ->
                        println("${exception.message}")
                    }
            }
            else {
                Toast.makeText(context, "No User Found.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun deleteAccount(){
        val currentUser = firebaseAuth.currentUser
        currentUser?.delete()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                println("User account deleted.")
            }
        }
    }

    fun signOut(){
        firebaseAuth.signOut()
    }

    fun dbCreateUser(email: String, firstname: String, surname: String):Boolean{
        var userCreated = false
        databaseReference.orderByChild("username").equalTo(email).addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.exists()){
                    val id = security.enc(FirebaseAuth.getInstance().currentUser!!.uid)
                    val userData = UserData(security.enc(email), security.enc(firstname), security.enc(surname), security.enc(""))
                    databaseReference.child(id).setValue(userData)
                    userCreated = true
                } else {
                    userCreated = true
                }
            }
            override fun onCancelled(databaseError: DatabaseError){
                userCreated = false
            }
        })
        return userCreated
    }

    fun splitName(displayName:String): Pair<String, String> {
        var firstName = displayName
        var surname = ""
        if(displayName.contains(" ")){
            val parts = displayName.split(" ")
            firstName = parts[0]
            surname = parts.subList(1, parts.size).joinToString(" ")
        }
        return Pair(firstName, surname)
    }

    fun populateUserSettingsList(list:ExpandableListView){

    }


}