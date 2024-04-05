package com.example.final_login
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
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

    @RequiresApi(Build.VERSION_CODES.O)
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

    @RequiresApi(Build.VERSION_CODES.O)
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

    fun dbCreateUser(email: String, firstname: String, surname: String):Boolean{
        var userCreated = false
        databaseReference.orderByChild("username").equalTo(email).addListenerForSingleValueEvent(object: ValueEventListener{
            @RequiresApi(Build.VERSION_CODES.O)
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



}