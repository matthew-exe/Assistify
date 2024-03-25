package com.example.final_login

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class Dashboard : AppCompatActivity() {

    private lateinit var userIDText: TextView
    private lateinit var firstNameText: TextView
    private lateinit var surnameText: TextView
    private lateinit var logoutBtn: Button

    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference

    val security = Security()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.reference.child("users")

        val userId: String = intent.getStringExtra("user_id")!!
        userIDText = findViewById(R.id.uid_from_auth)
        firstNameText = findViewById(R.id.firstnameText)
        surnameText = findViewById(R.id.surnameText)

        databaseReference.child(userId).get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                val id = dataSnapshot.child("id").getValue(String::class.java)!!
                val firstname = security.dec(dataSnapshot.child("firstname").getValue(String::class.java))
                val surname = security.dec(dataSnapshot.child("surname").getValue(String::class.java))
                val user = UserData(id, firstname, surname)
                loadProfileInformation(user)
            } else {
                println("firebase Error: Data not found or empty")
            }
        }.addOnFailureListener { exception ->
            println("firebase Error getting data: $exception")
        }

        logoutBtn = findViewById(R.id.btn_logout)
        logoutBtn.setOnClickListener{
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this@Dashboard, Login::class.java))
            finish()
        }
    }

    private fun getUserObject(){
    //TODO: Return user object, need to encrypt the userId above and request from firebase and push it into UserData class
        // Already Done above just needs reformatting into function down belopw
    }

    private fun genGuardianKey(){
        // TODO: Generate a secure key that can be shared between monitor and guardian allowing the monitor to have access to guardian data
    }

    private fun loadProfileInformation(user:UserData){
        userIDText.text = user.id
        firstNameText.text = user.firstname
        surnameText.text = user.surname
    }

}