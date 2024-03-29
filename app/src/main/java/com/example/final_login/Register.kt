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

    private lateinit var inputFirstname: TextInputEditText
    private lateinit var inputSurname: TextInputEditText
    private lateinit var inputEmail: TextInputEditText
    private lateinit var inputPassword: TextInputEditText
    private lateinit var inputConfirmPassword: TextInputEditText
    private lateinit var checkBoxTermsConditons: CheckBox

    private lateinit var btnRegister: Button
    private lateinit var textLogin: TextView
    private lateinit var textTermsConditons: TextView

    private val formController = FormController()
    private val security = Security()
    private val user = User()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        // Database
        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.reference.child("users")
        // Inputs
        inputFirstname = findViewById(R.id.inputFirstname)
        inputSurname = findViewById(R.id.inputSurname)
        inputEmail = findViewById(R.id.inputEmail)
        inputPassword = findViewById(R.id.inputPassword)
        inputConfirmPassword = findViewById(R.id.inputConfirmPassword)
        checkBoxTermsConditons = findViewById(R.id.checkBoxTermsConditons)
        // Buttons
        btnRegister = findViewById(R.id.btnRegister)
        // Text Links & Styling
        textLogin = findViewById(R.id.textLogin)
        textLogin.text = setColorsOnString(R.id.textLogin, "Login Here!", R.color.blue)
        textTermsConditons = findViewById(R.id.textTermsConditons)
        textTermsConditons.text = setColorsOnString(R.id.textTermsConditons, "Terms and Conditions.", R.color.warning)

        // Input on Change Events
        inputFirstname.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                formController.checkName(inputFirstname)
                checkAllInputs(inputFirstname, inputSurname, inputEmail, inputPassword, inputConfirmPassword, checkBoxTermsConditons)
            }
        }
        inputSurname.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                formController.checkName(inputSurname)
                checkAllInputs(inputFirstname, inputSurname, inputEmail, inputPassword, inputConfirmPassword, checkBoxTermsConditons)
            }
        }
        inputEmail.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                formController.checkEmail(inputEmail)
                checkAllInputs(inputFirstname, inputSurname, inputEmail, inputPassword, inputConfirmPassword, checkBoxTermsConditons)
            }
        }
        inputPassword.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                checkAllInputs(inputFirstname, inputSurname, inputEmail, inputPassword, inputConfirmPassword, checkBoxTermsConditons)
            }
        }
        inputConfirmPassword.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                checkAllInputs(inputFirstname, inputSurname, inputEmail, inputPassword, inputConfirmPassword, checkBoxTermsConditons)
            }
        }
        checkBoxTermsConditons.setOnCheckedChangeListener { _, _ ->
            checkAllInputs(inputFirstname, inputSurname, inputEmail, inputPassword, inputConfirmPassword, checkBoxTermsConditons)
        }

        // Button Clicks
        btnRegister.setOnClickListener{
            val signupUsername = inputEmail.text.toString()
            val signupPassword = inputPassword.text.toString()
            if(isAllInputsValid(inputFirstname, inputSurname, inputEmail, inputPassword, inputConfirmPassword, checkBoxTermsConditons)){
                sendRegistration(signupUsername,signupPassword, inputFirstname, inputSurname)
            } else {
                // TODO: Implement some kind of messaging system between the form controller and here to show why it failed such as incorrect email format
                Toast.makeText(this@Register,"All fields are mandatory.",Toast.LENGTH_SHORT).show()
            }
        }
        // Link Clicks
        textLogin.setOnClickListener{
            startActivity(Intent(this@Register, Login::class.java))
            finish()
        }
        textTermsConditons.setOnClickListener{
            showTermsAndConditionsDialog()
        }
    }
    // User Signup Function
    private fun sendRegistration(username:String, password:String, firstname: TextInputEditText, surname: TextInputEditText){
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(username, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                user.dbCreateUser(username,firstname.text.toString(), surname.text.toString())
                Toast.makeText(this@Register,"You are Registered.",Toast.LENGTH_SHORT).show()
                val intent = Intent(this@Register, Login::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this@Register,"Failed To Create User Please Try Again.",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isAllInputsValid(forename: TextInputEditText, surname: TextInputEditText, username: TextInputEditText, password: TextInputEditText, confirmPassword: TextInputEditText, checkbox:CheckBox): Boolean {
        return formController.isValidName(forename) && formController.isValidName(surname) && formController.isValidEmail(username) && formController.isValidPassword(password) && formController.passwordsMatch(password, confirmPassword) && formController.isChecked(checkbox)
    }

    private fun checkAllInputs(forename: TextInputEditText, surname: TextInputEditText, username: TextInputEditText, password: TextInputEditText, confirmPassword: TextInputEditText, checkbox:CheckBox) {
        btnRegister.isEnabled = isAllInputsValid(forename, surname, username, password, confirmPassword, checkbox)
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
        val btnAcceptTerms = dialogView.findViewById<Button>(R.id.btnAcceptTerms)
        val btnDeclineTerms = dialogView.findViewById<Button>(R.id.btnDeclineTerms)

        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Terms and Conditions")

        val alertDialog = dialogBuilder.show()

        btnAcceptTerms.setOnClickListener {
            acceptedTermsConditions()
            alertDialog.dismiss()
        }

        btnDeclineTerms.setOnClickListener {
            declinedTermsConditions()
            alertDialog.dismiss()
        }
    }

    private fun acceptedTermsConditions() {
        checkBoxTermsConditons.isEnabled = true
        checkBoxTermsConditons.isChecked = true
    }

    private fun declinedTermsConditions() {
        checkBoxTermsConditons.isEnabled = false
        checkBoxTermsConditons.isChecked = false
    }

}