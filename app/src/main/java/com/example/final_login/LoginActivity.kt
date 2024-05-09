package com.example.final_login

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class LoginActivity : AppCompatActivity() {

    private val formController = FormController()
    private val user = User()
    private val security = Security()

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

    private var themeAccessibleActive = false
    private lateinit var wholePage:ConstraintLayout
    private lateinit var cardPage: CardView

    private val rCSignIn = 9001


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
        wholePage = findViewById(R.id.wholePage)
        cardPage = findViewById(R.id.cardPage)
        themeAccessibleActive = !intent.getBooleanExtra("themeAccessibleActive", false)
        toggleTheme()
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

        if (intent.getBooleanExtra("verificationEmailSent", false)) {
            val rootView = findViewById<View>(android.R.id.content)
            Snackbar.make(rootView, "Verification email to your new address. Please check your inbox and click the link to complete the change.", Snackbar.LENGTH_LONG).show()
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
                Toast.makeText(this@LoginActivity,"All fields are mandatory.",Toast.LENGTH_SHORT).show()
            }
        }
        btnThemeSwitch.setOnClickListener{
            toggleTheme()
        }
        // Google Login Btn
        btnGoogleLogin.setOnClickListener {
            // TODO: Deprecated!! Don't Know What To Replace With
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, rCSignIn)
        }
        // Register Text Link
        textRegister.setOnClickListener{
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra("themeAccessibleActive", themeAccessibleActive)
            startActivity(intent)
            finish()
        }
        // Forgot Password Text
        textForgotPassword.setOnClickListener {
            showForgotPasswordDialog()
        }

    }

    // Email & Password Login
    private fun loginUserEmailPassword(username: String, password: String) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(username, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                checkIfEmailUpdated()
                Toast.makeText(this@LoginActivity,"You are Logged In.",Toast.LENGTH_SHORT).show()
                val intent = Intent(this@LoginActivity, ConfigHealthConnectActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this@LoginActivity, "Login Failed! Please try again shortly.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Google Auth Login
    // Google Auth Function
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == rCSignIn) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!, account)
            } catch (e: ApiException) {
                println(e)
            }
        }
    }
    private fun firebaseAuthWithGoogle(idToken: String,account:GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    user.checkUserExistsUID {
                        println("User Exists: $it")
                        if(!it){
                            val name = user.splitName(account.displayName!!)
                            user.dbCreateUser(account.email!!, name.first, name.second)
                            val intent = Intent(this@LoginActivity, ConfigHealthConnectActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        } else {
                            val intent = Intent(this@LoginActivity, ConfigHealthConnectActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }

                    }

                } else {
                    Toast.makeText(this@LoginActivity, "Authentication Failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkIfEmailUpdated() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            databaseReference.child(security.enc(currentUser.uid)).get().addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                    val email = security.dec(dataSnapshot.child("email").getValue(String::class.java))
                    if (email != currentUser.email) {
                        databaseReference.child(security.enc(currentUser.uid)).child("email").setValue(security.enc(currentUser.email!!))
                    }
                } else {
                    println("firebase Error: Data not found or empty")
                }
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
        // TODO: Switch from accessible to normal theme just implement functionality, styling can be done once designed by UI
        if(themeAccessibleActive){
            wholePage.setBackgroundColor(resources.getColor(R.color.white, null))
            cardPage.setCardBackgroundColor(resources.getColor(R.color.off_white, null))
            themeAccessibleActive = false
            btnThemeSwitch.setChecked(false)
        } else {
            wholePage.setBackgroundColor(resources.getColor(R.color.accessiblePurple, null))
            cardPage.setCardBackgroundColor(resources.getColor(R.color.accessibleYellow, null))
            themeAccessibleActive = true
            btnThemeSwitch.setChecked(true)
        }
    }

    // Forgot Password Dialog
    private fun showForgotPasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.forgot_password_dialog, null)
        val inputEmailForgotPassword = dialogView.findViewById<TextInputEditText>(R.id.inputEmailForgotPassword)
        val btnEmailForgotPassword = dialogView.findViewById<Button>(R.id.btnEmailForgotPassword)

        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)

        if (themeAccessibleActive) {
            dialogView.findViewById<RelativeLayout>(R.id.forgotPasswordLayout).setBackgroundColor(resources.getColor(R.color.accessibleYellow, null))
        }

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