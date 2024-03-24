package com.example.final_login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth

class Dashboard : AppCompatActivity() {

    private lateinit var userIDText: TextView
    private lateinit var logoutBtn: Button

    val security = Security()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val userId = intent.getStringExtra("user_id")
        userIDText = findViewById(R.id.uid_from_auth)
        userIDText.text = getString(R.string.userid, userId)

        logoutBtn = findViewById(R.id.btn_logout)
        logoutBtn.setOnClickListener{
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this@Dashboard, Login::class.java))
            finish()
        }
    }

}