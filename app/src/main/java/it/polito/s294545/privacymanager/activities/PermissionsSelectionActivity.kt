package it.polito.s294545.privacymanager.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import it.polito.s294545.privacymanager.R
import it.polito.s294545.privacymanager.customDataClasses.Rule
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class PermissionsSelectionActivity : AppCompatActivity() {

    private val savedPermissions = mutableListOf<String>()
    private lateinit var forwardButton : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permissions_selection)

        // Get toolbar
        val toolbarLayout = findViewById<ConstraintLayout>(R.id.toolbarLayout)
        val toolbar = toolbarLayout.findViewById<MaterialToolbar>(R.id.toolbar)

        // Set the navigation icon
        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.icon_cross)
        toolbar.setNavigationIconTint(resources.getColor(R.color.white))

        toolbar.setNavigationOnClickListener { manageBackNavigation() }

        // Manage cancel button
        val cancelButton = findViewById<Button>(R.id.cancel_button)
        cancelButton.setOnClickListener { manageBackNavigation() }

        // Manage user's back pressure
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                manageBackNavigation()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        // ----- Manage permission buttons -----

        // Notifications
        val notificationsButton = findViewById<ExtendedFloatingActionButton>(R.id.notification_permission)
        notificationsButton.setOnClickListener { togglePermissionSelection(it, "notifications") }

        // Location
        val locationButton = findViewById<ExtendedFloatingActionButton>(R.id.location_permission)
        locationButton.setOnClickListener { togglePermissionSelection(it, "location") }

        // Calendar
        val calendarButton = findViewById<ExtendedFloatingActionButton>(R.id.calendar_permission)
        calendarButton.setOnClickListener { togglePermissionSelection(it, "calendar") }

        // Camera
        val cameraButton = findViewById<ExtendedFloatingActionButton>(R.id.camera_permission)
        cameraButton.setOnClickListener { togglePermissionSelection(it, "camera") }

        // SMS
        val smsButton = findViewById<ExtendedFloatingActionButton>(R.id.sms_permission)
        smsButton.setOnClickListener { togglePermissionSelection(it, "sms") }

        // ----- End permissions management -----

        forwardButton = findViewById(R.id.forward_button)

        // Check if we are editing a rule
        val editRule = intent.extras?.get("rule")
        if (editRule != null) {
            val retrievedRule = Json.decodeFromString<Rule>(editRule.toString())

            for (p in retrievedRule.permissions!!) {
                savedPermissions.add(p)

                when (p) {
                    "notifications" -> {
                        notificationsButton.isSelected = true
                        notificationsButton.setBackgroundColor(resources.getColor(R.color.secondary))
                    }

                    "location" -> {
                        locationButton.isSelected = true
                        locationButton.setBackgroundColor(resources.getColor(R.color.secondary))
                    }

                    "calendar" -> {
                        calendarButton.isSelected = true
                        calendarButton.setBackgroundColor(resources.getColor(R.color.secondary))
                    }

                    "camera" -> {
                        cameraButton.isSelected = true
                        cameraButton.setBackgroundColor(resources.getColor(R.color.secondary))
                    }

                    "sms" -> {
                        smsButton.isSelected = true
                        smsButton.setBackgroundColor(resources.getColor(R.color.secondary))
                    }
                }
            }

            forwardButton.setBackgroundColor(resources.getColor(R.color.primary))
            forwardButton.isClickable = true
        }

        // Manage forward button
        // Go to rule definition only if at least one permission has been selected
        forwardButton.setOnClickListener {
            if (savedPermissions.isNotEmpty()) {
                val intent = Intent(this, RuleDefinitionActivity::class.java)

                if (editRule != null) {
                    intent.putExtra("rule", editRule.toString())
                }

                intent.putExtra("permissions", ArrayList(savedPermissions))
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        savedPermissions.clear()
    }

    private fun manageBackNavigation() {
        val intent = Intent(this@PermissionsSelectionActivity, MainActivity::class.java)
        startActivity(intent)
    }

    // Manage permission selection/deselection
    private fun togglePermissionSelection(permission: View, text: String) {
        if (!permission.isSelected) {
            permission.isSelected = true
            permission.setBackgroundColor(resources.getColor(R.color.secondary))

            if (savedPermissions.isEmpty()) {
                forwardButton.setBackgroundColor(resources.getColor(R.color.primary))
                forwardButton.isClickable = true
            }

            savedPermissions.add(text)
        }
        else {
            permission.isSelected = false
            permission.setBackgroundColor(resources.getColor(R.color.grey))
            savedPermissions.remove(text)

            if (savedPermissions.isEmpty()) {
                forwardButton.setBackgroundColor(resources.getColor(R.color.dark_grey))
                forwardButton.isClickable = false
            }
        }
    }
}