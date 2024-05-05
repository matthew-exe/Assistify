package com.example.final_login
import android.content.Context
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class User{

    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val databaseReference = firebaseDatabase.reference.child("users")
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val security = Security()

    fun getDashboard(adapter: MyAdapter){
        if(isUserLoggedIn()){
            val userRef = databaseReference.child(security.enc(firebaseAuth.currentUser!!.email!!))
            val dashboardRef = userRef.child("dashboard")
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
                returnList.add(SensorData(item.toString(), if (item.toString() == "Pulse") R.drawable.pulse else if (item.toString() == "Steps") R.drawable.steps else R.drawable.calories))
            }
        }
        return returnList
    }

    fun sendDashboardToDatabase(dashboard: MutableList<SensorData>){
        if(isUserLoggedIn()){
            val userRef = databaseReference.child(security.enc(firebaseAuth.currentUser!!.email!!))
            val dashboardRef = userRef.child("dashboard")
            val x = HashMap<String, String>()
            for(item in dashboard){
                x[dashboard.indexOf(item).toString()] = item.name
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
                    val id = databaseReference.child(security.enc(email)).key!!
                    val userData = UserData(security.enc(email), security.enc(firstname), security.enc(surname))
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
}