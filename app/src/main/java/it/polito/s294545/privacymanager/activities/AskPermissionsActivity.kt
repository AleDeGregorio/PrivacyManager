package it.polito.s294545.privacymanager.activities

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Bundle
import android.provider.Settings
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import it.polito.s294545.privacymanager.R
import it.polito.s294545.privacymanager.utilities.PreferencesManager
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig
import uk.co.deanwild.materialshowcaseview.ShowcaseTooltip


class AskPermissionsActivity : AppCompatActivity() {

    private val TUTORIAL_ID = "Tutorial ask permissions"

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

    private lateinit var infoTextView: TextView

    private lateinit var confirmButton: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ask_permissions)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // Get toolbar
        val toolbarLayout = findViewById<ConstraintLayout>(R.id.toolbarLayout)
        val toolbar = toolbarLayout.findViewById<MaterialToolbar>(R.id.toolbar)

        // Set the help icon
        toolbar.inflateMenu(R.menu.toolbar_menu)
        toolbar.overflowIcon = ContextCompat.getDrawable(this, R.drawable.icon_help)

        toolbar.setOnMenuItemClickListener {
            showTutorial()
            true
        }

        // Manage user's back pressure
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Do nothing
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        infoTextView = findViewById(R.id.ask_permissions_info)

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
            val fromHome = intent.extras?.getBoolean("fromHome")

            if (fromHome == null) {
                goToMainApplication()
            }
        }

        if (!PreferencesManager.getAskPermissionsTutorialShown(this)) {
            showTutorial()
            PreferencesManager.saveAskPermissionsTutorialShown(this)
        }
    }

    private fun showTutorial() {
        MaterialShowcaseView.resetSingleUse(this, TUTORIAL_ID)

        val config = ShowcaseConfig()
        config.delay = 500

        val sequence = MaterialShowcaseSequence(this, TUTORIAL_ID)

        sequence.addSequenceItem(
            MaterialShowcaseView.Builder(this)
                .setTarget(infoTextView)
                .withoutShape()
                .setContentText(getText("Benvenuto!\n\n", "Lo scopo di questa applicazione è permetterti di gestire la privacy e la sicurezza dei tuoi dati, creando delle regole personalizzate per monitorare il comportamento delle applicazioni che utilizzi"))
                .setContentTextColor(resources.getColor(R.color.white))
                .setDismissText("Tocca per continuare")
                .setDismissOnTouch(true)
                .setSkipText("Salta")
                .renderOverNavigationBar()
                .build()
        )

        sequence.addSequenceItem(
            MaterialShowcaseView.Builder(this)
                .setTarget(notificationsButton)
                .withoutShape()
                .setContentText(getText("Prima di cominiciare\n\n", "È necessario accedere ad alcune funzionalità del tuo dispositivo. Ti verranno adesso spiegati i motivi per ciasuna funzionalità richiesta"))
                .setContentTextColor(resources.getColor(R.color.white))
                .setDismissText("Tocca per continuare")
                .setDismissOnTouch(true)
                .setSkipText("Salta")
                .renderOverNavigationBar()
                .build()
        )

        val toolTipLocation = ShowcaseTooltip.build(this)
            .corner(30)
            .text("Per permetterti di salvare dei luoghi di tuo interesse e monitorare se un'app sta utilizzando il servizio")

        sequence.addSequenceItem(
            MaterialShowcaseView.Builder(this)
                .setTarget(locationButton)
                .setToolTip(toolTipLocation)
                .withRectangleShape()
                .setTooltipMargin(30)
                .setShapePadding(50)
                .setDismissOnTouch(true)
                .setSkipText("Salta")
                .renderOverNavigationBar()
                .build()
        )

        val toolTipCalendar = ShowcaseTooltip.build(this)
            .corner(30)
            .text("Per monitorare se un'app sta utilizzando il servizio, modificando un evento del calendario")

        sequence.addSequenceItem(
            MaterialShowcaseView.Builder(this)
                .setTarget(calendarButton)
                .setToolTip(toolTipCalendar)
                .withRectangleShape()
                .setTooltipMargin(30)
                .setShapePadding(50)
                .setDismissOnTouch(true)
                .setSkipText("Salta")
                .renderOverNavigationBar()
                .build()
        )

        val toolTipBluetooth = ShowcaseTooltip.build(this)
            .corner(30)
            .text("Per permetterti di selezionare dei dispositivi bluetooth che hai salvato")

        sequence.addSequenceItem(
            MaterialShowcaseView.Builder(this)
                .setTarget(bluetoothButton)
                .setToolTip(toolTipBluetooth)
                .withRectangleShape()
                .setTooltipMargin(30)
                .setShapePadding(50)
                .setDismissOnTouch(true)
                .renderOverNavigationBar()
                .build()
        )

        val toolTipNotifications = ShowcaseTooltip.build(this)
            .corner(30)
            .text("Per monitorare le notifiche di altre app, oscurandole o bloccandole secondo i tuoi parametri")

        sequence.addSequenceItem(
            MaterialShowcaseView.Builder(this)
                .setTarget(notificationsButton)
                .setToolTip(toolTipNotifications)
                .withRectangleShape()
                .setTooltipMargin(30)
                .setShapePadding(50)
                .setDismissOnTouch(true)
                .renderOverNavigationBar()
                .build()
        )

        val toolTipUsage = ShowcaseTooltip.build(this)
            .corner(30)
            .text("Per monitorare le applicazioni in esecuzione e avvisarti nel caso qualcuna di queste stia violando la regola che hai definito")

        sequence.addSequenceItem(
            MaterialShowcaseView.Builder(this)
                .setTarget(usageButton)
                .setToolTip(toolTipUsage)
                .withRectangleShape()
                .setTooltipMargin(30)
                .setShapePadding(50)
                .setDismissOnTouch(true)
                .renderOverNavigationBar()
                .build()
        )

        val toolTipConfirm = ShowcaseTooltip.build(this)
            .corner(30)
            .text("Quando hai finito, clicca qui per iniziare ad utilizzare l'applicazione")

        sequence.addSequenceItem(
            MaterialShowcaseView.Builder(this)
                .setTarget(confirmButton)
                .setToolTip(toolTipConfirm)
                .withCircleShape()
                .setTooltipMargin(30)
                .setShapePadding(50)
                .setDismissOnTouch(true)
                .renderOverNavigationBar()
                .build()
        )

        sequence.start()
    }

    // Fix context text opacity problem in tutorial
    private fun getText(title: String, content: String): SpannableString {
        val spannableString = SpannableString(title + content)
        // 1.5f for title i.e. default text size * 1.5f
        spannableString.setSpan(RelativeSizeSpan(1.5f), 0, title.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        // 0.85f for content i.e. default text size * 0.85f, smaller than the title
        spannableString.setSpan(RelativeSizeSpan(1.0f), title.length + 1, (title + content).length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannableString
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
        button.setTextColor(resources.getColor(R.color.white))
        button.setIconTintResource(R.color.white)
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_right_icon -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}