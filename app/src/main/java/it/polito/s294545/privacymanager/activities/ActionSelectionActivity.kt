package it.polito.s294545.privacymanager.activities

import android.content.Intent
import android.content.res.ColorStateList
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar
import it.polito.s294545.privacymanager.R

class ActionSelectionActivity : AppCompatActivity() {

    private var permissionsIntent: Any? = null
    private var permissions: ArrayList<String>? = null

    private var appsIntent: Any? = null
    private var pkgsIntent: Any? = null

    private var conditionsIntent: String? = null

    private var actionIntent: String? = null

    private var savedAction = "signal_app"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_action_selection)

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

        // Setting title and options
        val title = findViewById<TextView>(R.id.infoTextView)
        val signalAppRadio = findViewById<RadioButton>(R.id.signal_app)
        val closeAppRadio = findViewById<RadioButton>(R.id.close_app)

        permissionsIntent = intent.extras?.get("permissions")
        permissions = permissionsIntent as ArrayList<String>

        appsIntent = intent.extras?.get("apps")
        pkgsIntent = intent.extras?.get("pkgs")
        conditionsIntent = intent.extras?.getString("rule")

        if (permissions!!.contains("notifications")) {
            title.text = resources.getText(R.string.info_action_notification_selection)
            signalAppRadio.text = resources.getText(R.string.obscure_notification_text)
            closeAppRadio.text = resources.getText(R.string.block_notification_text)

            savedAction = "obscure_notification"
        }

        actionIntent = intent.extras?.getString("action")

        if (actionIntent != null) {
            if (actionIntent != "signal_app") {
                savedAction = actionIntent!!

                if (actionIntent == "close_app" || actionIntent == "block_notification") {
                    closeAppRadio.isChecked = true
                }
            }
        }

        // Setting radio buttons color programmatically
        signalAppRadio.buttonTintList = ColorStateList.valueOf(ContextCompat.getColor(this,
            R.color.primary
        ))

        closeAppRadio.buttonTintList = ColorStateList.valueOf(ContextCompat.getColor(this,
            R.color.primary
        ))

        // Check if we are editing a rule
        /*
        if (retrievedRule != null) {
            savedAction = retrievedRule!!.action!!

            if (savedAction == "close_app") {
                signalAppRadio.isChecked = false
                closeAppRadio.isChecked = true
            }
        }

         */

        // Pass action info as parameter
        signalAppRadio.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                savedAction = if (permissions!!.contains("notifications")) {
                    "obscure_notification"
                } else {
                    "signal_app"
                }
            }
        }

        closeAppRadio.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                savedAction = if (permissions!!.contains("notifications")) {
                    "block_notification"
                } else {
                    "close_app"
                }
            }
        }

        // Manage save button
        val saveButton = findViewById<Button>(R.id.save_button)
        saveButton.setOnClickListener {
            val intent = Intent(this, ParametersDefinitionActivity::class.java)

            /*
            if (editRule != null) {
                intent.putExtra("rule", editRule.toString())
            }

             */

            intent.putExtra("permissions", permissions)
            intent.putExtra("action", savedAction)
            intent.putExtra("apps", ArrayList(appsIntent as ArrayList<String>))
            intent.putExtra("pkgs", ArrayList(pkgsIntent as ArrayList<String>))

            if (conditionsIntent != null) {
                intent.putExtra("rule", conditionsIntent)
            }

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        permissionsIntent = null
        appsIntent = null
        pkgsIntent = null
        conditionsIntent = null
        permissions!!.clear()
        actionIntent = null
        savedAction = "signal_app"
    }

    private fun manageBackNavigation() {
        val intent = Intent(this, ParametersDefinitionActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)

        intent.putExtra("permissions", ArrayList(permissions!!))

        if (appsIntent != null) {
            intent.putExtra("apps", ArrayList(appsIntent as ArrayList<String>))
            intent.putExtra("pkgs", ArrayList(pkgsIntent as ArrayList<String>))
        }
        if (conditionsIntent != null) {
            intent.putExtra("rule", conditionsIntent)
        }
        if (actionIntent != null) {
            intent.putExtra("action", actionIntent)
        }

        permissionsIntent = null
        appsIntent = null
        pkgsIntent = null
        conditionsIntent = null
        permissions!!.clear()
        actionIntent = null
        savedAction = "signal_app"

        startActivity(intent)
        finish()
    }
}