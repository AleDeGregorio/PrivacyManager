package it.polito.s294545.privacymanager.utilities

import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageStatsManager
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
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
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import it.polito.s294545.privacymanager.R
import it.polito.s294545.privacymanager.customDataClasses.CustomAddress
import it.polito.s294545.privacymanager.customDataClasses.Rule
import it.polito.s294545.privacymanager.customDataClasses.TimeSlot
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// List of all currently active rules
var activeRules: List<Rule>? = null
// List of currently active rules that have to be monitored (all parameters respected)
var rulesToMonitor: MutableList<Rule>? = null

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
        val retrievedRules = intent?.extras?.get("activeRules")

        activeRules = Json.decodeFromString(retrievedRules.toString())

        startForeground(NOTIFICATION_ID, createNotification()) // Start as a foreground service
        // Initiate monitoring
        handler.post(monitorRunnable)

        if (activeRules!!.any { it.permissions!!.contains("notifications") }) {
            val notificationIntent = Intent(this, NotificationListener::class.java)
            startService(notificationIntent)
        }

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
                monitorLocation(rule.packageNames!!.filter { it in runningApps })
            }
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

    // Check that all the parameters that are defined (position, time slot, network, bluetooth, battery) are satisfied
    private fun checkParameters(rule: Rule) : Boolean {
        if (rule.positions != null) {
            monitorPositions(rule.positions!!)

            if (!isNear) {
                return false
            }
        }

        if (rule.timeSlot != null) {
            if (!monitorTimeSlot(rule.timeSlot!!)) {
                return false
            }
        }

        if (rule.networks != null) {
            monitorNetwork(rule.networks!!)

            if (!isConnected) {
                return false
            }
        }

        if (rule.bt != null) {
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

    private fun monitorLocation(listApps : List<String>) {
        // Get a reference to the PackageManager
        val packageManager = packageManager

        for (app in listApps) {
            val packageInfo = packageManager.getPackageInfo(app, PackageManager.GET_PERMISSIONS)

            val listPermissions = packageInfo.requestedPermissions

            if (listPermissions!!.contains("android.permission.ACCESS_COARSE_LOCATION") || listPermissions.contains("android.permission.ACCESS_FINE_LOCATION")) {
                Log.d("myapp", "location accessed")
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

        activeRules = null

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
                // Get the notification object
                val notification = sbn.notification
                // Get the notification title
                val title = notification.extras.getString(Notification.EXTRA_TITLE)
                // Get the notification text
                val text = notification.extras.getString(Notification.EXTRA_TEXT)
                // Do something with the notification data
                Log.d("myapp", "notification: ${sbn.packageName}")

                //cancelNotification(sbn.key)

                /*
                val intent = Intent("android.service.notification.NotificationListenerService")
                intent.putExtra("Notification Title", notification)
                sendBroadcast(intent)
                 */
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // Do something when the notification is removed
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