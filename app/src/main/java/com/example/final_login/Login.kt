package com.example.final_login

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class Login : AppCompatActivity() {

    private val formController = FormController()
    private val security = Security()
    private val user = User()

    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var inputEmail: TextInputEditText
    private lateinit var inputPassword: TextInputEditText

    private lateinit var textRegister: TextView
    private lateinit var textForgotPassword: TextView

    private lateinit var btnGoogleLogin: Button
    private lateinit var btnThemeSwitch: SwitchMaterial
    private lateinit var btnLogin: Button
    private val RC_SIGN_IN = 9001
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Database
        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.reference.child("users")
        firebaseAuth = FirebaseAuth.getInstance()
        // Google Signin
        val googleSigninOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            // default_web_client_id - appears as a red error but works fine, it relates to the google.json services file in the app root
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail().build()
        googleSignInClient = GoogleSignIn.getClient(this, googleSigninOptions)
        // Inputs
        inputEmail = findViewById(R.id.inputEmail)
        inputPassword = findViewById(R.id.inputPassword)
        // Login Button
        btnLogin = findViewById(R.id.btnLogin)
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin)
        // Register Link and Styling
        textRegister = findViewById(R.id.textRegister)
        textRegister.text = setColorsOnString(R.id.textRegister, "Register Here!", R.color.blue)
        // Theme Switch
        btnThemeSwitch = findViewById(R.id.btnAccessibleTheme)
        // Forgot Password Link and Styling
        textForgotPassword = findViewById(R.id.textForgotPassword)
        textForgotPassword.text = setColorsOnString(R.id.textForgotPassword, "Reset Here.", R.color.warning)
        // Input on Change Events
        inputEmail.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                formController.checkEmail(inputEmail)
                btnLogin.isEnabled = formController.isValidPassword(inputPassword) && formController.isValidEmail(inputEmail)
            }
        }

        inputPassword.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                btnLogin.isEnabled = formController.isValidPassword(inputPassword) && formController.isValidEmail(inputEmail)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // DO NOT DELETE
                // Doesn't need anything inside but has to be implemented otherwise the TextWatch class throws error
            }

            override fun afterTextChanged(s: Editable?) {
                // DO NOT DELETE
                // Doesn't need anything inside but has to be implemented otherwise the TextWatch class throws error
            }
        })

        // Button Clicks
        btnLogin.setOnClickListener{
            val loginEmail = inputEmail.text.toString()
            val loginPassword = inputPassword.text.toString()
            if(formController.isValidEmail(inputEmail) && loginPassword.isNotEmpty()){
                loginUserEmailPassword(loginEmail, loginPassword)
            } else {
                Toast.makeText(this@Login,"All fields are mandatory.",Toast.LENGTH_SHORT).show()
            }
        }

        btnThemeSwitch.setOnClickListener{
            toggleTheme()
        }

        btnGoogleLogin.setOnClickListener {
            // TODO: Deprecated!! Don't Know What To Replace With
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        textRegister.setOnClickListener{
            startActivity(Intent(this@Login, Register::class.java))
            finish()
        }

        textForgotPassword.setOnClickListener {
            showForgotPasswordDialog()
        }

    }

    // Email & Password Login
    @RequiresApi(Build.VERSION_CODES.O)
    private fun loginUserEmailPassword(username: String, password: String) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(username, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this@Login,"You are Logged In.",Toast.LENGTH_SHORT).show()
                val intent = Intent(this@Login, Dashboard::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this@Login, "Login Failed! Please try again shortly.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Google Auth Login
    @RequiresApi(Build.VERSION_CODES.O)
    // Google Auth Function
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                println(account.displayName)
                user.checkUserExists(account.email!!) { userExists ->
                    if(userExists){
                        println("User EXists")
                        firebaseAuthWithGoogle(account.idToken!!)
                    }
                    else {
                        println("Create User")
                        user.dbCreateUser(account.email!!, account.displayName!!, account.displayName!!)
                        firebaseAuthWithGoogle(account.idToken!!)
                    }
                }
            } catch (e: ApiException) {
                println(e)
            }
        }
    }
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val intent = Intent(this@Login, Dashboard::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@Login, "Authentication Failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Split Colors On String
    private fun setColorsOnString(txtViewId: Int, strToChange: String, colour: Int): SpannableString {
        // TODO: Move to a class called Utils, makes code more readable
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
        println("Toggle Theme")
    }

    // Forgot Password Dialog
    @RequiresApi(Build.VERSION_CODES.O)
    private fun showForgotPasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.forgot_password_dialog, null)
        val inputEmailForgotPassword = dialogView.findViewById<TextInputEditText>(R.id.inputEmailForgotPassword)
        val btnEmailForgotPassword = dialogView.findViewById<Button>(R.id.btnEmailForgotPassword)

        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)

        val alertDialog = dialogBuilder.create()

        btnEmailForgotPassword.setOnClickListener {
            if (formController.isValidEmail(inputEmailForgotPassword)) {
                user.sendResetPasswordEmail(this, inputEmailForgotPassword.text.toString().trim())
                alertDialog.dismiss()
            }
        }

        inputEmailForgotPassword.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                btnEmailForgotPassword.isEnabled = formController.isValidEmail(inputEmailForgotPassword)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // DO NOT DELETE
                // Doesn't need anything inside but has to be implemented otherwise the TextWatch class throws error
            }

            override fun afterTextChanged(s: Editable?) {
                // DO NOT DELETE
                // Doesn't need anything inside but has to be implemented otherwise the TextWatch class throws error
            }
        })

        alertDialog.show()
    }



}