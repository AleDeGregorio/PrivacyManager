package it.polito.s294545.privacymanager.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.imageview.ShapeableImageView
import it.polito.s294545.privacymanager.R

class NotificationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

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

        // Get info from intent
        val ruleName = intent.extras?.getString("ruleName")
        val pkg = intent.extras?.getString("pkg")
        val title = intent.extras?.getString("title")
        val content = intent.extras?.getString("content")

        val infoText = findViewById<TextView>(R.id.infoTextView)
        infoText.text = "La seguente app definita in \"$ruleName\" ha inviato questa notifica"

        // Get app info from package name
        val packageManager = packageManager
        val appInfo = packageManager.getApplicationInfo(pkg!!, 0)
        val appName = appInfo.loadLabel(packageManager).toString()
        val appIcon = appInfo.loadIcon(packageManager)

        val appNameTxt = findViewById<TextView>(R.id.app_title)
        appNameTxt.text = appName

        val appIconImg = findViewById<ShapeableImageView>(R.id.app_icon)
        appIconImg.setImageDrawable(appIcon)

        // Set notification info
        val notificationTitle = findViewById<TextView>(R.id.notification_title)
        notificationTitle.text = title

        val notificationContent = findViewById<TextView>(R.id.notification_content)
        notificationContent.text = content
    }

    private fun manageBackNavigation() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }
}