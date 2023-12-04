package it.polito.s294545.privacymanager.utilities

import android.app.AppOpsManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageStatsManager
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.database.Cursor
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.provider.CalendarContract
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import it.polito.s294545.privacymanager.R
import it.polito.s294545.privacymanager.customDataClasses.Rule
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class MonitorManager : Service() {
    // Permissions
    private val PACKAGE_USAGE_STATS_PERMISSION_REQUEST = 101

    // Service notification
    private val NOTIFICATION_ID = 1
    private lateinit var notificationManager: NotificationManager

    // Service utilities
    private val channelId = "MonitorServiceChannel"
    private val handler = Handler()

    // Monitoring interval in milliseconds
    private val monitoringInterval = 5000
    // List of all currently active rules
    private lateinit var activeRules: List<Rule>

    private lateinit var calendarObserver: CalendarObserver

    private val monitorRunnable = object : Runnable {
        override fun run() {
            monitorRunningApps()
            handler.postDelayed(this, monitoringInterval.toLong())
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val retrievedRules = intent?.extras?.get("activeRules")

        activeRules = Json.decodeFromString(retrievedRules.toString())

        startForeground(NOTIFICATION_ID, createNotification()) // Start as a foreground service
        // Initiate monitoring
        handler.post(monitorRunnable)

        return START_STICKY
    }

    private fun createNotification(): Notification {
        notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationChannel = NotificationChannel(
            channelId,
            "Monitor Service Channel",
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(notificationChannel)

        return Notification.Builder(this, channelId)
            .setContentTitle("Monitoring Service")
            .setContentText("Monitoring running apps...")
            .setSmallIcon(R.drawable.icon_app) // Replace with your own icon
            .build()
    }

    private fun monitorRunningApps() {
        // Check if permission is granted
        if (!hasUsageStatsPermission()) {
            requestUsageStatsPermission()
            return
        }

        // Your code for accessing package usage statistics goes here
        // Get a reference to the activity manager service and usage stats manager object
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        // Get a time interval in milliseconds (e.g. 5 seconds)
        val timeInterval = monitoringInterval

        // Get the current time in milliseconds
        val currentTime = System.currentTimeMillis()

        // Query usage statistics for apps within the time interval
        val appList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, currentTime - timeInterval, currentTime)

        // Create a list to store all the running app processes. Just apps defined in a rule are saved
        val runningApps = mutableListOf<String>()

        // Iterate over each app in the list
        for (app in appList) {
            // Check if the app is defined in a monitoring rule
            for (rule in activeRules) {
                if (rule.packageNames!!.contains(app.packageName)) {
                    // Check if the app has a last time used value greater than or equal to the current time minus the time interval
                    if (app.lastTimeUsed >= currentTime - timeInterval) {
                        // Add it to the list of running apps
                        runningApps.add(app.packageName)
                    }
                }
            }
        }

        // Check for defined permissions monitoring
        for (rule in activeRules) {
            // Check calendar permission and if the rule has some running app
            if (rule.permissions!!.contains("calendar") && rule.packageNames!!.any { it in runningApps }) {
                monitorCalendar()
            }
            // Check camera permission and if the rule has some running app
            if (rule.permissions!!.contains("camera") && rule.packageNames!!.any { it in runningApps }) {
                monitorCamera()
            }
        }
    }

    private fun monitorCalendar() {
        // creates and starts a new thread set up as a looper
        val thread = HandlerThread("CalendarHandlerThread")
        thread.start()

        // creates the handler using the passed looper
        val handler = Handler(thread.looper)

        calendarObserver = CalendarObserver(contentResolver, handler)

        calendarObserver.startObserving()
    }

    private fun monitorCamera() {
        // Get an instance of the CameraManager
        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        // Create an availability callback
        val availabilityCallback = object : CameraManager.AvailabilityCallback() {
            override fun onCameraAvailable(cameraId: String) {
                // This is called when a camera device becomes available to open
            }

            override fun onCameraUnavailable(cameraId: String) {
                // This is called when a camera device becomes unavailable to open
                Log.d("myapp", "Camera is unavailable")
            }
        }

        // Register the availability callback
        cameraManager.registerAvailabilityCallback(availabilityCallback, null)
        //cameraManager.unregisterAvailabilityCallback(availabilityCallback, null)
    }

    private fun hasUsageStatsPermission() : Boolean {
        val appOps = getSystemService(AppCompatActivity.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun requestUsageStatsPermission() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        //startActivityForResult(context as Activity, intent, PACKAGE_USAGE_STATS_PERMISSION_REQUEST, null)
    }

    fun stopForegroundService() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(STOP_FOREGROUND_REMOVE) // Stop as a foreground service
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}

class CalendarObserver(private val contentResolver: ContentResolver, handler: Handler) {

    private val uri: Uri = CalendarContract.Events.CONTENT_URI

    private val observer = object : ContentObserver(handler) {
        override fun onChange(selfChange: Boolean) {
            Log.d("myapp", "calendar event changed")
        }
    }

    private fun observe() {
        contentResolver.registerContentObserver(uri, true, observer)
    }

    fun startObserving() {
        observe()
    }

    fun stopObserving() {
        contentResolver.unregisterContentObserver(observer)
    }
}