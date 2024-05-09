package com.example.final_login

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter

class ProfileAdapter(private val context: Context) : PagerAdapter() {
    var layouts = mutableListOf<Pair<Int, ProfileData>>()
    private val user = User()

    init {
        user.checkAndGetChildFromDatabase(context, this, false)
    }

    fun loadNoProfile(){
        layouts.add(Pair(R.layout.activity_link_profile, user.emptyUserDetails))
        notifyDataSetChanged()
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
                (context as ProfileActivity).showKeyEnterDialog()
            }

            val preLinkText1 = layout.findViewById<TextView>(R.id.pre_link_text_1)
            val preLinkText2 = layout.findViewById<TextView>(R.id.pre_link_text_2)
            val linkButton = layout.findViewById<TextView>(R.id.linkButton)
            if (layouts.size > 1) {
                preLinkText1.text = ""
                preLinkText2.text = "Multi-account Monitoring Coming Soon."
                linkButton.isEnabled = false
            } else {
                preLinkText1.text = "It appears you are not yet linked to a client."
                preLinkText2.text = "Please tap the button below to link."
                linkButton.isEnabled = true
            }
        } else if (layoutResId == R.layout.activity_linked_profile) {
            // This is the activity_linked_profile layout
            updateUserProfile(layout, userDetails)
        } else {
            updateStatProfile(layout, userDetails)
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
        val emergencyNumber = "Emergency contact no. ${userDetails.emergencyNumber}"

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
            user.removeChildForGuardian()
            removeLayout(userDetails)
        }

        layout.findViewById<Button>(R.id.call_button)?.setOnClickListener {
            (context as ProfileActivity).callClient()
        }
    }

    private fun updateStatProfile(layout: View, userDetails: ProfileData) {
        val linearLayout = layout.findViewById<LinearLayout>(R.id.linearLayoutStat)
        linearLayout.setBackgroundResource(R.drawable.border)

        val statPicture = R.drawable.statistics
        val statText = "Statistics:"
        val heartRate = "Heart Rate:"
        val currentBPM = "Current: ${userDetails.currentBPM}bpm"
        val minBPM = "Min: ${userDetails.minBPM}bpm"
        val maxBPM = "Max: ${userDetails.maxBPM}bpm"
        val avgBPM = "Avg: ${userDetails.avgBPM}bpm"
        val stepsText = "Movement:"
        val stepsLastDetected = "Last Movements: ${userDetails.stepsLastDetected}"
        val steps24hTotal = "Total Steps Today: ${userDetails.steps24hTotal}"
        val stepsFirstDetected = "Woke Up: ${userDetails.stepsFirstDetected}"
        val caloriesText = "Calories:"
        val caloriesSpent = "Total Today: ${userDetails.caloriesTotalSpent}"
        val sleepText = "Sleep:"
        val totalSleep = "Total ${userDetails.sleepTotal}"

        layout.findViewById<ImageView>(R.id.statistics_picture)
            .setImageResource(statPicture)
        layout.findViewById<TextView>(R.id.statistics_text).text = statText
        layout.findViewById<TextView>(R.id.heartRate_text).text = heartRate
        layout.findViewById<TextView>(R.id.currBPM_text).text = currentBPM
        layout.findViewById<TextView>(R.id.minBPM_text).text = minBPM
        layout.findViewById<TextView>(R.id.maxBPM_text).text = maxBPM
        layout.findViewById<TextView>(R.id.averageBPM_text).text = avgBPM
        layout.findViewById<TextView>(R.id.steps_text).text = stepsText
        layout.findViewById<TextView>(R.id.steps24hTotal_text).text = steps24hTotal
        layout.findViewById<TextView>(R.id.stepsLastDetected_text).text = stepsLastDetected
        layout.findViewById<TextView>(R.id.stepsFirstDetected_text).text = stepsFirstDetected
        layout.findViewById<TextView>(R.id.sleep_text).text = sleepText
        layout.findViewById<TextView>(R.id.sleep_total_text).text = totalSleep
        layout.findViewById<TextView>(R.id.calories_text).text = caloriesText
        layout.findViewById<TextView>(R.id.caloriesTotalSpent_text).text = caloriesSpent
    }

    fun addLinkedProfileLayout(userDetails: ProfileData) {
        layouts.add(0, Pair(R.layout.activity_linked_profile, userDetails))
        layouts.add(1, Pair(R.layout.activity_stat_profile, userDetails))
        notifyDataSetChanged()
    }

    private fun removeLayout(userDetails: ProfileData) {
        val layoutToRemove = layouts.find { it.second == userDetails }
        val statIndex = layouts.indexOf(layoutToRemove)
        val statToRemove = layouts[statIndex + 1]

        if (layoutToRemove != null) {
//            layouts.remove(layoutToRemove)
//            layouts.remove(statToRemove)
            layouts.clear()
            (context as ProfileActivity).unlinkSnackBar(userDetails)
            loadNoProfile()
            notifyDataSetChanged()
        }
    }
}