package com.example.final_login

import android.util.Patterns
import android.widget.CheckBox
import com.google.android.material.textfield.TextInputEditText

class FormController {

    fun isValidName(name: TextInputEditText): Boolean {
        return name.text.toString().isNotBlank() && name.text.toString().matches("[a-zA-Z]+(?:[-'\\s][a-zA-Z]+)*".toRegex())
    }

    fun isValidEmail(email: TextInputEditText): Boolean {
        return email.text.toString().isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email.text.toString()).matches()
    }

    fun isValidPassword(password: TextInputEditText): Boolean {
        val passwordRegex = Regex("^(?=.*[A-Z])(?=.*\\d.*\\d)(?=.*[^A-Za-z0-9]).{9,}\$")
        return passwordRegex.matches(password.text.toString())
    }

    fun passwordsMatch(password: TextInputEditText, confirmPassword: TextInputEditText): Boolean {
        return password.text.toString() == confirmPassword.text.toString()
    }

    fun checkName(element:TextInputEditText): Boolean {
        return if(isValidName(element)){
            applyGreenTick(element)
            true
        } else {
            removeTick(element)
            false
        }
    }

    fun checkEmail(element:TextInputEditText): Boolean {
        return if(isValidEmail(element)){
            applyGreenTick(element)
            true
        } else {
            removeTick(element)
            false
        }
    }

    fun isChecked(element: CheckBox): Boolean {
        return element.isChecked
    }

    // Styling
    private fun applyGreenTick(element: TextInputEditText) {
        element.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.correct_tick, 0)
    }
    private fun removeTick(element: TextInputEditText) {
        element.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
    }

}