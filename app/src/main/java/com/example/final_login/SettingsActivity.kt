package com.example.final_login

import android.content.Context
import android.content.Intent
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SettingsActivity : AppCompatActivity() {
    private val user = User()
    private val security = Security()
    private lateinit var adapter: MyExpandableListAdapter
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var signOutButton: Button
    private lateinit var expandableListView: ExpandableListView
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.reference.child("users")
        firebaseAuth = FirebaseAuth.getInstance()
        currentUser = firebaseAuth.currentUser!!

        if (!user.isUserLoggedIn()) {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }

        signOutButton = findViewById(R.id.sign_out_button)
        signOutButton.setOnClickListener {
            user.signOut()
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }

        bottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_settings

        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, Dashboard::class.java)
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
            // TODO Make it so that the user's details are displayed in the settings list and the notifications item shows button toggles for each type of notification.
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
                "Person Details",
                listOf(
                    "Age",
                    "Date of Birth",
                    "Blood Type",
                    "NHS Number",
                    "Medical Conditions"
                )
            ),
            SettingsItem("Change Password", null),
            SettingsItem("Notifications", null),
            SettingsItem(
                "Privacy Policy",
                listOf(
                    "This is a sample privacy policy for our app. We respect your privacy and are committed to protecting it through our compliance with this policy. This policy describes the types of information we may collect from you or that you may provide when you use our app and our practices for collecting, using, maintaining, protecting, and disclosing that information.\n\nPlease read this policy carefully to understand our policies and practices regarding your information and how we will treat it. If you do not agree with our policies and practices, your choice is not to use our app."
                )
            ),
            SettingsItem(
                "About App",
                listOf(
                    "This app is simply amazing!"
                )
            ),
            SettingsItem(
                "How to use", listOf(
                    "You can follow this tutorial on Youtube to learn how to use the app:\n\nhttps://www.youtube.com/watch?v=dQw4w9WgXcQ"
                )
            ),
            SettingsItem(
                "Contact us",
                listOf(
                    "Our customer service team is available Monday - Friday, 9am - 5pm to help you with any questions or concerns you may have.\n\nYou can reach us by phone (0735027350) or email (example@gmail.com).\n\nWe look forward to hearing from you!"
                )
            ),
        )

        populateUserDetails(sections)
    }

    private fun populateUserDetails(sections: List<SettingsItem>) {
        databaseReference.child(security.enc(currentUser.uid!!)).get()
            .addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                    val email = dataSnapshot.child("email").getValue(String::class.java) ?.let { security.dec(it) } ?: ""
                    val firstname = dataSnapshot.child("firstname").getValue(String::class.java) ?.let { security.dec(it) } ?: ""
                    val surname = dataSnapshot.child("surname").getValue(String::class.java) ?.let { security.dec(it) } ?: ""
                    val phoneNumber = dataSnapshot.child("phoneNumber").getValue(String::class.java) ?.let { security.dec(it) } ?: ""

                    val maskedEmail = email.replace(Regex("""^(\w{1})(\w+)(@\w+\.\w+)$"""), "$1******$3")
                    val maskedPhoneNumber = phoneNumber.replace(Regex("""^(\d{2})(\d+)(\d{2})$"""), "$1******$3")

                    sections[0].displayValue = firstname
                    sections[1].displayValue = surname
                    sections[2].displayValue = maskedEmail
                    sections[3].displayValue = maskedPhoneNumber

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

    private fun showEditDialog(
        title: String,
        initialValue: String,
        validateInput: (String) -> Boolean,
        onSave: (String) -> Unit
    ) {
        val editText = EditText(this)
        editText.setText(initialValue)

        val dialog = AlertDialog.Builder(this)
            .setTitle(title)
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                val newValue = editText.text.toString()
                if (validateInput(newValue)) {
                    onSave(newValue)
                } else {
                    val rootView = findViewById<View>(android.R.id.content)
                    Snackbar.make(rootView, "Invalid input", Snackbar.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun showPersonalDetailsDialog(section: SettingsItem) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_personal_details, null)
        val etAge = dialogView.findViewById<EditText>(R.id.et_age)
        val etBloodType = dialogView.findViewById<EditText>(R.id.et_blood_type)
        val etDateOfBirth = dialogView.findViewById<EditText>(R.id.et_date_of_birth)
        val etNhsNumber = dialogView.findViewById<EditText>(R.id.et_nhs_number)
        val rvMedicalConditions = dialogView.findViewById<RecyclerView>(R.id.rv_medical_conditions)

        val medicalConditionsAdapter = MedicalConditionsAdapter(this)
        rvMedicalConditions.adapter = medicalConditionsAdapter
        rvMedicalConditions.layoutManager = LinearLayoutManager(this)

        // TODO: Populate the EditText fields with the user's existing personal details

        val dialog = AlertDialog.Builder(this)
            .setTitle("Personal Details")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val age = etAge.text.toString()
                val bloodType = etBloodType.text.toString()
                val dateOfBirth = etDateOfBirth.text.toString()
                val nhsNumber = etNhsNumber.text.toString()
                val medicalConditions = medicalConditionsAdapter.medicalConditions

                // TODO: Save the personal details to the database
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun showMedicalConditionsDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_medical_conditions, null)
        val rvMedicalConditions = dialogView.findViewById<RecyclerView>(R.id.rv_medical_conditions)
        val etMedicalCondition = dialogView.findViewById<EditText>(R.id.et_medical_condition)

        val medicalConditionsAdapter = MedicalConditionsAdapter(this)
        rvMedicalConditions.adapter = medicalConditionsAdapter
        rvMedicalConditions.layoutManager = LinearLayoutManager(this)

        // Set some dummy data for medical conditions
        medicalConditionsAdapter.addCondition("Asthma")
        medicalConditionsAdapter.addCondition("Diabetes")

        val dialog = AlertDialog.Builder(this)
            .setTitle("Medical Conditions")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                // Save the medical conditions to the database
                val conditions = medicalConditionsAdapter.medicalConditions
                // ...
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()

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

    private fun showResetPasswordDialog() {
        AlertDialog.Builder(this)
            .setTitle("Change Password")
            .setMessage("Do you want us to send you an email to reset your password?")
            .setPositiveButton("Yes") { _, _ ->
                user.sendResetPasswordEmail(this, currentUser.email!!)
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
                                    val intent = Intent(this@SettingsActivity, Login::class.java)
                                    intent.putExtra("verificationEmailSent", true)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    // Handle the failure case
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
                when (sections[groupPosition].title) {
                    "Person Details" -> {
                        when (childItem) {
                            "Age" -> showEditDialog("Edit Age", "", validateInput = { it.isNotBlank() }, onSave = { /* Save age */ })
                            "Date of Birth" -> showEditDialog("Edit Date of Birth", "", validateInput = { /* Validate date */ true }, onSave = { /* Save date of birth */ })
                            "Blood Type" -> showEditDialog("Edit Blood Type", "", validateInput = { it.isNotBlank() }, onSave = { /* Save blood type */ })
                            "NHS Number" -> showEditDialog("Edit NHS Number", "", validateInput = { /* Validate NHS number */ true }, onSave = { /* Save NHS number */ })
                            "Medical Conditions" -> showMedicalConditionsDialog()
                        }
                    }
                }
            }

            return itemView
        }

        override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = true
    }
}

