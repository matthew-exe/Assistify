package com.example.final_login

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView

class MedicalConditionsAdapter(private val context: Context) :
    RecyclerView.Adapter<MedicalConditionsAdapter.ViewHolder>() {

    val medicalConditions = mutableListOf<String>()

    fun addCondition(condition: String) {
        medicalConditions.add(condition)
        notifyDataSetChanged()
    }

    fun removeCondition(position: Int) {
        medicalConditions.removeAt(position)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_medical_condition, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(medicalConditions[position])
    }

    override fun getItemCount() = medicalConditions.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val etMedicalCondition = itemView.findViewById<EditText>(R.id.et_medical_condition)
        private val btnRemoveCondition = itemView.findViewById<ImageButton>(R.id.btn_remove_condition)

        fun bind(condition: String) {
            etMedicalCondition.setText(condition)
            btnRemoveCondition.setOnClickListener {
                removeCondition(adapterPosition)
            }
        }
    }
}