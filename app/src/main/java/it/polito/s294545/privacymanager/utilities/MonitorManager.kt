package it.polito.s294545.privacymanager.utilities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.AppOpsManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.TaskStackBuilder
import android.app.usage.UsageStatsManager
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.hardware.camera2.CameraManager
import android.location.Location
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.provider.CalendarContract
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import it.polito.s294545.privacymanager.R
import it.polito.s294545.privacymanager.activities.NotificationActivity
import it.polito.s294545.privacymanager.activities.ViolationActivity
import it.polito.s294545.privacymanager.customDataClasses.CustomAddress
import it.polito.s294545.privacymanager.customDataClasses.Rule
import it.polito.s294545.privacymanager.customDataClasses.TimeSlot
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

val NOTIFICATION_ACTION_RESET_FLAG = "it.polito.s294545.privacymanager.utilities.ACTION_RESET_FLAG"

// List of all currently active rules
var activeRules: List<Rule>? = null
// List of currently active rules that have to be monitored (all parameters respected)
var rulesToMonitor: MutableList<Rule>? = null

// Signal violation notification
lateinit var notificationManager: NotificationManager
lateinit var signalNotification: Notification

// Obscure notification
val obscureChannelID = "ObscureNotificationChannel"
lateinit var obscureNotification: Notification

// In order to kill a process
lateinit var activityManager: ActivityManager

lateinit var context: Context

class MonitorManager : Service() {
    // Permissions
    private val PACKAGE_USAGE_STATS_PERMISSION_REQUEST = 101

    // Service notification
    private val NOTIFICATION_MONITOR_ID = 1

    // Service utilities
    private val monitorChannelID = "MonitorServiceChannel"
    private val handler = Handler()

    // Monitoring interval in milliseconds
    private val monitoringInterval = 5000

    // Signal violation notification
    private val signalChannelID = "SignalViolationChannel"
    private lateinit var signalNotificationChannel: NotificationChannel

    private lateinit var calendarObserver: CalendarObserver

    // To manage positions
    private var isNear = false
    // To manage network connection
    private var isConnected = false

    private val monitorRunnable = object : Runnable {
        override fun run() {
            monitorRunningApps()
            handler.postDelayed(this, monitoringInterval.toLong())
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        context = this

        pendingViolationsInfo.clear()

        // Get the ActivityManager instance in order to kill a process
        activityManager = getSystemService(Activity.ACTIVITY_SERVICE) as ActivityManager

        val retrievedRules = intent?.extras?.get("activeRules")

        if (retrievedRules != null) {
            activeRules = Json.decodeFromString(retrievedRules.toString())
        }

        startForeground(NOTIFICATION_MONITOR_ID, createNotification()) // Start as a foreground service
        // Initiate monitoring
        handler.post(monitorRunnable)

        if (!activeRules.isNullOrEmpty() && activeRules!!.any { it.permissions!!.contains("notifications") }) {
            // Initialize obscure notification channel
            val obscureNotificationChannel = NotificationChannel(
                obscureChannelID,
                "Obscure Notification Channel",
                NotificationManager.IMPORTANCE_HIGH
            )

            notificationManager.createNotificationChannel(obscureNotificationChannel)

            obscureNotification = NotificationCompat.Builder(this, obscureChannelID)
                .setContentTitle("Un'app ha inviato una notifica")
                .setContentText("Clicca qui per i dettagli")
                .setSmallIcon(R.drawable.icon_safety)
                .setDefaults(Notification.DEFAULT_SOUND or Notification.DEFAULT_VIBRATE)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setAutoCancel(true)
                .build()

            val notificationIntent = Intent(this, NotificationListener::class.java)
            startService(notificationIntent)
        }

        // Initialize signal violation notification
        signalNotificationChannel = NotificationChannel(
            signalChannelID,
            "Signal Violation Channel",
            NotificationManager.IMPORTANCE_HIGH
        )

        notificationManager.createNotificationChannel(signalNotificationChannel)

        signalNotification = NotificationCompat.Builder(this, signalChannelID)
            .setContentTitle("Rilevata violazione di una regola")
            .setContentText("Un\'app potrebbe avere avuto accesso ad un\'autorizzazione definita in una regola")
            .setSmallIcon(R.drawable.icon_safety)
            .setOngoing(true)
            .setDefaults(Notification.DEFAULT_SOUND or Notification.DEFAULT_VIBRATE)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(true)
            .build()

        return START_STICKY
    }

    private fun createNotification(): Notification {
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationChannel = NotificationChannel(
            monitorChannelID,
            "Monitor Service Channel",
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(notificationChannel)

        return Notification.Builder(this, monitorChannelID)
            .setContentTitle("Privacy manager")
            .setContentText("Servizio di monitoraggio attivo")
            .setSmallIcon(R.drawable.icon_safety)
            .build()
    }

    private fun monitorRunningApps() {
        // Check if permission is granted
        if (!hasUsageStatsPermission()) {
            return
        }

        if (activeRules.isNullOrEmpty()) {
            return
        }

        // Active rules with satisfied conditions
        rulesToMonitor = mutableListOf()

        // For each rule, check if it is not already in the rulesToMonitor list (avoid duplicates)
        for (r in activeRules!!) {
            // If no parameter (position, time slot, network, bluetooth, battery) is defined, we have to monitor
            if (r.positions == null && r.timeSlot == null && r.networks == null && r.bt == null && r.battery == null) {
                rulesToMonitor!!.add(r)
            }
            // Otherwise, all the defined parameters have to be satisfied
            else if (checkParameters(r)) {
                rulesToMonitor!!.add(r)
            }
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
            for (rule in rulesToMonitor!!) {
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
        for (rule in rulesToMonitor!!) {
            // Check location permission and if the rule has some running app
            if (rule.permissions!!.contains("location") && rule.packageNames!!.any { it in runningApps }) {
                monitorLocation(rule.packageNames!!.filter { it in runningApps }, rule.name!!, rule.action!!)
            }
            // Check calendar permission and if the rule has some running app
            if (rule.permissions!!.contains("calendar") && rule.packageNames!!.any { it in runningApps }) {
                monitorCalendar(rule.packageNames!!.filter { it in runningApps }, rule.name!!, rule.action!!)
            }
            // Check camera permission and if the rule has some running app
            if (rule.permissions!!.contains("camera") && rule.packageNames!!.any { it in runningApps }) {
                monitorCamera(rule.packageNames!!.filter { it in runningApps }, rule.name!!, rule.action!!)
            }
        }
    }

    // Check that all the parameters that are defined (position, time slot, network, bluetooth, battery) are satisfied
    private fun checkParameters(rule: Rule) : Boolean {

        if (!rule.positions.isNullOrEmpty()) {
            monitorPositions(rule.positions!!)

            if (!isNear) {
                return false
            }
        }

        if (rule.timeSlot != null && rule.timeSlot!!.time.first != "") {
            if (!monitorTimeSlot(rule.timeSlot!!)) {
                return false
            }
        }

        if (!rule.networks.isNullOrEmpty()) {
            monitorNetwork(rule.networks!!)

            if (!isConnected) {
                return false
            }
        }

        if (!rule.bt.isNullOrEmpty()) {
            if (!monitorBT(rule.bt!!)) {
                return false
            }
        }

        if (rule.battery != null) {
            if (!monitorBattery(rule.battery!!)) {
                return false
            }
        }

        return true
    }

    private fun monitorLocation(listApps : List<String>, ruleName: String, ruleAction: String) {
        // Get a reference to the PackageManager
        val packageManager = packageManager

        for (app in listApps) {
            val packageInfo = packageManager.getPackageInfo(app, PackageManager.GET_PERMISSIONS)

            val listPermissions = packageInfo.requestedPermissions

            if (listPermissions!!.contains("android.permission.ACCESS_COARSE_LOCATION") || listPermissions.contains("android.permission.ACCESS_FINE_LOCATION")) {
                signalNotification(app, ruleName, ruleAction, "location")
            }
        }
    }

    private fun monitorCalendar(listApps : List<String>, ruleName: String, ruleAction: String) {
        val app = listApps[0]

        // creates and starts a new thread set up as a looper
        val thread = HandlerThread("CalendarHandlerThread")
        thread.start()

        // creates the handler using the passed looper
        val handler = Handler(thread.looper)

        calendarObserver = CalendarObserver(contentResolver, handler)

        calendarObserver.startObserving(app, ruleName, ruleAction)
    }

    private fun monitorCamera(listApps : List<String>, ruleName: String, ruleAction: String) {
        val app = listApps[0]

        // Get an instance of the CameraManager
        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        // Create an availability callback
        val availabilityCallback = object : CameraManager.AvailabilityCallback() {
            override fun onCameraAvailable(cameraId: String) {
                // This is called when a camera device becomes available to open
            }

            override fun onCameraUnavailable(cameraId: String) {
                // This is called when a camera device becomes unavailable to open
                signalNotification(app, ruleName, ruleAction, "camera")
            }
        }

        // Register the availability callback
        cameraManager.registerAvailabilityCallback(availabilityCallback, null)
        cameraManager.unregisterAvailabilityCallback(availabilityCallback)
    }

    @SuppressLint("MissingPermission")
    private fun monitorPositions(listPositions: List<CustomAddress>) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Request the current location of the user
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    comparePositions(location, listPositions)
                }
            }
    }

    private fun comparePositions(currentPosition: Location, listPositions: List<CustomAddress>) {
        // Define a constant for the distance threshold in meters
        val threshold = 100

        for (position in listPositions) {
            val distance = FloatArray(1)

            Location.distanceBetween(currentPosition.latitude, currentPosition.longitude, position.latitude!!, position.longitude!!, distance)

            if (distance[0] <= threshold) {
                isNear = true
                return
            }
        }
    }

    private fun monitorTimeSlot(timeSlot: TimeSlot) : Boolean {
        // Get info about current date and time
        val currentDateTime = LocalDateTime.now()
        val currentDay = currentDateTime.dayOfWeek.toString().lowercase()
        val currentTime = currentDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))

        // Check if the current day is in the list of defined days
        if (!timeSlot.days.contains(currentDay)) {
            return false
        }

        // Check if the current time is within the specified time range
        val startTime = timeSlot.time.first
        val endTime = timeSlot.time.second

        val currentTimeInMinutes = convertToMinutes(currentTime)
        val startTimeInMinutes = convertToMinutes(startTime)
        val endTimeInMinutes = convertToMinutes(endTime)

        return currentTimeInMinutes in startTimeInMinutes..endTimeInMinutes
    }

    private fun convertToMinutes(time: String): Int {
        val (hours, minutes) = time.split(":").map { it.toInt() }
        return hours * 60 + minutes
    }

    private fun monitorNetwork(listNetwork: List<String>) {
        // Get an instance of ConnectivityManager and WifiManager
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val wifiManager = getSystemService(Context.WIFI_SERVICE) as WifiManager

        // Create a network request for wi-fi network
        val wifiNetworkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()

        // Get the network capabilities of the active network
        val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)

        if (listNetwork.contains("mobile_data")) {
            // Check if the network has cellular transport type
            isConnected = networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ?: false
            return
        }

        // Create a network callback for wi-fi network
        val wifiNetworkCallback = object : ConnectivityManager.NetworkCallback(
            FLAG_INCLUDE_LOCATION_INFO
        ) {
            // This method is called when the wi-fi network is available
            override fun onAvailable(network: Network) {
                super.onAvailable(network)

                // Get the connection info of the wi-fi network
                val wifiConnectionInfo = wifiManager.connectionInfo

                // Get the SSID of the wi-fi network
                val wifiSSID = wifiConnectionInfo.ssid

                // Remove the quotation marks from the SSID
                val wifiSSIDWithoutQuotes = wifiSSID.replace("\"", "")

                // Compare the SSID with the specific wi-fi network name to check
                if (listNetwork.contains(wifiSSIDWithoutQuotes) || wifiSSID == "<unknown ssid>") {
                    // The user is connected to the specific wi-fi network
                    isConnected = true
                }
            }
        }

        // Register the network callback for wi-fi network
        connectivityManager.registerNetworkCallback(wifiNetworkRequest, wifiNetworkCallback)

        // Unregister the network callback for wi-fi network when you don't need it anymore
        //connectivityManager.unregisterNetworkCallback(wifiNetworkCallback)
    }

    @SuppressLint("MissingPermission")
    private fun monitorBT(listBT: List<String>) : Boolean {
        // Get the BluetoothManager
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

        // Get the list of connected devices
        val connectedDevices = bluetoothManager.getConnectedDevices(BluetoothProfile.GATT)
        // Check if the list is not empty
        if (connectedDevices.isNotEmpty()) {
            // Loop through the list
            for (device in connectedDevices) {
                // Get the device name
                val deviceName = device.name
                // Check if the device connected is defined in the saved list
                if (listBT.any { it == deviceName }) {
                    return true
                }
            }
        }

        return false
    }

    private fun monitorBattery(level: Int) : Boolean {
        val bm = getSystemService(BATTERY_SERVICE) as BatteryManager
        val batLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

        return batLevel < level
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

    fun stopForegroundService() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()

        activeRules = null
        rulesToMonitor = null

        // Stop notification service as well
        //val notificationIntent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
        //notificationIntent.addFlags(FLAG_ACTIVITY_NEW_TASK)
        //startActivity(notificationIntent)

        stopForeground(STOP_FOREGROUND_REMOVE) // Stop as a foreground service
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {
        //var canNotify: Boolean = false
        // Save info about violation revealed
        // If a violation has already bean signaled, then don't send the notification
        // A violation is identified by the pair (app, permission)
        var pendingViolationsInfo = mutableListOf<Pair<String, String>>()
    }
}

fun signalNotification(app: String, ruleName: String, ruleAction: String, permission: String) {
    if (!MonitorManager.pendingViolationsInfo.contains(Pair(app, permission))) {
        MonitorManager.pendingViolationsInfo.add(Pair(app, permission))

        if (ruleAction == "close_app" || ruleAction == "block_notification") {
            // Kill the app with the given package name
            activityManager.killBackgroundProcesses(app)
        }

        // On click reset the canNotify flag
        val broadcastIntent = Intent(context, NotificationClickReceiver::class.java)
        broadcastIntent.action = NOTIFICATION_ACTION_RESET_FLAG

        // Necessary to have a unique notification id
        val timestamp = System.currentTimeMillis().toInt()

        val broadcastPendingIntent = PendingIntent.getBroadcast(context, timestamp, broadcastIntent, PendingIntent.FLAG_MUTABLE)

        // And also start the violation activity
        val signalIntent = Intent(context, ViolationActivity::class.java)
        signalIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        signalIntent.putExtra("ruleName", ruleName)
        signalIntent.putExtra("pkg", app)
        signalIntent.putExtra("action", ruleAction)

        val signalPendingIntent = PendingIntent.getActivity(context, timestamp, signalIntent, PendingIntent.FLAG_MUTABLE)

        // Combine the two intents
        val stackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addNextIntentWithParentStack(signalIntent)
        stackBuilder.addNextIntent(broadcastIntent)

        val intents = stackBuilder.intents

        val contentIntent = PendingIntent.getActivities(context, timestamp, intents, PendingIntent.FLAG_MUTABLE)

        signalNotification.contentIntent = contentIntent

        notificationManager.notify(timestamp, signalNotification)
    }
}

// Extend the NotificationListenerService class and override the methods
class NotificationListener : NotificationListenerService() {

    override fun onBind(intent: Intent): IBinder? {
        return super.onBind(intent)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val notificationRules = mutableListOf<Rule>()

        // Consider only rules with permission "notifications"
        if (!rulesToMonitor.isNullOrEmpty()) {
            for (r in rulesToMonitor!!) {
                if (r.permissions!!.contains("notifications")) {
                    notificationRules.add(r)
                }
            }
        }

        // If there is at least one rule
        if (notificationRules.isNotEmpty()) {
            // If the intercepted notification is posted by a tracked app
            if (notificationRules.any { it.packageNames!!.contains(sbn.packageName) }) {
                // Get the rule
                val rule = notificationRules.first { it.packageNames!!.contains(sbn.packageName) }

                // Get the app
                val app = sbn.packageName
                // Get the notification object
                val notification = sbn.notification
                // Get the notification title
                val title = notification.extras.getString(Notification.EXTRA_TITLE)
                // Get the notification content
                val content = notification.extras.getString(Notification.EXTRA_TEXT)

                // Cancel the notification arrived
                cancelNotification(sbn.key)

                // Signal notification received
                if (rule.action!! == "obscure_notification") {
                    // Start the notification activity
                    val intent = Intent(context, NotificationActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                    intent.putExtra("ruleName", rule.name)
                    intent.putExtra("pkg", app)
                    intent.putExtra("title", title)
                    intent.putExtra("content", content)

                    // Necessary to have a unique notification id
                    val timestamp = System.currentTimeMillis().toInt()

                    val pendingIntent = PendingIntent.getActivity(context, timestamp, intent, PendingIntent.FLAG_MUTABLE)

                    obscureNotification.contentIntent = pendingIntent

                    notificationManager.notify(timestamp, obscureNotification)
                }
                // Or block it
                else {
                    // Start the notification activity
                    val intent = Intent(context, ViolationActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                    intent.putExtra("action", rule.action)
                    intent.putExtra("isNotification", true)
                    intent.putExtra("ruleName", rule.name)
                    intent.putExtra("pkg", app)

                    // Necessary to have a unique notification id
                    val timestamp = System.currentTimeMillis().toInt()

                    val pendingIntent = PendingIntent.getActivity(context, timestamp, intent, PendingIntent.FLAG_MUTABLE)

                    obscureNotification.contentIntent = pendingIntent

                    notificationManager.notify(timestamp, obscureNotification)
                }
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // Do something when the notification is removed
    }
}

class CalendarObserver(private val contentResolver: ContentResolver, handler: Handler) {

    private val uri: Uri = CalendarContract.Events.CONTENT_URI

    private lateinit var app: String
    private lateinit var ruleName: String
    private lateinit var ruleAction: String

    private val observer = object : ContentObserver(handler) {
        override fun onChange(selfChange: Boolean) {
            signalNotification(app, ruleName, ruleAction, "calendar")
        }
    }

    private fun observe() {
        contentResolver.registerContentObserver(uri, true, observer)
    }

    fun startObserving(app: String, ruleName: String, ruleAction: String) {
        this.app = app
        this.ruleName = ruleName
        this.ruleAction = ruleAction

        observe()
    }

    fun stopObserving() {
        contentResolver.unregisterContentObserver(observer)
    }
}

class NotificationClickReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        // This function will be executed when the user clicks on the notification
        MonitorManager.pendingViolationsInfo.clear()
    }
}