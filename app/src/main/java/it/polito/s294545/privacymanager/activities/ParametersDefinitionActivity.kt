package it.polito.s294545.privacymanager.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import it.polito.s294545.privacymanager.R
import it.polito.s294545.privacymanager.customDataClasses.Rule
import it.polito.s294545.privacymanager.ruleDefinitionFragments.listAppsInfo
import it.polito.s294545.privacymanager.utilities.PreferencesManager
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class ParametersDefinitionActivity : AppCompatActivity() {

    private var permissionsIntent: Any? = null
    private var appsIntent: Any? = null
    private var pkgsIntent: Any? = null
    private var conditionsIntent: Any? = null
    private var actionIntent: String? = null
    private var nameIntent: String? = null

    private lateinit var savedPermissions: ArrayList<String>
    private lateinit var savedApps: ArrayList<String>
    private lateinit var savedPkgs: ArrayList<String>
    private lateinit var savedConditions: Rule
    private lateinit var savedAction: String

    private var name: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parameters_definition)

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

        val permissionsButton = findViewById<ExtendedFloatingActionButton>(R.id.permissions_button)
        val appsButton = findViewById<ExtendedFloatingActionButton>(R.id.apps_button)
        val conditionsButton = findViewById<ExtendedFloatingActionButton>(R.id.conditions_button)
        val actionButton = findViewById<ExtendedFloatingActionButton>(R.id.action_button)

        val saveButton = findViewById<Button>(R.id.save_button)

        // ----- Manage all intents -----
        // Name
        nameIntent = intent.extras?.getString("name")

        if (nameIntent != null) {
            name = nameIntent as String
        }

        // Permissions
        permissionsIntent = intent.extras?.get("permissions")

        if (permissionsIntent != null) {
            savedPermissions = permissionsIntent as ArrayList<String>

            permissionsButton.setBackgroundColor(resources.getColor(R.color.primary))

            appsButton.setBackgroundColor(resources.getColor(R.color.cancel))
            appsButton.isClickable = true
            appsButton.isFocusable = true
            appsButton.setTextColor(resources.getColor(R.color.white))
            appsButton.setIconTintResource(R.color.white)
        }

        // Apps
        appsIntent = intent.extras?.get("apps")
        pkgsIntent = intent.extras?.get("pkgs")

        if (appsIntent != null && pkgsIntent != null) {
            savedApps = appsIntent as ArrayList<String>
            savedPkgs = pkgsIntent as ArrayList<String>

            appsButton.setBackgroundColor(resources.getColor(R.color.primary))

            conditionsButton.setBackgroundColor(resources.getColor(R.color.cancel))
            conditionsButton.isClickable = true
            conditionsButton.isFocusable = true
            conditionsButton.setTextColor(resources.getColor(R.color.white))
            conditionsButton.setIconTintResource(R.color.white)

            actionButton.setBackgroundColor(resources.getColor(R.color.cancel))
            actionButton.isClickable = true
            actionButton.isFocusable = true
            actionButton.setTextColor(resources.getColor(R.color.white))
            actionButton.setIconTintResource(R.color.white)
        }

        // Conditions
        conditionsIntent = intent.extras?.get("rule")

        if (conditionsIntent != null) {
            savedConditions = Json.decodeFromString(conditionsIntent.toString())

            conditionsButton.setBackgroundColor(resources.getColor(R.color.primary))
        }

        // Action
        actionIntent = intent.extras?.getString("action")

        if (actionIntent != null) {
            savedAction = actionIntent as String
            actionButton.setBackgroundColor(resources.getColor(R.color.primary))

            if (appsIntent != null) {
                saveButton.setBackgroundColor(resources.getColor(R.color.primary))
                saveButton.isClickable = true
                saveButton.isFocusable = true
            }
        }

        // ----- Manage parameters buttons -----
        // Permissions
        permissionsButton.setOnClickListener {
            val intent = Intent(this, PermissionsSelectionActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)

            if (nameIntent != null) {
                intent.putExtra("name", name)
            }
            if (permissionsIntent != null) {
                intent.putExtra("permissions", ArrayList(savedPermissions))
            }
            if (appsIntent != null) {
                intent.putExtra("apps", savedApps)
                intent.putExtra("pkgs", savedPkgs)
            }
            if (conditionsIntent != null) {
                intent.putExtra("rule", conditionsIntent.toString())
            }
            if (actionIntent != null) {
                intent.putExtra("action", savedAction)
            }

            startActivity(intent)
            finish()
        }

        // Apps
        if (permissionsIntent != null) {
            appsButton.setOnClickListener {
                val intent = Intent(this, AppsSelectionActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                intent.putExtra("permissions", ArrayList(savedPermissions))

                if (nameIntent != null) {
                    intent.putExtra("name", name)
                }
                if (appsIntent != null) {
                    intent.putExtra("apps", savedApps)
                    intent.putExtra("pkgs", savedPkgs)
                }
                if (conditionsIntent != null) {
                    intent.putExtra("rule", conditionsIntent.toString())
                }
                if (actionIntent != null) {
                    intent.putExtra("action", savedAction)
                }

                startActivity(intent)
                finish()
            }
        }

        // Conditions
        if (appsIntent != null) {
            conditionsButton.setOnClickListener {
                val intent = Intent(this, RuleDefinitionActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)

                if (nameIntent != null) {
                    intent.putExtra("name", name)
                }
                intent.putExtra("permissions", ArrayList(savedPermissions))
                intent.putExtra("apps", savedApps)
                intent.putExtra("pkgs", savedPkgs)

                if (conditionsIntent != null) {
                    intent.putExtra("rule", conditionsIntent.toString())
                }
                if (actionIntent != null) {
                    intent.putExtra("action", savedAction)
                }

                startActivity(intent)
                finish()
            }
        }

        // Action
        if (appsIntent != null) {
            actionButton.setOnClickListener {
                val intent = Intent(this, ActionSelectionActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)

                if (nameIntent != null) {
                    intent.putExtra("name", name)
                }
                intent.putExtra("permissions", ArrayList(savedPermissions))
                intent.putExtra("apps", savedApps)
                intent.putExtra("pkgs", savedPkgs)

                if (conditionsIntent != null) {
                    intent.putExtra("rule", conditionsIntent.toString())
                }
                if (actionIntent != null) {
                    intent.putExtra("action", savedAction)
                }

                startActivity(intent)
                finish()
            }
        }

        // ----- Save the rule -----
        if (actionIntent != null) {

            // It is a new rule that has to be saved
            if (nameIntent == null) {
                saveButton.setOnClickListener { v -> showPopupSaveRule(v) }
            }
            // It is an edited rule
            else {
                saveButton.setOnClickListener { saveRule() }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun showPopupSaveRule(view: View) {
        val (popupView, popupWindow) = managePopup(view, R.layout.popup_save_rule)

        // Initialize the elements of our window, install the handler
        val ruleName = popupView.findViewById<TextInputEditText>(R.id.edit_rule_name)
        val buttonConfirm = popupView.findViewById<Button>(R.id.confirm_save_button)
        val buttonCancel = popupView.findViewById<Button>(R.id.cancel_save_button)
        val errorName = popupView.findViewById<TextView>(R.id.error_name)

        // Rule name
        ruleName.doOnTextChanged { text, start, before, count ->
            errorName.visibility = GONE

            if (!text.isNullOrEmpty()) {
                buttonConfirm.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.primary))
                buttonConfirm.isClickable = true
            }
            else {
                buttonConfirm.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.dark_grey))
                buttonConfirm.isClickable = false
            }
        }

        // Save rule
        buttonConfirm.setOnClickListener {
            if (!ruleName.text.isNullOrEmpty()) {
                name = ruleName.text.toString().trim()

                if (PreferencesManager.ruleNameAlreadyExists(this, name!!)) {
                    errorName.visibility = VISIBLE
                }
                else {
                    saveRule()
                }
            }
        }

        // Dismiss window
        buttonCancel.setOnClickListener {
            popupWindow.dismiss()
        }


        // Handler for clicking on the inactive zone of the window
        popupView.setOnTouchListener { v, event -> // Close the window when clicked
            popupWindow.dismiss()
            true
        }
    }

    private fun saveRule() {
        // Save the inserted rule in a Rule object
        val rule = Rule()

        rule.name = name
        rule.permissions = savedPermissions
        rule.apps = savedApps
        rule.packageNames = savedPkgs

        if (conditionsIntent != null) {
            rule.timeSlot = savedConditions.timeSlot
            rule.positions = savedConditions.positions?.filter { it.latitude != null }
            rule.networks = savedConditions.networks
            rule.bt = savedConditions.bt
            rule.battery = savedConditions.battery
        }

        rule.action = savedAction

        // Convert rule object to JSON string
        val ruleJSON = Json.encodeToString(Rule.serializer(), rule)

        // Save privacy rule in shared preferences
        PreferencesManager.savePrivacyRule(this, rule.name!!, ruleJSON)

        clearAll()

        // Navigate back to homepage
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()

        clearAll()
    }

    private fun clearAll() {
        permissionsIntent = null
        appsIntent = null
        pkgsIntent = null
        conditionsIntent = null
        actionIntent = null
        nameIntent = null
        name = null
    }

    private fun manageBackNavigation() {
        clearAll()

        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }
}