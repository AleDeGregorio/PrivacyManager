package it.polito.s294545.privacymanager.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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

    private lateinit var notificationsButton: ExtendedFloatingActionButton
    private lateinit var locationButton: ExtendedFloatingActionButton
    private lateinit var calendarButton: ExtendedFloatingActionButton
    private lateinit var cameraButton: ExtendedFloatingActionButton

    private var permissionsIntent: Any? = null
    private var appsIntent: Any? = null
    private var pkgsIntent: Any? = null
    private var conditionsIntent: String? = null
    private var actionIntent: String? = null

    private val savedPermissions = mutableListOf<String>()
    private lateinit var forwardButton : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permissions_selection)

        // Get toolbar
        val toolbarLayout = findViewById<ConstraintLayout>(R.id.toolbarLayout)
        val toolbar = toolbarLayout.findViewById<MaterialToolbar>(R.id.toolbar)

        // Set the navigation icon
        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.icon_left_arrow)
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
        notificationsButton = findViewById(R.id.notification_permission)
        notificationsButton.setOnClickListener { togglePermissionSelection(notificationsButton, "notifications") }

        // Location
        locationButton = findViewById(R.id.location_permission)
        locationButton.setOnClickListener { togglePermissionSelection(locationButton, "location") }

        // Calendar
        calendarButton = findViewById(R.id.calendar_permission)
        calendarButton.setOnClickListener { togglePermissionSelection(calendarButton, "calendar") }

        // Camera
        cameraButton = findViewById(R.id.camera_permission)
        cameraButton.setOnClickListener { togglePermissionSelection(cameraButton, "camera") }

        /*
        // SMS
        val smsButton = findViewById<ExtendedFloatingActionButton>(R.id.sms_permission)
        smsButton.setOnClickListener { togglePermissionSelection(it, "sms") }

         */

        // ----- End permissions management -----

        forwardButton = findViewById(R.id.forward_button)

        // Check if we are editing a rule
        /*
        val editRule = intent.extras?.get("rule")
        if (editRule != null) {
            val retrievedRule = Json.decodeFromString<Rule>(editRule.toString())

            setSavedPermissions(retrievedRule.permissions!!)
        }

         */

        permissionsIntent = intent.extras?.get("permissions")
        appsIntent = intent.extras?.get("apps")
        pkgsIntent = intent.extras?.get("pkgs")
        actionIntent = intent.extras?.getString("action")
        conditionsIntent = intent.extras?.getString("rule")

        if (permissionsIntent != null) {
            setSavedPermissions(permissionsIntent as ArrayList<String>)
        }

        // Manage save button
        // Go to rule definition only if at least one permission has been selected
        forwardButton.setOnClickListener {
            if (savedPermissions.isNotEmpty()) {
                val intent = Intent(this, ParametersDefinitionActivity::class.java)

                /*
                if (editRule != null) {
                    intent.putExtra("rule", editRule.toString())
                }

                 */

                intent.putExtra("permissions", ArrayList(savedPermissions))

                if (permissionsIntent != null) {
                    if (appsIntent != null && pkgsIntent != null && savedPermissions == permissionsIntent as ArrayList<String>) {
                        intent.putExtra("apps", ArrayList(appsIntent as ArrayList<String>))
                        intent.putExtra("pkgs", ArrayList(pkgsIntent as ArrayList<String>))
                    }
                }

                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun setSavedPermissions(permissions: List<String>) {
        for (p in permissions) {
            savedPermissions.add(p)

            when (p) {
                "notifications" -> {
                    notificationsButton.isSelected = true
                    notificationsButton.setBackgroundColor(resources.getColor(R.color.primary))
                    notificationsButton.setTextColor(resources.getColor(R.color.white))
                    notificationsButton.setIconTintResource(R.color.white)
                }

                "location" -> {
                    locationButton.isSelected = true
                    locationButton.setBackgroundColor(resources.getColor(R.color.primary))
                    locationButton.setTextColor(resources.getColor(R.color.white))
                    locationButton.setIconTintResource(R.color.white)
                }

                "calendar" -> {
                    calendarButton.isSelected = true
                    calendarButton.setBackgroundColor(resources.getColor(R.color.primary))
                    calendarButton.setTextColor(resources.getColor(R.color.white))
                    calendarButton.setIconTintResource(R.color.white)
                }

                "camera" -> {
                    cameraButton.isSelected = true
                    cameraButton.setBackgroundColor(resources.getColor(R.color.primary))
                    cameraButton.setTextColor(resources.getColor(R.color.white))
                    cameraButton.setIconTintResource(R.color.white)
                }

                /*
                "sms" -> {
                    smsButton.isSelected = true
                    smsButton.setBackgroundColor(resources.getColor(R.color.primary))
                }

                 */
            }
        }

        forwardButton.setBackgroundColor(resources.getColor(R.color.primary))
        forwardButton.isClickable = true
    }

    override fun onDestroy() {
        super.onDestroy()

        savedPermissions.clear()
        appsIntent = null
        pkgsIntent = null
        conditionsIntent = null
        actionIntent = null
    }

    private fun manageBackNavigation() {
        val intent = Intent(this@PermissionsSelectionActivity, ParametersDefinitionActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)

        if (permissionsIntent != null) {
            intent.putExtra("permissions", ArrayList(permissionsIntent as ArrayList<String>))
        }
        if (appsIntent != null && pkgsIntent != null) {
            intent.putExtra("apps", ArrayList(appsIntent as ArrayList<String>))
            intent.putExtra("pkgs", ArrayList(pkgsIntent as ArrayList<String>))
        }
        if (conditionsIntent != null) {
            intent.putExtra("rule", conditionsIntent)
        }
        if (actionIntent != null) {
            intent.putExtra("action", actionIntent)
        }

        savedPermissions.clear()
        appsIntent = null
        pkgsIntent = null
        conditionsIntent = null
        actionIntent = null

        startActivity(intent)
        finish()
    }

    // Manage permission selection/deselection
    private fun togglePermissionSelection(permission: ExtendedFloatingActionButton, text: String) {
        if (!permission.isSelected) {
            permission.isSelected = true
            permission.setBackgroundColor(resources.getColor(R.color.primary))
            permission.setTextColor(resources.getColor(R.color.white))
            permission.setIconTintResource(R.color.white)

            if (savedPermissions.isEmpty()) {
                forwardButton.setBackgroundColor(resources.getColor(R.color.primary))
                forwardButton.isClickable = true
            }

            savedPermissions.add(text)
        }
        else {
            permission.isSelected = false
            permission.setBackgroundColor(resources.getColor(R.color.grey))
            permission.setTextColor(resources.getColor(R.color.black))
            permission.setIconTintResource(R.color.black)
            savedPermissions.remove(text)

            if (savedPermissions.isEmpty()) {
                forwardButton.setBackgroundColor(resources.getColor(R.color.dark_grey))
                forwardButton.isClickable = false
            }
        }
    }
}