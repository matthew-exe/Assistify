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
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Register : AppCompatActivity() {

    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference

    private lateinit var editTextForename: TextInputEditText
    private lateinit var editTextSurname: TextInputEditText
    private lateinit var editTextEmail: TextInputEditText
    private lateinit var editTextPassword: TextInputEditText
    private lateinit var editTextConfirmPassword: TextInputEditText
    private lateinit var termsCheckBox: CheckBox

    private lateinit var buttonReg: Button
    private lateinit var loginRedirect: TextView
    private lateinit var termsConditionsTxtView: TextView

    private val formController = FormController()
    private val security = Security()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        // Database
        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.reference.child("users")
        // Inputs
        editTextForename = findViewById(R.id.signupForename)
        editTextSurname = findViewById(R.id.signupSurname)
        editTextEmail = findViewById(R.id.signupUsername)
        editTextPassword = findViewById(R.id.signupPassword)
        editTextConfirmPassword = findViewById(R.id.signupConfirmPassword)
        termsCheckBox = findViewById(R.id.termsCheck)
        // Buttons
        buttonReg = findViewById(R.id.btn_register)
        // Text Links & Styling
        loginRedirect = findViewById(R.id.loginRedirect)
        loginRedirect.text = setColorsOnString(R.id.loginRedirect, "Login Here!", R.color.blue)
        termsConditionsTxtView = findViewById(R.id.termsConditionsBtn)
        termsConditionsTxtView.text = setColorsOnString(R.id.termsConditionsBtn, "Terms and Conditions.", R.color.warning)

        // Input on Change Events
        editTextForename.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                formController.checkName(editTextForename)
                checkAllInputs(editTextForename, editTextSurname, editTextEmail, editTextPassword, editTextConfirmPassword, termsCheckBox)
            }
        }
        editTextSurname.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                formController.checkName(editTextSurname)
                checkAllInputs(editTextForename, editTextSurname, editTextEmail, editTextPassword, editTextConfirmPassword, termsCheckBox)
            }
        }
        editTextEmail.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                formController.checkEmail(editTextEmail)
                checkAllInputs(editTextForename, editTextSurname, editTextEmail, editTextPassword, editTextConfirmPassword, termsCheckBox)
            }
        }
        editTextPassword.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                checkAllInputs(editTextForename, editTextSurname, editTextEmail, editTextPassword, editTextConfirmPassword, termsCheckBox)
            }
        }
        editTextConfirmPassword.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                checkAllInputs(editTextForename, editTextSurname, editTextEmail, editTextPassword, editTextConfirmPassword, termsCheckBox)
            }
        }
        termsCheckBox.setOnCheckedChangeListener { _, _ ->
            checkAllInputs(editTextForename, editTextSurname, editTextEmail, editTextPassword, editTextConfirmPassword, termsCheckBox)
        }

        // Button Clicks
        buttonReg.setOnClickListener{
            val signupUsername = editTextEmail.text.toString()
            val signupPassword = editTextPassword.text.toString()
            if(isAllInputsValid(editTextForename, editTextSurname, editTextEmail, editTextPassword, editTextConfirmPassword, termsCheckBox)){
                createUser(signupUsername,signupPassword, editTextForename, editTextSurname)
            } else {
                // TODO: Implement some kind of messaging system between the form controller and here to show why it failed such as incorrect email format
                Toast.makeText(this@Register,"All fields are mandatory.",Toast.LENGTH_SHORT).show()
            }
        }
        // Link Clicks
        loginRedirect.setOnClickListener{
            startActivity(Intent(this@Register, Login::class.java))
            finish()
        }
        termsConditionsTxtView.setOnClickListener{
            showTermsAndConditionsDialog()
        }
    }
    // User Signup Function
    private fun createUser(username:String, password:String, firstname: TextInputEditText, surname: TextInputEditText){
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(username, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val firebaseUser: FirebaseUser = task.result!!.user!!
                createUserDBObj(firebaseUser.uid,firstname, surname)
                Toast.makeText(this@Register,"You are Registered.",Toast.LENGTH_SHORT).show()
                val intent = Intent(this@Register, Login::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this@Register,"Whoops.",Toast.LENGTH_SHORT).show()
            }
        }
    }
    // Create User In Database
    private fun createUserDBObj(uid: String, firstname: TextInputEditText, surname: TextInputEditText){
        databaseReference.orderByChild("username").equalTo(uid).addListenerForSingleValueEvent(object: ValueEventListener{
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.exists()){
                    val id = databaseReference.child(security.enc(uid)).key!!
                    val userData = UserData(id, security.enc(firstname.text.toString()), security.enc(surname.text.toString()))
                    databaseReference.child(id).setValue(userData)
                    Toast.makeText(this@Register,"Signup Successful",Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@Register, Login::class.java))
                    finish()
                } else {
                    Toast.makeText(this@Register,"User already exists",Toast.LENGTH_SHORT).show()
                }
            }
            override fun onCancelled(databaseError: DatabaseError){
                Toast.makeText(this@Register,"Database Error: ${databaseError.message} ",Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun isAllInputsValid(forename: TextInputEditText, surname: TextInputEditText, username: TextInputEditText, password: TextInputEditText, confirmPassword: TextInputEditText, checkbox:CheckBox): Boolean {
        return formController.isValidName(forename) && formController.isValidName(surname) && formController.isValidEmail(username) && formController.isValidPassword(password) && formController.passwordsMatch(password, confirmPassword) && formController.isChecked(checkbox)
    }

    private fun checkAllInputs(forename: TextInputEditText, surname: TextInputEditText, username: TextInputEditText, password: TextInputEditText, confirmPassword: TextInputEditText, checkbox:CheckBox) {
        val regBtn: Button = findViewById(R.id.btn_register)
        regBtn.isEnabled = isAllInputsValid(forename, surname, username, password, confirmPassword, checkbox)
    }
    // String Colours
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
    // Terms & Conditions
    private fun showTermsAndConditionsDialog() {
        val dialogView = layoutInflater.inflate(R.layout.terms_and_conditions_dialog, null)
        val acceptButton = dialogView.findViewById<Button>(R.id.acceptButton)
        val declineButton = dialogView.findViewById<Button>(R.id.declineButton)

        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Terms and Conditions")

        val alertDialog = dialogBuilder.show()

        acceptButton.setOnClickListener {
            acceptedTermsConditions()
            alertDialog.dismiss()
        }

        declineButton.setOnClickListener {
            declinedTermsConditions()
            alertDialog.dismiss()
        }
    }

    private fun acceptedTermsConditions() {
        val myCheckBox: CheckBox = findViewById(R.id.termsCheck)
        myCheckBox.isEnabled = true
        myCheckBox.isChecked = true
    }

    private fun declinedTermsConditions() {
        val myCheckBox: CheckBox = findViewById(R.id.termsCheck)
        myCheckBox.isEnabled = false
        myCheckBox.isChecked = false
    }

}