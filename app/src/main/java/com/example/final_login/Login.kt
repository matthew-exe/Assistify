package com.example.final_login

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class Login : AppCompatActivity() {

    private val formController = FormController()
    private val security = Security()

    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference

    private lateinit var editTextEmail: TextInputEditText
    private lateinit var editTextPassword: TextInputEditText
    private lateinit var buttonLog: Button
    private lateinit var registerRedirect: TextView
    private lateinit var forgotPasswordText: TextView

    private lateinit var themeSwitchBtn: Button


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        // Database
        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.reference.child("users")
        // Inputs
        editTextEmail = findViewById(R.id.email)
        editTextPassword = findViewById(R.id.password)
        // Login Button
        buttonLog = findViewById(R.id.btn_login)
        // Register Link and Styling
        registerRedirect = findViewById(R.id.registerRedirect)
        registerRedirect.text = setColorsOnString(R.id.registerRedirect, "Register Here!", R.color.blue)
        // Theme Switch
        themeSwitchBtn = findViewById(R.id.btnAccessibleTheme)
        // Forgot Password Link and Styling
        forgotPasswordText = findViewById(R.id.forgotPassword)
        forgotPasswordText.text = setColorsOnString(R.id.forgotPassword, "Reset Here.", R.color.warning)
        // Input on Change Events
        editTextEmail.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                formController.checkEmail(editTextEmail)
            }
        }
        // Button Clicks
        buttonLog.setOnClickListener{
            val loginEmail = editTextEmail.text.toString()
            val loginPassword = editTextPassword.text.toString()
            if(formController.isValidEmail(editTextEmail) && loginPassword.isNotEmpty()){
                loginUser(loginEmail, loginPassword)
            } else {
                Toast.makeText(this@Login,"All fields are mandatory.",Toast.LENGTH_SHORT).show()
            }
        }

        registerRedirect.setOnClickListener{
            startActivity(Intent(this@Login, Register::class.java))
            finish()
        }

        themeSwitchBtn.setOnClickListener{
            toggleTheme()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loginUser(username: String, password: String) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(username, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val firebaseUser: FirebaseUser = task.result!!.user!!
                Toast.makeText(this@Login,"You are Logged In.",Toast.LENGTH_SHORT).show()
                val intent = Intent(this@Login, Dashboard::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                intent.putExtra("user_id", security.enc(firebaseUser.uid))
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this@Login, "Login Failed! Please try again shortly.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loginUserGoogle(){
        // TODO: Implement google auth
    }
    private fun setColorsOnString(txtViewId: Int, strToChange: String, colour: Int): SpannableString {
        val txtView: TextView = findViewById(txtViewId)
        val spannableString = SpannableString(txtView.text)
        val newColour = ContextCompat.getColor(this, colour)
        spannableString.setSpan(
            ForegroundColorSpan(newColour),
            spannableString.indexOf(strToChange),
            spannableString.indexOf(strToChange) + strToChange.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spannableString
    }

    private fun toggleTheme() {
        // TODO: Switch from accessible to normal theme just implemenmt functionality, styling can be done once designed by UI
        // Toggles theme from accessible to normal
    }

}