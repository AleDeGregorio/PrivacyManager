package it.polito.s294545.privacymanager.activities

import android.Manifest
import android.app.AppOpsManager
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Bundle
import android.provider.Settings
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import it.polito.s294545.privacymanager.R

class AskPermissionsActivity : AppCompatActivity() {

    private val NUMBER_OF_PERMISSIONS = 5
    // Count the permissions granted. If there are NUMBER_OF_PERMISSIONS of them, then ok
    private var countPermissions = 0

    // Permissions
    private val LOCATION_PERMISSION_REQUEST = 101
    private val LOCATION_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private val CALENDAR_PERMISSION_REQUEST = 102
    private val CALENDAR_PERMISSIONS = arrayOf(
        Manifest.permission.READ_CALENDAR,
        Manifest.permission.WRITE_CALENDAR
    )

    private val BLUETOOTH_PERMISSION_REQUEST = 103
    private val BLUETOOTH_PERMISSIONS = arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_ADVERTISE,
        Manifest.permission.BLUETOOTH_CONNECT
    )

    private val NOTIFICATION_PERMISSION_REQUEST = 104

    private val PACKAGE_USAGE_STATS_PERMISSION_REQUEST = 105

    private val KILL_PROCESS_PERMISSION_REQUEST = 106

    // Permission buttons
    private lateinit var locationButton: ExtendedFloatingActionButton
    private lateinit var calendarButton: ExtendedFloatingActionButton
    private lateinit var bluetoothButton: ExtendedFloatingActionButton
    private lateinit var notificationsButton: ExtendedFloatingActionButton
    private lateinit var usageButton: ExtendedFloatingActionButton

    private lateinit var confirmButton: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ask_permissions)

        // Manage user's back pressure
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Do nothing
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        // Confirm permissions button
        confirmButton = findViewById(R.id.confirm_permissions)

        // Manage permission buttons
        locationButton = findViewById(R.id.location_permission)
        if (hasLocationPermission()) {
            setPermissionButton(locationButton)
        }
        else {
            locationButton.setOnClickListener {
                requestLocationPermission()
            }
        }

        calendarButton = findViewById(R.id.calendar_permission)
        if (hasCalendarPermission()) {
            setPermissionButton(calendarButton)
        }
        else {
            calendarButton.setOnClickListener {
                requestCalendarPermission()
            }
        }

        bluetoothButton = findViewById(R.id.bluetooth_permission)
        if (hasBluetoothPermission()) {
            setPermissionButton(bluetoothButton)
        }
        else {
            bluetoothButton.setOnClickListener {
                requestBluetoothPermission()
            }
        }

        notificationsButton = findViewById(R.id.notifications_permission)
        if (hasNotificationPermission()) {
            setPermissionButton(notificationsButton)
        }
        else {
            notificationsButton.setOnClickListener {
                requestNotificationPermission()
            }
        }

        usageButton = findViewById(R.id.usage_permission)
        if (hasUsageStatsPermission()) {
            setPermissionButton(usageButton)
        }
        else {
            usageButton.setOnClickListener {
                requestUsageStatsPermission()
            }
        }

        if (!hasKillProcessPermission()) {
            requestKillProcessPermission()
        }

        if (countPermissions == NUMBER_OF_PERMISSIONS) {
            goToMainApplication()
        }
    }

    private fun goToMainApplication() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }

    private fun setConfirmButton() {
        confirmButton.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.primary))
        confirmButton.isClickable = true
        confirmButton.isFocusable = true

        confirmButton.setOnClickListener {
            goToMainApplication()
        }
    }

    private fun setPermissionButton(button: ExtendedFloatingActionButton) {
        countPermissions++

        if (countPermissions == NUMBER_OF_PERMISSIONS) {
            setConfirmButton()
        }

        button.setBackgroundColor(resources.getColor(R.color.primary))
    }

    // ----- Check if permission has been granted -----

    private fun hasLocationPermission() : Boolean {
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasCalendarPermission() : Boolean {
        return checkSelfPermission(Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasBluetoothPermission() : Boolean {
        return checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasNotificationPermission() : Boolean {
        return NotificationManagerCompat.getEnabledListenerPackages(this).contains(packageName)
    }

    private fun hasUsageStatsPermission() : Boolean {
        val appOps = getSystemService(APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun hasKillProcessPermission() : Boolean {
        return checkSelfPermission(Manifest.permission.KILL_BACKGROUND_PROCESSES) == PackageManager.PERMISSION_GRANTED
    }

    // ----- End of check if permission granted -----

    // ----- Request permission -----

    private fun requestLocationPermission() {
        requestPermissions(LOCATION_PERMISSIONS, LOCATION_PERMISSION_REQUEST)
    }

    private fun requestCalendarPermission() {
        requestPermissions(CALENDAR_PERMISSIONS, CALENDAR_PERMISSION_REQUEST)
    }

    private fun requestBluetoothPermission() {
        requestPermissions(BLUETOOTH_PERMISSIONS, BLUETOOTH_PERMISSION_REQUEST)
    }

    private fun requestNotificationPermission() {
        val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
        startActivityForResult(intent, NOTIFICATION_PERMISSION_REQUEST)
    }

    private fun requestUsageStatsPermission() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        startActivityForResult(intent, PACKAGE_USAGE_STATS_PERMISSION_REQUEST)
    }

    private fun requestKillProcessPermission() {
        requestPermissions(arrayOf(Manifest.permission.KILL_BACKGROUND_PROCESSES), KILL_PROCESS_PERMISSION_REQUEST)
    }

    // ----- End of request permission -----

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            LOCATION_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setPermissionButton(locationButton)
                }
            }
            CALENDAR_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setPermissionButton(calendarButton)
                }
            }
            BLUETOOTH_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setPermissionButton(bluetoothButton)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            NOTIFICATION_PERMISSION_REQUEST -> {
                if (NotificationManagerCompat.getEnabledListenerPackages(this).contains(packageName)) {
                    setPermissionButton(notificationsButton)
                }
            }
            PACKAGE_USAGE_STATS_PERMISSION_REQUEST -> {
                val appOps = getSystemService(APP_OPS_SERVICE) as AppOpsManager
                val mode = appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(),
                    packageName
                )
                if (mode == AppOpsManager.MODE_ALLOWED) {
                    setPermissionButton(usageButton)
                }
            }
        }
    }
}