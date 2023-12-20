package it.polito.s294545.privacymanager.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import it.polito.s294545.privacymanager.R

class ParametersDefinitionActivity : AppCompatActivity() {

    private lateinit var savedPermissions: ArrayList<String>

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

        // ----- Manage parameters buttons -----
        val permissionsButton = findViewById<ExtendedFloatingActionButton>(R.id.permissions_button)
        val appsButton = findViewById<ExtendedFloatingActionButton>(R.id.apps_button)
        val conditionsButton = findViewById<ExtendedFloatingActionButton>(R.id.conditions_button)
        val actionButton = findViewById<ExtendedFloatingActionButton>(R.id.action_button)

        // Permissions
        val permissionsIntent = intent.extras?.get("permissions")

        if (permissionsIntent != null) {
            savedPermissions = permissionsIntent as ArrayList<String>

            Log.d("myapp", "$savedPermissions")
        }

        if (permissionsIntent != null) {
            permissionsButton.setBackgroundColor(resources.getColor(R.color.primary))

            appsButton.setBackgroundColor(resources.getColor(R.color.cancel))
            appsButton.isClickable = true
            appsButton.isFocusable = true
        }

        permissionsButton.setOnClickListener {
            val intent = Intent(this, PermissionsSelectionActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)

            if (permissionsIntent != null) {
                intent.putExtra("permissions", ArrayList(savedPermissions))
            }

            startActivity(intent)
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        //savedPermissions.clear()
    }

    private fun manageBackNavigation() {
        //savedPermissions.clear()

        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }
}