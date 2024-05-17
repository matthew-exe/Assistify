package com.example.final_login

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.BaseExpandableListAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ExpandableListView
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import java.time.LocalDate
import java.util.Calendar

class SettingsActivity : AppCompatActivity() {
    private val user = User()
    private val security = Security()
    private val themeChangeMessageKey = "themeChangeMessage"
    private var themeChangeMessage: String? = null
    private lateinit var adapter: MyExpandableListAdapter
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var signOutButton: Button
    private lateinit var expandableListView: ExpandableListView
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser
    private lateinit var rootView: View
    private lateinit var wholePage: ConstraintLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.reference.child("users")
        firebaseAuth = FirebaseAuth.getInstance()
        currentUser = firebaseAuth.currentUser!!
        rootView = findViewById(android.R.id.content)


        if (!user.isUserLoggedIn()) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        signOutButton = findViewById(R.id.sign_out_button)
        signOutButton.setOnClickListener {
            user.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        bottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_settings

        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, DashboardActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.nav_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.nav_settings -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                    true
                }

                else -> false
            }
        }

        expandableListView = findViewById(R.id.expandable_list_view)
        expandableListView.setGroupIndicator(null)

        // Create the adapter and set it up
        val sections = listOf(
            SettingsItem(
                "First Name",
                null,
                "John"
            ),
            SettingsItem(
                "Surname",
                null,
                "Doe"
            ),
            SettingsItem(
                "Email Address",
                null,
                "e***********@gmail.com"
            ),
            SettingsItem(
                "Phone Number",
                null,
                "07*********61"
            ),
            SettingsItem(
                "Personal Details",
                listOf(
                    "Age",
                    "Date of Birth",
                    "Blood Type",
                    "NHS Number",
                    "View Medical Conditions",
                    "View Emergency Contact Details"
                ),
                null,
                PersonalDetails()
            ),
            SettingsItem("Change Password", null),
            SettingsItem("Generate Monitor Key", null),
            SettingsItem("Switch Theme", null),
            SettingsItem(
                "Privacy Policy", null),
            SettingsItem(
                "About App",
                listOf(
                    "Our mission is to seamlessly integrate smart devices and adaptive controls into a user-friendly mobile application. With real-time monitoring capabilities, we empower caregivers to watch over vulnerable care-recipients, alerting them to any potential hazards. This app is designed to offer peace of mind to both caregivers and care-recipients, enhancing independence while ensuring safety."
                )
            ),
            SettingsItem(
                "Contact us",
                listOf(
                    "Our customer service team is available Monday - Friday, 9am - 5pm to help you with any questions or concerns you may have.\n\nYou can reach us by phone (07570586472) or email (s5519054@bournemouth.ac.uk).\n\nWe look forward to hearing from you!"
                )
            ),
        )
        populateUserDetails(sections)

        wholePage = findViewById(R.id.wholePage)
        setTheme()

        savedInstanceState?.getString(themeChangeMessageKey)?.let {
            Snackbar.make(rootView, it, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun populateUserDetails(sections: List<SettingsItem>) {
        databaseReference.child(security.enc(currentUser.uid!!)).get()
            .addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                    val email = dataSnapshot.child("email").getValue(String::class.java)?.let { security.dec(it) } ?: ""
                    val firstname = dataSnapshot.child("firstname").getValue(String::class.java)?.let { security.dec(it) } ?: ""
                    val surname = dataSnapshot.child("surname").getValue(String::class.java)?.let { security.dec(it) } ?: ""
                    val phoneNumber = dataSnapshot.child("phoneNumber").getValue(String::class.java)?.let { security.dec(it) } ?: ""

                    val maskedEmail = email.replace(Regex("""^(\w{1})(\w+)(@\w+\.\w+)$"""), "$1******$3")
                    val maskedPhoneNumber = phoneNumber.replace(Regex("""^(\d{2})(\d+)(\d{2})$"""), "$1******$3")

                    sections[0].displayValue = firstname
                    sections[1].displayValue = surname
                    sections[2].displayValue = maskedEmail
                    sections[3].displayValue = maskedPhoneNumber

                    var age: String
                    var dateOfBirth: String
                    var bloodType: String
                    var nhsNumber: String
                    var emergencyContactName: String
                    var emergencyContactRelation: String
                    var emergencyContactNumber: String
                    var medicalConditions: List<String>

                    val personalDetailsSnapshot = dataSnapshot.child("personalDetails")
                    if (personalDetailsSnapshot.exists()) {
                        age = personalDetailsSnapshot.child("age").getValue(String::class.java)?.let { security.dec(it) } ?: ""
                        dateOfBirth = personalDetailsSnapshot.child("dateOfBirth").getValue(String::class.java)?.let { security.dec(it) } ?: ""
                        bloodType = personalDetailsSnapshot.child("bloodType").getValue(String::class.java)?.let { security.dec(it) } ?: ""
                        nhsNumber = personalDetailsSnapshot.child("nhsNumber").getValue(String::class.java)?.let { security.dec(it) } ?: ""
                        emergencyContactName = personalDetailsSnapshot.child("emergencyContactName").getValue(String::class.java)?.let { security.dec(it) } ?: ""
                        emergencyContactRelation = personalDetailsSnapshot.child("emergencyContactRelation").getValue(String::class.java)?.let { security.dec(it) } ?: ""
                        emergencyContactNumber = personalDetailsSnapshot.child("emergencyContactNumber").getValue(String::class.java)?.let { security.dec(it) } ?: ""
                        val genericTypeIndicator : GenericTypeIndicator<List<String>> = object : GenericTypeIndicator<List<String>>() {}
                        val medicalConditionsSnapshot = personalDetailsSnapshot.child("medicalConditions").getValue(genericTypeIndicator)
                        medicalConditions = medicalConditionsSnapshot?.map { security.dec(it) } ?: emptyList()

                    } else {
                        age = ""
                        dateOfBirth = ""
                        bloodType = ""
                        nhsNumber = ""
                        emergencyContactName = ""
                        emergencyContactRelation = ""
                        emergencyContactNumber = ""
                        medicalConditions = emptyList()
                    }


                    val personalDetails = PersonalDetails(
                        age,
                        dateOfBirth,
                        bloodType,
                        nhsNumber,
                        emergencyContactName,
                        emergencyContactRelation,
                        emergencyContactNumber,
                        medicalConditions,
                    )

                    sections[4].children = listOf(
                        "Age: $age",
                        "Date of Birth: $dateOfBirth",
                        "Blood Type: $bloodType",
                        "NHS Number: $nhsNumber",
                        "View Medical Conditions",
                        "View Emergency Contact Details"
                    )

                    sections[4].personalDetails = personalDetails

                    adapter = MyExpandableListAdapter(this, sections)
                    expandableListView.setAdapter(adapter)
                } else {
                    println("firebase Error: Data not found or empty")
                }
            }.addOnFailureListener { exception ->
                println("firebase Error getting data: $exception")
            }
    }

    private fun updateUserDetails(valueToUpdate: String, value: String, onSuccess: () -> Unit) {
        databaseReference.child(security.enc(currentUser.uid!!)).get()
            .addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                    databaseReference.child(security.enc(currentUser.uid!!)).updateChildren(
                        mapOf(
                            valueToUpdate to security.enc(value)
                        )
                    ).addOnSuccessListener {
                        onSuccess()
                    }
                } else {
                    println("firebase Error: Data not found or empty")
                }
            }
    }

    private fun updateUserPersonalDetails(valueToUpdate: String, value: String, onSuccess: () -> Unit) {
        databaseReference.child(security.enc(currentUser.uid!!)).child("personalDetails").get()
            .addOnSuccessListener { dataSnapshot ->
                databaseReference.child(security.enc(currentUser.uid!!)).child("personalDetails").updateChildren(
                    mapOf(
                        valueToUpdate to security.enc(value)
                    )
                ).addOnSuccessListener {
                    onSuccess()
                }
            }
    }

    private fun showEditDialog(
        title: String,
        initialValue: String,
        validateInput: (String) -> Boolean,
        onSave: (String) -> Unit
    ) {
        val editText = EditText(this)
        editText.setText(initialValue)

        val dialog = if (!ThemeSharedPref.getThemeState(this)) {
            AlertDialog.Builder(this, R.style.MyDialogTheme)
        } else {
            AlertDialog.Builder(this)
        }
            .setTitle(title)
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                val newValue = editText.text.toString()
                if (validateInput(newValue)) {
                    onSave(newValue)
                    Snackbar.make(rootView, "User settings updated successfully", Snackbar.LENGTH_SHORT).show()
                } else {
                    Snackbar.make(rootView, "Invalid input", Snackbar.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
        if (!ThemeSharedPref.getThemeState(this)) {
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(resources.getColor(R.color.black, null))
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(resources.getColor(R.color.black, null))
        }
    }


    private fun showMedicalConditionsDialog(sections: MutableList<SettingsItem>, groupPosition: Int) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_medical_conditions, null)
        val rvMedicalConditions = dialogView.findViewById<RecyclerView>(R.id.rv_medical_conditions)
        val etMedicalCondition = dialogView.findViewById<EditText>(R.id.et_medical_condition)

        val personalDetails = sections[groupPosition].personalDetails ?: PersonalDetails()
        val medicalConditionsAdapter = MedicalConditionsAdapter(this, personalDetails.medicalConditions ?: emptyList())
        rvMedicalConditions.adapter = medicalConditionsAdapter
        rvMedicalConditions.layoutManager = LinearLayoutManager(this)

        val dialog = if (!ThemeSharedPref.getThemeState(this)) {
            AlertDialog.Builder(this, R.style.MyDialogTheme)
        } else {
            AlertDialog.Builder(this)
        }
            .setTitle("Medical Conditions")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                // Save the medical conditions to the database
                val conditions = medicalConditionsAdapter.medicalConditions
                databaseReference.child(security.enc(currentUser.uid!!)).child("personalDetails").child("medicalConditions")
                    .setValue(conditions.map { security.enc(it) })
                    .addOnSuccessListener {
                        sections[groupPosition].personalDetails = personalDetails.copy(medicalConditions = conditions)
                        adapter.notifyDataSetChanged()
                        Snackbar.make(rootView, "Medical conditions saved successfully", Snackbar.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { exception ->
                        println("firebase Error saving medical conditions: $exception")
                    }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
        if (!ThemeSharedPref.getThemeState(this)) {
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(resources.getColor(R.color.black, null))
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(resources.getColor(R.color.black, null))
        }

        etMedicalCondition.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val condition = etMedicalCondition.text.toString().trim()
                if (condition.isNotBlank()) {
                    medicalConditionsAdapter.addCondition(condition)
                    etMedicalCondition.setText("")
                }
                true
            } else {
                false
            }
        }
    }

    private fun showEmergencyContactDialog(
        sections: MutableList<SettingsItem>,
        groupPosition: Int
    ) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_emergency_contact, null)
        val etContactName = dialogView.findViewById<EditText>(R.id.et_contact_name)
        val etContactRelation = dialogView.findViewById<EditText>(R.id.et_contact_relation)
        val etContactNumber = dialogView.findViewById<EditText>(R.id.et_contact_number)

        val personalDetails = sections[groupPosition].personalDetails ?: PersonalDetails()
        etContactName.setText(personalDetails.emergencyContactName ?: "")
        etContactRelation.setText(personalDetails.emergencyContactRelation ?: "")
        etContactNumber.setText(personalDetails.emergencyContactNumber ?: "")

        val dialog = if (!ThemeSharedPref.getThemeState(this)) {
            AlertDialog.Builder(this, R.style.MyDialogTheme)
        } else {
            AlertDialog.Builder(this)
        }
            .setTitle("Emergency Contact")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val name = etContactName.text.toString().trim()
                val relation = etContactRelation.text.toString().trim()
                val number = etContactNumber.text.toString().trim()

                // Validate inputs
                if (name.isBlank() || relation.isBlank() || number.isBlank()) {
                    Snackbar.make(rootView, "Please fill in all the fields", Snackbar.LENGTH_SHORT)
                        .show()
                    return@setPositiveButton
                }

                val phoneNumberPattern = Regex("^\\+?\\d{10,13}$")
                if (!phoneNumberPattern.matches(number)) {
                    Snackbar.make(
                        rootView,
                        "Please enter a valid phone number",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                val encryptedName = security.enc(name)
                val encryptedRelation = security.enc(relation)
                val encryptedNumber = security.enc(number)

                val updateMap = mapOf(
                    "emergencyContactName" to encryptedName,
                    "emergencyContactRelation" to encryptedRelation,
                    "emergencyContactNumber" to encryptedNumber
                )

                databaseReference.child(security.enc(currentUser.uid!!)).child("personalDetails")
                    .updateChildren(updateMap)
                    .addOnSuccessListener {
                        sections[groupPosition].personalDetails = personalDetails.copy(
                            emergencyContactName = name,
                            emergencyContactRelation = relation,
                            emergencyContactNumber = number
                        )
                        adapter.notifyDataSetChanged()
                        Snackbar.make(
                            rootView,
                            "Emergency contact saved successfully",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                    .addOnFailureListener { exception ->
                        println("firebase Error saving emergency contact: $exception")
                        Snackbar.make(
                            rootView,
                            "Error saving emergency contact",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
        if (!ThemeSharedPref.getThemeState(this)) {
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(resources.getColor(R.color.black, null))
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(resources.getColor(R.color.black, null))
        }
    }

    private fun showPrivacyPolicyDialog() {
        val dialogView = layoutInflater.inflate(R.layout.privacy_policy_dialog, null)
        val btnAcceptTerms = dialogView.findViewById<Button>(R.id.btnClose)

        val dialogBuilder = if (!ThemeSharedPref.getThemeState(this@SettingsActivity)) {
            AlertDialog.Builder(this, R.style.MyDialogTheme)
                .setView(dialogView)
        } else {
            AlertDialog.Builder(this)
                .setView(dialogView)
        }

        val alertDialog = dialogBuilder.show()

        btnAcceptTerms.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    private fun showResetPasswordDialog() {
        val dialog = if (!ThemeSharedPref.getThemeState(this)) {
            AlertDialog.Builder(this, R.style.MyDialogTheme)
        } else {
            AlertDialog.Builder(this)
        }
            .setTitle("Change Password")
            .setMessage("Do you want us to send you an email to reset your password?")
            .setPositiveButton("Yes") { _, _ ->
                user.sendResetPasswordEmail(this, currentUser.email!!)
            }
            .setNegativeButton("No", null)
            .create()

        dialog.show()
        if (!ThemeSharedPref.getThemeState(this)) {
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(resources.getColor(R.color.black, null))
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(resources.getColor(R.color.black, null))
        }
    }

    private fun showCopySnackBar(secureKey:String){
        val snackbar = Snackbar.make(rootView, secureKey, Snackbar.LENGTH_INDEFINITE)
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        snackbar.setAction("Copy") {
            val clip = ClipData.newPlainText("secureKey", secureKey)
            clipboardManager.setPrimaryClip(clip)
            Snackbar.make(rootView, "Keep Your Key Safe!", Snackbar.LENGTH_SHORT).show()
        }

        snackbar.show()
    }

    private fun showGenerateMonitorKeyDialog() {
        AlertDialog.Builder(this)
            .setTitle("Generate Monitor Key")
            .setMessage("Do you want to generate a new monitor key?")
            .setPositiveButton("Yes") { _, _ ->
                showCopySnackBar(user.generateSecureLinkKey())
                user.setToAccessPermitted()
            }
            .setNegativeButton("No", null)
            .show()
    }

    // Custom ExpandableListAdapter implementation
    private inner class MyExpandableListAdapter(
        private val context: Context,
        private val sections: List<SettingsItem>
    ) : BaseExpandableListAdapter() {

        override fun getGroupCount(): Int = sections.size

        override fun getChildrenCount(groupPosition: Int): Int =
            sections[groupPosition].children?.size ?: 0

        override fun getGroup(groupPosition: Int): Any = sections[groupPosition]

        override fun getChild(groupPosition: Int, childPosition: Int): Any =
            sections[groupPosition].children!![childPosition]

        override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()

        override fun getChildId(groupPosition: Int, childPosition: Int): Long =
            childPosition.toLong()

        override fun hasStableIds(): Boolean = true

        override fun getGroupView(
            groupPosition: Int,
            isExpanded: Boolean,
            convertView: View?,
            parent: ViewGroup?
        ): View {
            val itemView = convertView ?: layoutInflater.inflate(
                R.layout.item_group,
                parent,
                false
            )
            val titleTextView = itemView.findViewById<TextView>(R.id.tv_group_title)
            val valueTextView = itemView.findViewById<TextView>(R.id.tv_group_value)
            val indicatorImageView = itemView.findViewById<ImageView>(R.id.iv_group_indicator)

            val section = sections[groupPosition]
            titleTextView.text = section.title
            valueTextView.text = section.displayValue ?: ""

            // Show or hide the group value TextView based on whether the group item has a display value
            valueTextView.visibility = if (section.displayValue != null) View.VISIBLE else View.GONE

            // Show or hide the group indicator ImageView based on whether the group item has children
            indicatorImageView.visibility = if (section.children != null) View.VISIBLE else View.GONE

            // Set the group indicator image based on whether the group item is expanded or not
            indicatorImageView.setImageResource(if (isExpanded) R.drawable.expand_less else R.drawable.expand_more)

            // Add a click listener to the entire item view to expand or collapse the group item
            itemView.setOnClickListener {
                if (isExpanded) {
                    expandableListView.collapseGroup(groupPosition)
                } else {
                    expandableListView.expandGroup(groupPosition)
                }

                // Add click listeners for editing
                when (section.title) {
                    "First Name" -> showEditDialog("Edit First Name", section.displayValue ?: "",
                        validateInput = { !it.isBlank() },
                        onSave = { validValue ->
                            updateUserDetails("firstname", validValue) {
                                populateUserDetails(sections)
                            }
                        }
                    )
                    "Surname" -> showEditDialog("Edit Surname", section.displayValue ?: "",
                        validateInput = { !it.isBlank() },
                        onSave = { validValue ->
                            updateUserDetails("surname", validValue) {
                                populateUserDetails(sections)
                            }
                        }
                    )
                    "Email Address" -> showEditDialog("Edit Email Address", "",
                        validateInput = { android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches() },
                        onSave = { validValue ->
                            user.updateEmail(validValue) { success ->
                                if (success) {
                                    val intent = Intent(this@SettingsActivity, LoginActivity::class.java)
                                    intent.putExtra("verificationEmailSent", true)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    Snackbar.make(rootView, "Error updating email", Snackbar.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                    "Phone Number" -> showEditDialog("Edit Phone Number", "",
                        validateInput = { it.matches(Regex("""^\+?\d{10,13}$""")) },
                        onSave = { validValue ->
                            updateUserDetails("phoneNumber", validValue) {
                                populateUserDetails(sections)
                            }
                        }
                    )
                    "Change Password" -> showResetPasswordDialog()
                    "Generate Monitor Key" -> showGenerateMonitorKeyDialog()
                    "Switch Theme" -> switchTheme()
                    "Privacy Policy" -> showPrivacyPolicyDialog()
                }
            }

            return itemView
        }

        override fun getChildView(
            groupPosition: Int,
            childPosition: Int,
            isLastChild: Boolean,
            convertView: View?,
            parent: ViewGroup?
        ): View {
            val itemView = convertView ?: layoutInflater.inflate(
                android.R.layout.simple_list_item_1,
                parent,
                false
            )
            val textView = itemView.findViewById<TextView>(android.R.id.text1)
            val childItem = getChild(groupPosition, childPosition) as String
            textView.text = childItem

            itemView.setOnClickListener {
                val personalDetails = sections[groupPosition].personalDetails
                when (sections[groupPosition].title) {
                    "Personal Details" -> {
                        val childItemTitle = childItem.split(":")[0]
                        when (childItemTitle) {
                            "Age" -> showEditDialog("Edit Age", personalDetails?.age ?: "",
                                validateInput = { it.isNotBlank() && it.toIntOrNull() != null && it.toInt() >= 0 },
                                onSave = { validValue ->
                                    updateUserPersonalDetails("age", validValue) {
                                        populateUserDetails(sections)
                                    }
                                }
                            )
                            "Date of Birth" -> showEditDialog("Edit Date of Birth (DD/MM/YYYY)", personalDetails?.dateOfBirth ?: "",
                                validateInput = { input ->
                                    if (input.length != 10) {
                                        false
                                    } else {
                                        val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.UK)
                                        dateFormat.isLenient = false
                                        try {
                                            val date = dateFormat.parse(input)
                                            val calendar = Calendar.getInstance()
                                            if (date != null) {
                                                calendar.time = date
                                            }
                                            val year = calendar.get(Calendar.YEAR)
                                            val yearString = year.toString()
                                            input.substring(input.length - 4) == yearString
                                        } catch (e: java.text.ParseException) {
                                            false
                                        }
                                    }
                                },
                                onSave = { validValue ->
                                    updateUserPersonalDetails("dateOfBirth", validValue) {
                                        populateUserDetails(sections)
                                    }
                                }
                            )
                            "Blood Type" -> showEditDialog("Edit Blood Type", personalDetails?.bloodType ?: "",
                                validateInput = { it.isNotBlank() },
                                onSave = { validValue ->
                                    updateUserPersonalDetails("bloodType", validValue) {
                                        populateUserDetails(sections)
                                    }
                                }
                            )
                            "NHS Number" -> showEditDialog("Edit NHS Number", personalDetails?.nhsNumber ?: "",
                                validateInput = { it.isNotBlank() && it.toIntOrNull() != null && it.length == 10 },
                                onSave = { validValue ->
                                    updateUserPersonalDetails("nhsNumber", validValue) {
                                        populateUserDetails(sections)
                                    }
                                }
                            )
                            "View Medical Conditions" -> showMedicalConditionsDialog(sections.toMutableList(), groupPosition)
                            "View Emergency Contact Details" -> showEmergencyContactDialog(sections.toMutableList(), groupPosition)
                        }
                    }
                }
            }


            return itemView
        }


        override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = true
    }

    private fun switchTheme() {
        themeChangeMessage = if (ThemeSharedPref.getThemeState(this)) {
            ThemeSharedPref.setThemeState(this, false)
            "Switched to accessible theme"
        } else {
            ThemeSharedPref.setThemeState(this, true)
            "Switched to default theme"
        }
        recreate()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(themeChangeMessageKey, themeChangeMessage)
    }

    private fun setTheme() {
        if (!ThemeSharedPref.getThemeState(this)) {
            wholePage.setBackgroundColor(resources.getColor(R.color.accessiblePurple, null))

            bottomNavigationView.setBackgroundColor(resources.getColor(R.color.accessiblePurple, null))
            bottomNavigationView.itemRippleColor = ColorStateList.valueOf(resources.getColor(R.color.accessibleYellow, null))
            bottomNavigationView.itemActiveIndicatorColor = ColorStateList.valueOf(resources.getColor(R.color.accessibleYellow, null))
            bottomNavigationView.itemTextColor = ColorStateList.valueOf(resources.getColor(R.color.black, null))
            bottomNavigationView.itemIconTintList = ColorStateList.valueOf(resources.getColor(R.color.black, null))

            signOutButton.setBackgroundColor(resources.getColor(R.color.accessibleYellow, null))
            signOutButton.setTextColor(resources.getColor(R.color.black, null))

            expandableListView.setBackgroundColor(resources.getColor(R.color.accessibleYellow, null))
        }
    }
}