package it.polito.s294545.privacymanager.activities

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.view.View
import android.view.View.GONE
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import it.polito.s294545.privacymanager.R
import it.polito.s294545.privacymanager.customDataClasses.Rule
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.text.StringBuilder

class SavedRuleActivity : AppCompatActivity() {

    private lateinit var rule: Rule

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_rule)

        // Get toolbar
        val toolbarLayout = findViewById<ConstraintLayout>(R.id.toolbarLayout)
        val toolbar = toolbarLayout.findViewById<MaterialToolbar>(R.id.toolbar)

        // Set the navigation icon
        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.icon_left_arrow)
        toolbar.setNavigationIconTint(resources.getColor(R.color.white))

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        // Get saved rule info
        val retrievedRule = intent.extras?.get("rule")
        rule = Json.decodeFromString(retrievedRule.toString())

        // Set rule name title
        val ruleName = findViewById<TextView>(R.id.rule_name)
        ruleName.text = rule.name

        // Set rule permissions
        val permissions = findViewById<TextView>(R.id.permissions_text)
        permissions.text = getPermissionsString()

        // Set rule applications
        val apps = findViewById<TextView>(R.id.apps_text)
        apps.text = getAppsString()

        // Set rule action
        val action = findViewById<TextView>(R.id.action_text)
        action.text = getActionString()

        // Set rule positions
        val positionsCard = findViewById<MaterialCardView>(R.id.positions_card)
        if (rule.positions.isNullOrEmpty()) {
            positionsCard.visibility = GONE
        }
        else {
            val positions = findViewById<TextView>(R.id.positions_text)
            positions.text = getPositionString()
        }

        // Set rule time slot
        val timeSlotCard = findViewById<MaterialCardView>(R.id.time_slot_card)
        if (rule.timeSlot == null || rule.timeSlot!!.days.isEmpty()) {
            timeSlotCard.visibility = GONE
        }
        else {
            val timeSlot = findViewById<TextView>(R.id.time_slot_text)
            timeSlot.text = getTimeSlotString()
        }

        // Set rule parameters
        val parametersCard = findViewById<MaterialCardView>(R.id.parameters_card)
        if (rule.networks.isNullOrEmpty() && rule.bt.isNullOrEmpty() && rule.battery == null) {
            parametersCard.visibility = GONE
        }
        else {
            // Set rule networks
            val networksTitle = findViewById<TextView>(R.id.networks_title)
            val networks = findViewById<TextView>(R.id.networks_text)
            if (rule.networks.isNullOrEmpty()) {
                networksTitle.visibility = GONE
                networks.visibility = GONE
            }
            else {
                networks.text = getNetworkString()
            }

            // Set rule bluetooth
            val bluetoothTitle = findViewById<TextView>(R.id.bluetooth_title)
            val bluetooth = findViewById<TextView>(R.id.bluetooth_text)
            if (rule.bt.isNullOrEmpty()) {
                bluetoothTitle.visibility = GONE
                bluetooth.visibility = GONE
            }
            else {
                bluetooth.text = getBluetoothString()
            }

            // Set rule battery
            val batteryTitle = findViewById<TextView>(R.id.battery_title)
            val battery = findViewById<TextView>(R.id.battery_text)
            if (rule.battery == null) {
                batteryTitle.visibility = GONE
                battery.visibility = GONE
            }
            else {
                battery.text = getBatteryString()
            }
        }

        // Manage start rule
        val startRuleButton = findViewById<FloatingActionButton>(R.id.start_rule_button)
        startRuleButton.setOnClickListener { v -> showPopupStartRule(v) }
    }

    private fun getPermissionsString() : String {
        val permissionsStr = StringBuilder("")

        for (p in rule.permissions!!) {
            when (p) {
                "notifications" -> permissionsStr.append("Notifiche, ")
                "location" -> permissionsStr.append("Localizzazione, ")
                "calendar" -> permissionsStr.append("Calendario, ")
                "camera" -> permissionsStr.append("Fotocamera, ")
                "sms" -> permissionsStr.append("SMS, ")
            }
        }

        val tmp = permissionsStr.toString()
        return tmp.removeRange(tmp.lastIndex - 1, tmp.lastIndex)
    }

    private fun getAppsString() : String {
        val appsStr = StringBuilder("")

        for (a in rule.apps!!) {
            appsStr.append("$a, ")
        }

        val tmp = appsStr.toString()
        return tmp.removeRange(tmp.lastIndex - 1, tmp.lastIndex)
    }

    private fun getActionString() : String {
        return when (rule.action) {
            "signal_app" -> "Segnala applicazione"
            "close_app" -> "Chiudi applicazione"
            "obscure_notification" -> "Oscura notifica"
            "block_notification" -> "Chiudi app e blocca notifica"
            else -> ""
        }
    }

    private fun getPositionString() : String {
        val positionsStr = StringBuilder("")

        for (p in rule.positions!!) {
            positionsStr.append("- ${p.address}.\n\n")
        }

        val tmp = positionsStr.toString()
        return tmp.replaceAfterLast(".", "")
    }

    private fun getTimeSlotString() : String {
        val daysStr = StringBuilder("Giorni: ")

        for (d in rule.timeSlot!!.days) {
            when (d) {
                "monday" -> daysStr.append("Lun, ")
                "tuesday" -> daysStr.append("Mar, ")
                "wednesday" -> daysStr.append("Mer, ")
                "thursday" -> daysStr.append("Gio, ")
                "friday" -> daysStr.append("Ven, ")
                "saturday" -> daysStr.append("Sab, ")
                "sunday" -> daysStr.append("Dom, ")
            }
        }

        val tempDays = daysStr.removeRange(daysStr.lastIndex - 1, daysStr.lastIndex)
        return "$tempDays\nOra: ${rule.timeSlot!!.time.first}-${rule.timeSlot!!.time.second}"
    }

    private fun getNetworkString() : String {
        val networksStr = StringBuilder("")

        for (n in rule.networks!!) {
            if (n == "mobile_data") {
                networksStr.append("Connessione dati, ")
            }
            else {
                networksStr.append("$n, ")
            }
        }

        val tmp = networksStr.toString()
        return tmp.removeRange(tmp.lastIndex - 1, tmp.lastIndex)
    }

    private fun getBluetoothString() : String {
        val bluetoothStr = StringBuilder("")

        for (bt in rule.bt!!) {
            bluetoothStr.append("$bt, ")
        }

        val tmp = bluetoothStr.toString()
        return tmp.removeRange(tmp.lastIndex - 1, tmp.lastIndex)
    }

    private fun getBatteryString() : String {
        return "<${rule.battery}%"
    }
}

@SuppressLint("ClickableViewAccessibility")
fun showPopupStartRule(view: View) {
    val (popupView, popupWindow) = managePopup(view, R.layout.popup_start_rule)

    // Initialize the elements of our window, install the handler
    val buttonStartRule = popupView.findViewById<Button>(R.id.start_rule_button)
    buttonStartRule.setOnClickListener {
        // Start rule
    }

    val buttonCancel = popupView.findViewById<Button>(R.id.cancel_button)
    buttonCancel.setOnClickListener {
        popupWindow.dismiss()
    }


    // Handler for clicking on the inactive zone of the window
    popupView.setOnTouchListener { v, event -> // Close the window when clicked
        popupWindow.dismiss()
        true
    }
}