package com.example.final_login

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class DashboardAdapter(
    private val context: Context,
    private val isUserDashboard: Boolean
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
        if (isUserDashboard) {
            filteredData.add("Add") // Add the "Add" button at the end if it's the user's dashboard
        }
    }

    inner class ButtonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val button: View = view.findViewById(R.id.button)
    }

    inner class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvSensorName)
        val image: ImageView = view.findViewById(R.id.ivSensorImage)
        val stat: TextView = view.findViewById(R.id.tvSensorStat)
    }


    override fun getItemViewType(position: Int): Int {
        return if (position == itemCount - 1 && isUserDashboard) VIEW_TYPE_BUTTON else VIEW_TYPE_SENSOR
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
                if (isUserDashboard) {
                    val sensorCard = holder.button.findViewById<CardView>(R.id.button)
                    val sensorText = holder.button.findViewById<TextView>(R.id.buttonText)
                    if (!ThemeSharedPref.getThemeState(holder.button.context)) {
                        sensorText.setTextColor(holder.button.context.resources.getColor(R.color.black, null))
                        sensorCard.setCardBackgroundColor(holder.button.context.resources.getColor(R.color.accessibleYellow, null))
                    }
                    holder.button.setOnClickListener {
                        showSensorSelectionPopup()
                    }
                } else {
                    holder.button.isEnabled = false
                }
            }
            is ItemViewHolder -> {
                val sensor = filteredData[position] as SensorData
                val sensorCard = holder.itemView.findViewById<CardView>(R.id.sensorCard)
                val sensorName = holder.itemView.findViewById<TextView>(R.id.tvSensorName)
                val sensorStat = holder.itemView.findViewById<TextView>(R.id.tvSensorStat)
                val sensorImage = holder.itemView.findViewById<ImageView>(R.id.ivSensorImage)
                holder.name.text = sensor.name
                holder.image.setImageResource(sensor.image)
                if (!ThemeSharedPref.getThemeState(holder.itemView.context)) {
                    val blackColorFilter = PorterDuffColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN)

                    sensorImage.setImageResource(sensor.image)
                    sensorImage.colorFilter = blackColorFilter

                    sensorCard.setCardBackgroundColor(holder.itemView.context.resources.getColor(R.color.accessibleYellow, null))
                    sensorName.setTextColor(holder.itemView.context.resources.getColor(R.color.black, null))
                    sensorStat.setTextColor(holder.itemView.context.resources.getColor(R.color.black, null))
                }
                holder.stat.text = if(sensor.stat == null) "Syncing" else if(sensor.stat != "0") sensor.stat else "Syncing"

                if (sensor.name == "Pulse") {
                    val heartAnimation = AnimationUtils.loadAnimation(context, R.anim.anim_pulse)
                    holder.image.startAnimation(heartAnimation)
                }

                if (isUserDashboard) {
                    holder.itemView.setOnLongClickListener {
                        showDeleteConfirmationDialog(position)
                        true
                    }
                } else {
                    holder.itemView.isLongClickable = false
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return filteredData.size
    }

    private fun addItem(sensorData: SensorData) {
        user.populateDashboard(this, user.getUserUidToLoad())
        data.add(sensorData)
        user.sendDashboardToDatabase(data)
        filterData(editTextText.text.toString())

    }

    private fun showSensorSelectionPopup() {
        val builder = if (!ThemeSharedPref.getThemeState(context)) {
            AlertDialog.Builder(context, R.style.MyDialogTheme)
        } else {
            AlertDialog.Builder(context)
        }
        builder.setTitle("Select Sensor")

        val sensorItems = SensorRepository.sensorName.map { (id, name) ->
            val image = SensorRepository.structures[id] ?: 0
            SensorData(name, image, "0")
        }

        val adapter = SensorArrayAdapter(
            context,
            android.R.layout.simple_list_item_1,
            sensorItems.map { it.name },
            filteredData.filterIsInstance<SensorData>().map{it.name}
        )

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
        val builder = if (!ThemeSharedPref.getThemeState(context)) {
            AlertDialog.Builder(context, R.style.MyDialogTheme)
        } else {
            AlertDialog.Builder(context)
        }
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
        if (isUserDashboard) {
            filteredData.add("Add")
        }
        notifyDataSetChanged()
    }

}