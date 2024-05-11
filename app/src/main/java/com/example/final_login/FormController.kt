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

    fun isValidPhoneNumber(phone: TextInputEditText): Boolean {
        val ukMobileNumberRegex = Regex("^\\+44\\d{10}$|^\\(\\d{5}\\)\\d{6}$|^0\\d{10}$|^\\(0\\)\\d{10}$|^\\d{5}\\s\\d{6}$")
        return ukMobileNumberRegex.matches(phone.text.toString())
    }


    fun isValidPassword(password: TextInputEditText): Boolean {
        val passwordRegex = Regex("^(?=.*[A-Z])(?=.*\\d.*\\d)(?=.*[^A-Za-z0-9]).{9,}\$")
        return passwordRegex.matches(password.text.toString())
    }

    fun passwordsMatch(password: TextInputEditText, confirmPassword: TextInputEditText): Boolean {
        return password.text.toString() == confirmPassword.text.toString()
    }

    fun checkPhoneNumber(phone:TextInputEditText): Boolean {
        return if(isValidPhoneNumber(phone)){
            applyGreenTick(phone)
            true
        } else {
            removeTick(phone)
            false
        }
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