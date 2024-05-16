package com.example.final_login

import android.content.Intent
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
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference

    private lateinit var inputFirstname: TextInputEditText
    private lateinit var inputSurname: TextInputEditText
    private lateinit var inputEmail: TextInputEditText
    private lateinit var inputPassword: TextInputEditText
    private lateinit var inputPhone: TextInputEditText
    private lateinit var inputConfirmPassword: TextInputEditText
    private lateinit var checkBoxTermsConditons: CheckBox

    private lateinit var btnRegister: Button
    private lateinit var textLogin: TextView
    private lateinit var textTermsConditons: TextView

    private lateinit var btnThemeSwitch: SwitchMaterial
    private var themeAccessibleActive = false
    private lateinit var wholePage: ConstraintLayout
    private lateinit var cardPage: CardView

    private val formController = FormController()
    private val user = User()

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
        inputPhone = findViewById(R.id.inputPhone)
        inputPassword = findViewById(R.id.inputPassword)
        inputConfirmPassword = findViewById(R.id.inputConfirmPassword)
        checkBoxTermsConditons = findViewById(R.id.checkBoxTermsConditons)
        // Buttons
        btnRegister = findViewById(R.id.btnRegister)
        // Theme Switcheroo
        btnThemeSwitch = findViewById(R.id.btnAccessibleTheme)
        wholePage = findViewById(R.id.wholePage)
        cardPage = findViewById(R.id.cardPage)
        themeAccessibleActive = !intent.getBooleanExtra("themeAccessibleActive", false)
        // Text Links & Styling
        textLogin = findViewById(R.id.textLogin)
        textLogin.text = setColorsOnString(R.id.textLogin, "Login Here!", R.color.blue)
        textTermsConditons = findViewById(R.id.textTermsConditions)
        textTermsConditons.text = setColorsOnString(R.id.textTermsConditions, "Terms and Conditions.", R.color.warning)

        // Input on Change Events
        inputFirstname.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                formController.checkName(inputFirstname)
                checkAllInputs(inputFirstname, inputSurname, inputEmail,inputPassword, inputConfirmPassword, checkBoxTermsConditons, inputPhone)
            }
        }
        inputSurname.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                formController.checkName(inputSurname)
                checkAllInputs(inputFirstname, inputSurname, inputEmail,inputPassword, inputConfirmPassword, checkBoxTermsConditons, inputPhone)
            }
        }
        inputEmail.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                formController.checkEmail(inputEmail)
                checkAllInputs(inputFirstname, inputSurname, inputEmail,inputPassword, inputConfirmPassword, checkBoxTermsConditons, inputPhone)
            }
        }
        inputPhone.onFocusChangeListener = View.OnFocusChangeListener{_, hasFocus ->
            if(!hasFocus){
                formController.checkPhoneNumber(inputPhone)
                checkAllInputs(inputFirstname, inputSurname, inputEmail,inputPassword, inputConfirmPassword, checkBoxTermsConditons, inputPhone)
            }
        }
        inputPassword.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                checkAllInputs(inputFirstname, inputSurname, inputEmail,inputPassword, inputConfirmPassword, checkBoxTermsConditons, inputPhone)
            }
        }
        inputConfirmPassword.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                checkAllInputs(inputFirstname, inputSurname, inputEmail,inputPassword, inputConfirmPassword, checkBoxTermsConditons, inputPhone)
            }
        }

        checkBoxTermsConditons.setOnCheckedChangeListener { _, _ ->
            checkAllInputs(inputFirstname, inputSurname, inputEmail,inputPassword, inputConfirmPassword, checkBoxTermsConditons, inputPhone)
        }


        // Button Clicks
        btnRegister.setOnClickListener{
            val signupEmail = inputEmail.text.toString()
            val signupPassword = inputPassword.text.toString()
            if(isAllInputsValid(inputFirstname, inputSurname, inputEmail, inputPassword, inputConfirmPassword, checkBoxTermsConditons, inputPhone)){
                sendRegistration(signupEmail,signupPassword, inputFirstname, inputSurname, inputPhone)
            } else {
                Toast.makeText(this@RegisterActivity,"All fields are mandatory.",Toast.LENGTH_SHORT).show()
            }
        }
        // Link Clicks
        textLogin.setOnClickListener{

            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra("themeAccessibleActive", themeAccessibleActive)
            startActivity(intent)
            finish()
        }
        textTermsConditons.setOnClickListener{
            showTermsAndConditionsDialog()
        }

        btnThemeSwitch.setOnClickListener{
            toggleTheme()
        }
        toggleTheme()
    }
    // User Signup Function
    private fun sendRegistration(email:String, password:String, firstname: TextInputEditText, surname: TextInputEditText, phone:TextInputEditText){
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                user.dbCreateUser(email,firstname.text.toString(), surname.text.toString(), phone.text.toString())
                Toast.makeText(this@RegisterActivity,"You are Registered.",Toast.LENGTH_SHORT).show()
                val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this@RegisterActivity,"Failed To Create User Please Try Again.",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isAllInputsValid(forename: TextInputEditText, surname: TextInputEditText, username: TextInputEditText, password: TextInputEditText, confirmPassword: TextInputEditText, checkbox:CheckBox, phone:TextInputEditText): Boolean {
        return formController.isValidName(forename) && formController.isValidName(surname) && formController.isValidEmail(username) && formController.isValidPassword(password) && formController.passwordsMatch(password, confirmPassword) && formController.isChecked(checkbox) && formController.isValidPhoneNumber(phone)
    }

    private fun checkAllInputs(forename: TextInputEditText, surname: TextInputEditText, username: TextInputEditText, password: TextInputEditText, confirmPassword: TextInputEditText, checkbox:CheckBox, phone:TextInputEditText) {
        btnRegister.isEnabled = isAllInputsValid(forename, surname, username, password, confirmPassword, checkbox, phone)
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

        val dialogBuilder = if (!themeAccessibleActive) {
            AlertDialog.Builder(this)
                .setView(dialogView)
        } else {
            AlertDialog.Builder(this, R.style.MyDialogTheme)
                .setView(dialogView)
        }

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

    private fun toggleTheme() {
        if(themeAccessibleActive){
            wholePage.setBackgroundColor(resources.getColor(R.color.accessiblePurple, null))
            cardPage.setCardBackgroundColor(resources.getColor(R.color.off_white, null))
            ThemeSharedPref.setThemeState(this, true)
            themeAccessibleActive = false
            btnThemeSwitch.setChecked(false)
        } else {
            wholePage.setBackgroundColor(resources.getColor(R.color.accessiblePurple, null))
            cardPage.setCardBackgroundColor(resources.getColor(R.color.accessibleYellow, null))
            ThemeSharedPref.setThemeState(this, false)
            themeAccessibleActive = true
            btnThemeSwitch.setChecked(true)
        }
    }

}