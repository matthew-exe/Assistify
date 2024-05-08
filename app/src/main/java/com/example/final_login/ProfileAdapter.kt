package com.example.final_login

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.viewpager.widget.PagerAdapter
import com.google.android.material.snackbar.Snackbar

class ProfileAdapter(private val context: Context) : PagerAdapter() {
    private var layouts = mutableListOf<Pair<Int, ProfileData>>()

    init {
        val sampleUserDetails = ProfileData(
            "",
            R.drawable.profile,
            "",
            0,
            "",
            "",
            listOf(""),
            "",
            "",
            ""
        )
        layouts.add(Pair(R.layout.activity_link_profile, sampleUserDetails))
    }

    override fun getCount(): Int {
        return layouts.size
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(context)
        val (layoutResId, userDetails) = layouts[position]
        val layout = inflater.inflate(layoutResId, container, false)

        if (layoutResId == R.layout.activity_link_profile) {
            // This is the activity_link_profile layout
            layout.findViewById<Button>(R.id.linkButton)?.setOnClickListener {
                // Replace this with the actual user details
                val sampleUserDetails = ProfileData(
                    "Yvonne",
                    R.drawable.yvonne,
                    "1951-11-30",
                    73,
                    "A+",
                    "4857773456",
                    listOf("Hip replacement", "Arthritis"),
                    "Teresa",
                    "Daughter",
                    "07777123456"
                )
                (context as ProfileActivity).linkUserProfile(sampleUserDetails)
            }

            val preLinkText1 = layout.findViewById<TextView>(R.id.pre_link_text_1)
            if (layouts.size > 1) {
                preLinkText1.text = "You can link with another client."
            } else {
                preLinkText1.text = "It appears you are not yet linked to a client."
            }
        } else {
            // This is the activity_linked_profile layout
            updateUserProfile(layout, userDetails)
        }

        container.addView(layout)
        return layout
    }

    private fun updateUserProfile(layout: View, userDetails: ProfileData) {
        val linearLayout = layout.findViewById<LinearLayout>(R.id.linearLayout)
        linearLayout.setBackgroundResource(R.drawable.border)

        val dob = "Date of birth: ${userDetails.dateOfBirth}"
        val age = "Age: ${userDetails.age}"
        val bloodType = "Blood type: ${userDetails.bloodType}"
        val nhsNumber = "NHS number: ${userDetails.nhsNumber}"
        var medicalConditions = "Medical conditions: "
        if (userDetails.medConditions.isNotEmpty()) {
            for (medCon in userDetails.medConditions) {
                medicalConditions += "\n \u2022 $medCon"
            }
        }
        val details = "Details:"
        val contactInfo = "Contact Information:"
        val emergencyContact = "Emergency contact: ${userDetails.emergencyContact}"
        val emergencyRelation = "Relation to client: ${userDetails.emergencyRelation}"
        val emergencyNumber = "Emergency contact number: ${userDetails.emergencyNumber}"

        // Update the UI with the user's details
        layout.findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.profile_picture)
            .setImageResource(userDetails.profilePicture)
        layout.findViewById<TextView>(R.id.name_text).text = userDetails.name
        layout.findViewById<TextView>(R.id.dob_text).text = dob
        layout.findViewById<TextView>(R.id.age_text).text = age
        layout.findViewById<TextView>(R.id.blood_type_text).text = bloodType
        layout.findViewById<TextView>(R.id.nhs_number_text).text = nhsNumber
        layout.findViewById<TextView>(R.id.medical_conditions_text).text = medicalConditions
        layout.findViewById<TextView>(R.id.details_text).text = details
        layout.findViewById<TextView>(R.id.info_text).text = contactInfo
        layout.findViewById<TextView>(R.id.emergency_contact_text).text = emergencyContact
        layout.findViewById<TextView>(R.id.emergency_relation_text).text = emergencyRelation
        layout.findViewById<TextView>(R.id.emergency_number_text).text = emergencyNumber

        layout.findViewById<Button>(R.id.unlink_button)?.setOnClickListener {
            removeLayout(userDetails)
        }
    }

    fun addLinkedProfileLayout(userDetails: ProfileData) {
        layouts.add(0, Pair(R.layout.activity_linked_profile, userDetails))
        layouts.add(1, Pair(R.layout.activity_stat_profile, userDetails))
        notifyDataSetChanged()
    }

    private fun removeLayout(userDetails: ProfileData) {
        val layoutToRemove = layouts.find { it.second == userDetails }
        if (layoutToRemove != null) {
            layouts.remove(layoutToRemove)
            notifyDataSetChanged()
            (context as ProfileActivity).unlinkSnackBar(userDetails)
        }
    }
}