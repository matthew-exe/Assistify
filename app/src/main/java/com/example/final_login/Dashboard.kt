package com.example.final_login

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.MenuInflater
import android.view.View
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class Dashboard : AppCompatActivity() {

    private lateinit var textEmailAddress: TextView
    private lateinit var textFirstName: TextView
    private lateinit var textSurname: TextView
    private lateinit var btnUserMenu: ImageView

    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser
    private val user = User()
    private val security = Security()


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(!user.isUserLoggedIn()){
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }

        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.reference.child("users")
        firebaseAuth = FirebaseAuth.getInstance()

        currentUser = firebaseAuth.currentUser!!


        textEmailAddress = findViewById(R.id.textEmailAddress)
        textFirstName = findViewById(R.id.textFirstName)
        textSurname = findViewById(R.id.textSurname)

        btnUserMenu = findViewById(R.id.imgUserMenu)
        btnUserMenu.setOnClickListener{
            println("Open Menu")
            openUserMenu(btnUserMenu)
        }

        loadUserProfile()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadUserProfile(){
        databaseReference.child(security.enc(currentUser.email!!)).get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                val id = dataSnapshot.child("id").getValue(String::class.java)!!
                val firstname = security.dec(dataSnapshot.child("firstname").getValue(String::class.java))
                val surname = security.dec(dataSnapshot.child("surname").getValue(String::class.java))
                setProfileData(UserData(id, firstname, surname))
            } else {
                println("firebase Error: Data not found or empty")
            }
        }.addOnFailureListener { exception ->
            println("firebase Error getting data: $exception")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun genGuardianKey(){
        println(security.generateSecureKey(32))
    }

    private fun setProfileData(user:UserData){
        textEmailAddress.text = user.id
        textFirstName.text = user.firstname
        textSurname.text = user.surname
    }

    private fun openUserMenu(it:View){
        val popup = PopupMenu(this, it)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.popup_menu, popup.menu)
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.btnLogout -> {
                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(this@Dashboard, Login::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

}