package com.example.final_login

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.viewpager.widget.PagerAdapter
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class InstructionsAdapter(private val context: Context) : PagerAdapter() {

    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)
    private val healthConnectManager = HealthConnectManager(context)

    private val layoutResIds = arrayOf(
        R.layout.activity_introduction,
        R.layout.activity_instructions,
        R.layout.activity_install
    )

    override fun getCount(): Int {
        return layoutResIds.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val layoutResId = layoutResIds[position]
        val view = layoutInflater.inflate(layoutResId, container, false)
        container.addView(view)
        checkForView(layoutResId, view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    private fun checkForView(layoutId:Int, view:View){
        if(layoutId == R.layout.activity_instructions){
            setWatchButtons(view)
        } else if(layoutId == R.layout.activity_install){
            setInstallSyncPage(view)
        }
    }

    private fun setInstallSyncPage(view:View){
        val btnInstall = view.findViewById<Button>(R.id.btnInstall)

        if(healthConnectManager.availability == HealthConnectAvailability.NOT_SUPPORTED){
            addNotSupportedMessage(view, btnInstall)
        } else if(healthConnectManager.availability == HealthConnectAvailability.NOT_INSTALLED){
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                addSyncHealthConnect(btnInstall, true)
            } else {
                addPleaseInstallButton(btnInstall)
            }
        }

        if(healthConnectManager.availability == HealthConnectAvailability.INSTALLED) {
            println("IS INSTALLED ")
            addSyncHealthConnect(btnInstall, false)
        }
    }

    private fun addSyncHealthConnect(button: Button, api34:Boolean){
        button.text = "Sync"
        button.setOnClickListener {
            println("Syncing!")
            if(!api34){
                (context as ConfigHealthConnectActivity).requestPermissions.launch(healthConnectManager.PERMISSIONS)
            } else {
                val requestPermissionActivityContract = PermissionController.createRequestPermissionResultContract()
                (context as ConfigHealthConnectActivity).requestPermissions.launch(healthConnectManager.PERMISSIONS)
            }
        }
    }

    private fun addNotSupportedMessage(view:View, button:Button){
        Snackbar.make(view, "The Current Minimum Version is Android 28, Please Upgrade Your Device", Snackbar.LENGTH_LONG).show()
        button.isEnabled = false
    }

    private fun addPleaseInstallButton(button: Button){
        button.setOnClickListener {
            val uriString = ("market://details?id=com.google.android.apps.healthdata")
            context.startActivity(
                Intent(Intent.ACTION_VIEW).apply {
                    setPackage("com.android.vending")
                    data = Uri.parse(uriString)
                    putExtra("overlay", true)
                    putExtra("callerId", "com.example.final_login")
                })
            //TODO("Return and Refresh So It can Request Permissions Instead of Having To Force Close App and Restart")
        }
    }
    private fun setWatchButtons(view:View){
        val btnFitbitWatch = view.findViewById<Button>(R.id.btnFitbitWatch)
        val btnSamsungWatch = view.findViewById<Button>(R.id.btnSamsungWatch)
        val btnGoogleWatch= view.findViewById<Button>(R.id.btnGoogleWatch)
        btnFitbitWatch.setOnClickListener {
            openApp("com.fitbit.FitbitMobile")
        }
        btnSamsungWatch.setOnClickListener {
            openApp("com.samsung.android.app.watchmanager")
        }
        btnGoogleWatch.setOnClickListener {
            openApp("com.google.android.apps.fitness")
        }
    }

    private fun openApp(app:String){
        val uriString = ("market://details?id=$app")
        context.startActivity(
            Intent(Intent.ACTION_VIEW).apply {
                setPackage("com.android.vending")
                data = Uri.parse(uriString)
                putExtra("overlay", true)
                putExtra("callerId", "com.example.final_login")
            })
    }
}
