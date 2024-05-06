package com.example.final_login

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.recyclerview.widget.RecyclerView

class MyAdapter(
    private val context: Context,
    private val generateDummySensorData: (Int) -> List<SensorData>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val VIEW_TYPE_BUTTON = 0
        private const val VIEW_TYPE_SENSOR = 1
    }

    var data = mutableListOf<SensorData>()
    private val filteredData = mutableListOf<Any>()
    private lateinit var editTextText: AppCompatEditText
    private val user = User()

    init {
        filteredData.addAll(data)
        filteredData.add("Add") // Add the "Add" button at the end
    }

    inner class ButtonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val button: View = view.findViewById(R.id.button)
    }

    inner class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvSensorName)
        val image: ImageView = view.findViewById(R.id.ivSensorImage)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == itemCount - 1) VIEW_TYPE_BUTTON else VIEW_TYPE_SENSOR
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        editTextText = parent.rootView.findViewById(R.id.editTextText)
        return when (viewType) {
            VIEW_TYPE_BUTTON -> {
                val inflatedView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.button, parent, false)
                ButtonViewHolder(inflatedView)
            }
            VIEW_TYPE_SENSOR -> {
                val inflatedView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.sensor, parent, false)
                ItemViewHolder(inflatedView)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ButtonViewHolder -> {
                holder.button.setOnClickListener {
                    showSensorSelectionPopup()
                }
            }
            is ItemViewHolder -> {
                val sensor = filteredData[position] as SensorData
                holder.name.text = sensor.name
                holder.image.setImageResource(sensor.image)

                if(sensor.name == "Pulse"){
                    val heartAnimation = AnimationUtils.loadAnimation(context, R.anim.anim_pulse)
                    holder.image.startAnimation(heartAnimation)
                }

                holder.itemView.setOnLongClickListener {
                    showDeleteConfirmationDialog(position)
                    true
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return filteredData.size
    }

    private fun addItem(sensorData: SensorData) {
        data.add(sensorData)
        user.sendDashboardToDatabase(data)
        filterData(editTextText.text.toString())
    }

    private fun showSensorSelectionPopup() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Select Sensor")

        val sensorItems = SensorRepository.sensorName.map { (id, name) ->
            val image = SensorRepository.structures[id] ?: 0
            SensorData(name, image)
        }

        val adapter = SensorArrayAdapter(
            context,
            android.R.layout.simple_list_item_1,
            sensorItems.map { it.name },
            sensorItems.filter{it in filteredData }.map{ it.name } // Passes in a list of currently selected sensors on the dashboard so that no dashboard can have or store two of the same sensors
        )

        if(sensorItems.filter{it in filteredData }.map{ it.name } == sensorItems.map{it.name}){
//            TODO("Show text instead of sensor options, saying something like 'MORE COMING SOON....'")
        }

        builder.setAdapter(adapter) { _, which ->
            addItem(sensorItems[which])
        }

        builder.create().show()
    }

    class SensorArrayAdapter(
        context: Context,
        resource: Int,
        objects: List<String>,
        private val disabledItems: List<String>
    ) : ArrayAdapter<String>(context, resource, objects) {

        override fun isEnabled(position: Int): Boolean {
            // disables any inputs that are already assigned to users dashboard
            return getItem(position) !in disabledItems
        }
    }


    private fun showDeleteConfirmationDialog(position: Int) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Delete Sensor")
        builder.setMessage("Are you sure you want to delete this sensor?")
        builder.setPositiveButton("Yes") { _, _ ->
            deleteItem(position)
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    private fun deleteItem(position: Int) {
        data.removeAt(position)
        user.sendDashboardToDatabase(data)
        filterData(editTextText.text.toString())
    }

    fun filterData(query: String) {
        filteredData.clear()
        if (query.isEmpty()) {
            filteredData.addAll(data)
        } else {
            data.forEach { item ->
                if (item is SensorData && item.name.contains(query, ignoreCase = true)) {
                    filteredData.add(item)
                }
            }
        }
        filteredData.add("Add") // Add the "Add" button at the end
        notifyDataSetChanged()
    }

}