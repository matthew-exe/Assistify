package com.example.final_login
import android.content.Context
import android.widget.ExpandableListView
import android.widget.Toast
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class User{

    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val databaseReference = firebaseDatabase.reference.child("users")
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val security = Security()

    fun getDashboard(adapter: MyAdapter){
        if(isUserLoggedIn()){
            val userRef = databaseReference.child(security.enc(firebaseAuth.currentUser!!.uid!!))
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

    fun populateDashboard(adapter: MyAdapter){
        readStepsFromDatabase(security.enc(firebaseAuth.currentUser!!.uid), adapter)
        readHeartRateFromDatabase(security.enc(firebaseAuth.currentUser!!.uid), adapter)
        readCaloriesFromDatabase(adapter)
        readSleepFromDatabase(adapter)
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
            val userRef = databaseReference.child(security.enc(firebaseAuth.currentUser!!.uid!!))
            val dashboardRef = userRef.child(security.enc("dashboard"))
            val x = HashMap<String, String>()
            for(item in dashboard){
                x[dashboard.indexOf(item).toString()] = security.enc(item.name)
            }
            dashboardRef.setValue(x)
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

    fun checkUserExists(email: String, callback: (Boolean) -> Unit) {
        databaseReference.orderByChild("email").equalTo(security.enc(email)).addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val exists = dataSnapshot.exists() && dataSnapshot.hasChildren()
                callback(exists)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("Firebase Error getting data: $databaseError")
                callback(false)
            }
        })
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

    fun updateEmail(newEmail: String, callback: (Boolean) -> Unit) {
        val currentUser = firebaseAuth.currentUser
        currentUser?.verifyBeforeUpdateEmail(newEmail)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                println("User email address verification email sent.")
                callback(true)
            } else {
                println("Failed to update user email address.")
                callback(false)
            }
        }?.addOnFailureListener { exception ->
            println("Update email failed with exception: ${exception.message}")
            callback(false)
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
        val userData = UserData(security.enc(email), security.enc(firstname), security.enc(surname), security.enc(""))
        databaseReference.child(security.enc(firebaseAuth.currentUser!!.uid!!)).setValue(userData)
        try{
            val userData = UserData(security.enc(email), security.enc(firstname), security.enc(surname), security.enc(""))
            databaseReference.child(security.enc(firebaseAuth.currentUser!!.uid)).setValue(userData)
            userCreated = true
        } catch (e:Exception){
            println(e)
        }


//        databaseReference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(object: ValueEventListener{
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                if (!dataSnapshot.exists()){
//                    val id = security.enc(FirebaseAuth.getInstance().currentUser!!.uid!!)
//
//                    databaseReference.child(id).setValue(userData)
//                    userCreated = true
//                } else {
//                    userCreated = true
//                }
//            }
//            override fun onCancelled(databaseError: DatabaseError){
//                userCreated = false
//            }
//        })
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

    fun addUserNotification(notification: String) {
        val userRef = databaseReference.child(security.enc(firebaseAuth.currentUser!!.uid!!))
        val notificationsRef = userRef.child("notifications")
        val newNotificationRef = notificationsRef.push()
        val notificationId = newNotificationRef.key // Get the unique ID generated by Firebase
        if (notificationId != null) {
            val notificationData = mapOf("text" to security.enc(notification)) // Store notification text with key "text"
            notificationsRef.child(notificationId).setValue(notificationData)
                .addOnSuccessListener {
                    println("Notification added successfully")
                }
                .addOnFailureListener {
                    println("Failed to add notification: ${it.message}")
                }
        } else {
            println("Failed to get notification ID")
        }
    }

    fun deleteUserNotification(notificationId: String, callback: (Boolean) -> Unit) {
        val userRef = databaseReference.child(security.enc(firebaseAuth.currentUser!!.uid!!))
        val notificationsRef = userRef.child("notifications")
        notificationsRef.child(notificationId).removeValue()
            .addOnSuccessListener {
                println("Notification deleted successfully")
                callback(true)
            }
            .addOnFailureListener {
                println("Failed to delete notification: ${it.message}")
                callback(false)
            }
    }

    fun getUserNotifications(callback: (List<Pair<String, String>>) -> Unit) {
        val userRef = databaseReference.child(security.enc(firebaseAuth.currentUser!!.uid!!))
        val notificationsRef = userRef.child("notifications")
        notificationsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val notificationsList = mutableListOf<Pair<String, String>>()
                for (notificationSnapshot in dataSnapshot.children) {
                    val notificationId = notificationSnapshot.key // Get the unique ID of the notification
                    val encryptedNotification = notificationSnapshot.child("text").getValue(String::class.java)
                    if (notificationId != null && encryptedNotification != null) {
                        val decryptedNotification = security.dec(encryptedNotification)
                        notificationsList.add(Pair(notificationId, decryptedNotification))
                    }
                }
                callback(notificationsList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("Error getting notifications: ${databaseError.message}")
                callback(emptyList())
            }
        })
    }

    fun populateUserSettingsList(list:ExpandableListView){

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
//                    println("Last Movement Detected From Database $lastMoved")
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

    fun sendHeartRateToDatabase(avgBPM: Int, mostRecent: Long, lastTaken: Instant){
        if(isUserLoggedIn()){
            val userRef = databaseReference.child(security.enc(firebaseAuth.currentUser!!.uid))
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

    private fun formatCaloriesString(calString:String): String {
        val floatValue = calString.toFloat() // Convert string to float
        val roundedValue = Math.round(floatValue) // Round the float to the nearest whole number
        return roundedValue.toString()
    }

    fun sendCaloriesToDatabase(totalEnergy: String){
        if(isUserLoggedIn()){
            println(formatCaloriesString(totalEnergy))
            val userRef = databaseReference.child(security.enc(firebaseAuth.currentUser!!.uid))
            val last24Ref = userRef.child("health").child("calories").child("last24")
            last24Ref.setValue(formatCaloriesString(totalEnergy))
                .addOnSuccessListener {
                    println("Calories Saved")
                }
                .addOnFailureListener {
                    println("Calories Failed To Save")
                }
        } else {
            TODO("RETURN TO LOGIN")
        }
    }

    fun sendSleepToDatabase(totalTime: String){
        if(isUserLoggedIn()){
            val userRef = databaseReference.child(security.enc(firebaseAuth.currentUser!!.uid))
            val lastSleepRef = userRef.child("health").child("sleep").child("mostRecent")
            lastSleepRef.setValue(totalTime)
                .addOnSuccessListener {
                    println("Sleep Saved To Database")
                }
                .addOnFailureListener {
                    println("Sleep Failed To Saved To Database")
                }
        } else {
            TODO("RETURN TO LOGIN")
        }
    }

    fun readSleepFromDatabase(myAdapter: MyAdapter){
        if(isUserLoggedIn()) {
            val userRef = databaseReference.child(security.enc(firebaseAuth.currentUser!!.uid))
            val mostRecentRef = userRef.child("health").child("sleep").child("mostRecent")
            mostRecentRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val sleepTotal = dataSnapshot.value.toString()
                    if(myAdapter.data.filter{it.name == "Sleep"}.isNotEmpty()){
                        myAdapter.data.filter{it.name == "Sleep"}[0].stat = sleepTotal
                        myAdapter.filterData("")
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    println("Error: ${databaseError.message}")
                }
            })
        }
    }

    fun readCaloriesFromDatabase(myAdapter: MyAdapter){
        if(isUserLoggedIn()) {
            val userRef = databaseReference.child(security.enc(firebaseAuth.currentUser!!.uid))
            val avgBPM = userRef.child("health").child("calories").child("last24")
            avgBPM.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val calories = dataSnapshot.value.toString()
                    if(myAdapter.data.filter{it.name == "Calories"}.isNotEmpty()){
                        myAdapter.data.filter{it.name == "Calories"}[0].stat = formatCaloriesString(calories)
                        myAdapter.filterData("")
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    println("Error: ${databaseError.message}")
                }
            })
        }
    }

    fun readHeartRateFromDatabase(user:String, myAdapter: MyAdapter){
        if(isUserLoggedIn()){
            val userRef = databaseReference.child(user)
            val avgBPM = userRef.child("health").child("heart").child("avg")
            avgBPM.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val avg = dataSnapshot.value
//                    println("Average BPM From Database ${avg}bpm")
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    println("Error: ${databaseError.message}")
                }
            })

            val mostRecent = userRef.child("health").child("heart").child("now").child("mostRecent")
            mostRecent.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val mostRecentBpm = dataSnapshot.value
                    if(myAdapter.data.filter{it.name == "Pulse"}.size > 0) {
                        myAdapter.data.filter { it.name == "Pulse" }[0].stat = "${mostRecentBpm}bpm"
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

    fun generateSecureLinkKey(): String {
        return security.enc(firebaseAuth.currentUser!!.uid)
    }

    fun setToAccessPermitted(){
        if (isUserLoggedIn()) {
            val monitoredRef = databaseReference.child(security.enc(firebaseAuth.currentUser!!.uid))
            val updateMap = HashMap<String, Any>()
            updateMap["accessPermitted"] = "true"
            monitoredRef.updateChildren(updateMap)
        }  else {
            //TODO("Return to login")
        }
    }

    fun setGuardianAccount(secureKey: String, view: ViewPager){
        val guardianRef = databaseReference.child(security.enc(firebaseAuth.currentUser!!.uid))
        val updateMap = HashMap<String, Any>()
        updateMap["children"] = secureKey
        guardianRef.updateChildren(updateMap)
    }

    fun removeChild(){
        val guardianRef = databaseReference.child(security.enc(firebaseAuth.currentUser!!.uid))
        guardianRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()){
                    if(dataSnapshot.hasChild("children")){
                        val childrenRef = dataSnapshot.child("children")
                        setChildAccessPermittedToFalse(childrenRef.getValue(true).toString())
                        childrenRef.ref.removeValue()
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    fun setChildAccessPermittedToFalse(secureKey: String){
        val monitoredRef = databaseReference.child(secureKey)
        monitoredRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()){
                    if(dataSnapshot.hasChild("accessPermitted")){
                        val accessRef = dataSnapshot.child("accessPermitted")
                        accessRef.ref.setValue("false")
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    fun checkAccessIsPermittedBeforeLink(view: ViewPager, secureKey: String){
        if (isUserLoggedIn()) {
            val monitoredRef = databaseReference.child(secureKey)
            monitoredRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val accessPermitted = dataSnapshot.child("accessPermitted").getValue(String::class.java)
                        if (accessPermitted == "true") {
                            monitoredRef.child("accessPermitted").setValue(security.enc(firebaseAuth.currentUser!!.uid))
                            setGuardianAccount(secureKey, view)
                            checkAndGetChildFromDatabase(view.context, view.adapter as ProfileAdapter)
                        } else {
                            Snackbar.make(view, "Invalid Key! Please Try Again.", Snackbar.LENGTH_SHORT).show()
                        }
                    } else {
                        Snackbar.make(view, "Invalid Key!", Snackbar.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle database error
                }
            })
        } else {
            // TODO: Handle the case when the user is not logged in
        }
    }

    fun checkAndGetChildFromDatabase(context: Context, profileAdapter: ProfileAdapter) {
        val userRef = databaseReference.child(security.enc(firebaseAuth.currentUser!!.uid)).child("children")
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    println(dataSnapshot.getValue(true))
                    readProfileOfMonitored(dataSnapshot.getValue(true).toString(), context)
                } else {
                    profileAdapter.loadNoProfile()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
            }
        })
    }

    private fun linkAccount(secureKey:String) {
        if (isUserLoggedIn()) {
            val guardianRef = databaseReference.child(security.enc(firebaseAuth.currentUser!!.uid))
            val canReadRef = guardianRef.child("canRead")
            canReadRef.setValue(secureKey)

            val monitoredRef = databaseReference.child(secureKey)
            val accessPermitted = monitoredRef.child("permittedToo")
            accessPermitted.setValue(security.enc(firebaseAuth.currentUser!!.uid))
        } else {
            TODO("RETURN TO LOGIN")
        }
    }


    private fun readProfileOfMonitored(secureKey:String, context: Context){
        val monitoredRef = databaseReference.child(secureKey).child("accessPermitted")
        monitoredRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.value.toString() == security.enc(firebaseAuth.currentUser!!.uid)){
                    println("YES KEYS MATCH")
                    loadProfileOfMonitored(secureKey, context)
                } else {
                    (context as ProfileActivity).linkUserProfile(emptyUserDetails)
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                println("Error: ${databaseError.message}")
            }
        })
    }

    private fun loadProfileOfMonitored(secureKey:String, context: Context){
        val monitoredRef = databaseReference.child(secureKey)
        monitoredRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val emptyUser = emptyUserDetails.copy()
                if(dataSnapshot.hasChild("firstname")){
                    emptyUser.name = security.dec(dataSnapshot.child("firstname").value.toString())
                    println(security.dec(dataSnapshot.child("firstname").value.toString()))
                }
                if(dataSnapshot.hasChild("surname")){
                    emptyUser.name += " " + security.dec(dataSnapshot.child("surname").value.toString())
                }
                if(dataSnapshot.hasChild("health")){
                    val healthSnapShot = dataSnapshot.child("health")
                    if(healthSnapShot.hasChild("heart")){
                        val heartSnapshot = healthSnapShot.child("heart")
                        if(heartSnapshot.hasChild("aggregate")){
                            emptyUser.avgBPM = heartSnapshot.child("aggregate").child("avg").value.toString()
                            emptyUser.maxBPM = heartSnapshot.child("aggregate").child("max").value.toString()
                            emptyUser.minBPM = heartSnapshot.child("aggregate").child("min").value.toString()
                        }
                        if(healthSnapShot.hasChild("now")){
                            emptyUser.currentBPM = heartSnapshot.child("aggregate").child("mostRecent").value.toString()
                        }
                    }
                    if(healthSnapShot.hasChild("steps")){
                        val stepsSnapshot = healthSnapShot.child("steps")
                        if(stepsSnapshot.hasChild("lastMovement")){
                            emptyUser.stepsLastDetected = formatTimeString(stepsSnapshot.child("lastMovement").value.toString())
                        }
                        if(stepsSnapshot.hasChild("timeWoke")){
                            println(formatTimeString(stepsSnapshot.child("timeWoke").value.toString()))
                            emptyUser.stepsFirstDetected = formatTimeString(stepsSnapshot.child("timeWoke").value.toString())
                        }
                        if(stepsSnapshot.hasChild("total")){
                            emptyUser.steps24hTotal = stepsSnapshot.child("total").value.toString()
                        }
                    }
                    if(healthSnapShot.hasChild("calories")){
                        val calSnapshot = healthSnapShot.child("calories")
                        if(calSnapshot.hasChild("last24")){}
                        emptyUser.caloriesTotalSpent = calSnapshot.child("last24").value.toString() + "Kcal"
                    }
                    if(healthSnapShot.hasChild("sleep")){
                        val sleepSnapshot = healthSnapShot.child("sleep")
                        if(sleepSnapshot.hasChild("mostRecent")){}
                        emptyUser.sleepTotal = sleepSnapshot.child("mostRecent").value.toString()
                    }
                }
                if(dataSnapshot.hasChild("personalDetails")){
                    val detailsSnapshot = dataSnapshot.child("personalDetails")
                }
                (context as ProfileActivity).linkUserProfile(emptyUser)
            }
            override fun onCancelled(databaseError: DatabaseError) {
                println("Error: ${databaseError.message}")
            }
        })
    }

    private fun formatTimeString(timeString: String): String {
        val formatter = DateTimeFormatter.ofPattern("HH:mm EEE dd/MM/uu")
        val dateTime = LocalDateTime.parse(timeString, DateTimeFormatter.ISO_DATE_TIME)
        val formattedDateTime = dateTime.format(formatter)
        return formattedDateTime
    }


    val emptyUserDetails = ProfileData(
        "Yvonne",
        R.drawable.yvonne,
        "1951-11-30",
        73,
        "A+",
        "4857773456",
        listOf("Hip replacement", "Arthritis"),
        "Teresa",
        "Daughter",
        "07777123456",
        "75",
        "55",
        "105",
        "85",
        "1435",
        "20:45",
        "07:15",
        "578",
        "4h 34m"
        )

}