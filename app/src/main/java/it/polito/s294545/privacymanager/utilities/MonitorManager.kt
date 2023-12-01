package it.polito.s294545.privacymanager.utilities

import android.app.Activity
import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.startActivityForResult
import it.polito.s294545.privacymanager.customDataClasses.Rule

class MonitorManager(private val context: Context, private val activeRules: List<Rule>) {
    // permissions
    private val PACKAGE_USAGE_STATS_PERMISSION_REQUEST = 101

    private val handler = Handler()

    // Set the interval in milliseconds
    private val monitoringInterval = 5000

    private val monitorRunnable = object : Runnable {
        override fun run() {
            monitorRunningApps()
            handler.postDelayed(this, monitoringInterval.toLong())
        }
    }

    fun startMonitoring() {
        // Start the monitoring immediately
        handler.post(monitorRunnable)
    }

    fun stopMonitoring() {
        // Stop the monitoring
        handler.removeCallbacks(monitorRunnable)
    }

    fun monitorRunningApps() {
        // Check if permission is granted
        if (!hasUsageStatsPermission()) {
            requestUsageStatsPermission()
            return
        }

        // Your code for accessing package usage statistics goes here
        // Get a reference to the activity manager service and usage stats manager object
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        // Get a time interval in milliseconds (e.g. 5 seconds)
        val timeInterval = monitoringInterval

        // Get the current time in milliseconds
        val currentTime = System.currentTimeMillis()

        // Query usage statistics for apps within the time interval
        val appList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, currentTime - timeInterval, currentTime)

        // Create a list to store all the running app processes
        val runningApps = mutableListOf<String>()

        // Iterate over each app in the list
        for (app in appList) {
            // Check if the app has a last time used value greater than or equal to the current time minus the time interval
            if (app.lastTimeUsed >= currentTime - timeInterval) {
                // Add it to the list of running apps
                runningApps.add(app.packageName)
            }
        }

        Log.d("myapp", "$runningApps") //apps running in the last timeInterval ms
    }

    private fun hasUsageStatsPermission() : Boolean {
        val appOps = context.getSystemService(AppCompatActivity.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun requestUsageStatsPermission() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        startActivityForResult(context as Activity, intent, PACKAGE_USAGE_STATS_PERMISSION_REQUEST, null)
    }
}