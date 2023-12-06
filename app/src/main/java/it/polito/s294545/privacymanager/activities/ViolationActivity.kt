package it.polito.s294545.privacymanager.activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View.GONE
import android.widget.Button
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import it.polito.s294545.privacymanager.R

class ViolationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_violation)

        // Get toolbar
        val toolbarLayout = findViewById<ConstraintLayout>(R.id.toolbarLayout)
        val toolbar = toolbarLayout.findViewById<MaterialToolbar>(R.id.toolbar)

        // Set the navigation icon
        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.icon_cross)
        toolbar.setNavigationIconTint(resources.getColor(R.color.white))

        toolbar.setNavigationOnClickListener { manageBackNavigation() }

        // Manage user's back pressure
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                manageBackNavigation()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        // Get action
        val action = intent.extras?.getString("action")
        val isSignal = action == "signal_app"

        // Set name of rule violated
        val ruleName = intent.extras?.getString("ruleName")
        val infoText = findViewById<TextView>(R.id.infoTextView)

        val info = if (isSignal) {
            "La seguente app ha violato la regola \"$ruleName\""
        } else {
            "La seguente app ha violato la regola \"$ruleName\" ed è stata chiusa"
        }

        infoText.text = info

        // Set app info
        val pkgName = intent.extras?.getString("pkg")

        if (pkgName != null) {
            val packageManager = packageManager
            val appInfo = packageManager.getApplicationInfo(pkgName, 0)
            val appName = appInfo.loadLabel(packageManager).toString()
            val appIcon = appInfo.loadIcon(packageManager)

            val appNameTxt = findViewById<TextView>(R.id.app_title)
            appNameTxt.text = appName

            val appIconImg = findViewById<ShapeableImageView>(R.id.app_icon)
            appIconImg.setImageDrawable(appIcon)
        }

        // Manage revoke permission button
        val revokeButton = findViewById<ExtendedFloatingActionButton>(R.id.revoke_permission)

        if (!isSignal) {
            revokeButton.visibility = GONE
        }
        else {
            revokeButton.setOnClickListener {
                val appSettingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                appSettingsIntent.data = Uri.fromParts("package", pkgName, null)

                // Check if the intent can be resolved to avoid crashing on unsupported devices
                if (appSettingsIntent.resolveActivity(packageManager) != null) {
                    ContextCompat.startActivity(this, appSettingsIntent, null)
                }
            }
        }
    }

    private fun manageBackNavigation() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }
}