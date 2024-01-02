package it.polito.s294545.privacymanager.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator
import it.polito.s294545.privacymanager.R
import it.polito.s294545.privacymanager.customDataClasses.CustomAddress
import it.polito.s294545.privacymanager.customDataClasses.Rule
import it.polito.s294545.privacymanager.customDataClasses.TimeSlot
import it.polito.s294545.privacymanager.ruleDefinitionFragments.ActionNoNotificationSelectionFragment
import it.polito.s294545.privacymanager.ruleDefinitionFragments.ActionWithNotificationSelectionFragment
import it.polito.s294545.privacymanager.ruleDefinitionFragments.AppsSelectionFragment
import it.polito.s294545.privacymanager.ruleDefinitionFragments.BatterySelectionFragment
import it.polito.s294545.privacymanager.ruleDefinitionFragments.BluetoothSelectionFragment
import it.polito.s294545.privacymanager.ruleDefinitionFragments.NetworkSelectionFragment
import it.polito.s294545.privacymanager.ruleDefinitionFragments.PositionsSelectionFragment
import it.polito.s294545.privacymanager.ruleDefinitionFragments.TimeSlotSelectionFragment
import it.polito.s294545.privacymanager.ruleDefinitionFragments.listAppsInfo
import it.polito.s294545.privacymanager.ruleDefinitionFragments.listBluetooth
import it.polito.s294545.privacymanager.ruleDefinitionFragments.savedActionNoNotification
import it.polito.s294545.privacymanager.ruleDefinitionFragments.savedActionWithNotification
import it.polito.s294545.privacymanager.ruleDefinitionFragments.savedApps
import it.polito.s294545.privacymanager.ruleDefinitionFragments.savedBT
import it.polito.s294545.privacymanager.ruleDefinitionFragments.savedBattery
import it.polito.s294545.privacymanager.ruleDefinitionFragments.savedMobile
import it.polito.s294545.privacymanager.ruleDefinitionFragments.savedNetworks
import it.polito.s294545.privacymanager.ruleDefinitionFragments.savedPkg
import it.polito.s294545.privacymanager.ruleDefinitionFragments.savedPositions
import it.polito.s294545.privacymanager.ruleDefinitionFragments.savedSlot
import it.polito.s294545.privacymanager.utilities.ParameterListener
import it.polito.s294545.privacymanager.utilities.PreferencesManager
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

var retrievedRule: Rule? = null

class RuleDefinitionActivity : AppCompatActivity(), ParameterListener {

    private lateinit var viewPager : ViewPager2
    private lateinit var fragmentList : MutableList<Fragment>

    private lateinit var error : TextView

    private var nameIntent: String? = null
    private var permissionsIntent: Any? = null
    private var permissions: ArrayList<String>? = null
    private var appsIntent: Any? = null
    private var pkgsIntent: Any? = null
    private var actionIntent: String? = null

    // Rule parameters
    private var timeSlot : TimeSlot? = null
    private var positions : List<CustomAddress>? = null
    private var networks : List<String>? = null
    private var bt : List<String>? = null
    private var battery : Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rule_definition)

        // Get toolbar
        val toolbarLayout = findViewById<ConstraintLayout>(R.id.toolbarLayout)
        val toolbar = toolbarLayout.findViewById<MaterialToolbar>(R.id.toolbar)

        // Set the navigation icon
        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.icon_left_arrow)
        toolbar.setNavigationIconTint(resources.getColor(R.color.white))

        toolbar.setNavigationOnClickListener {
            manageBackNavigation()
        }

        // Manage user's back pressure
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                manageBackNavigation()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        // Initialize error text
        error = findViewById(R.id.error_app)

        // Initialize eventual edit rule
        val editRule = intent.extras?.get("rule")
        // Check if we are editing a rule
        if (editRule != null) {
            retrievedRule = Json.decodeFromString(editRule.toString())
        }

        nameIntent = intent.extras?.getString("name")

        permissionsIntent = intent.extras?.get("permissions")
        permissions = permissionsIntent as ArrayList<String>

        appsIntent = intent.extras?.get("apps")
        pkgsIntent = intent.extras?.get("pkgs")

        actionIntent = intent.extras?.getString("action")

        // Define rule parameters fragments
        viewPager = findViewById(R.id.view_pager)
        viewPager.setPageTransformer(ZoomOutPageTransformer())
        val dotsIndicator = findViewById<DotsIndicator>(R.id.dots_indicator)

        fragmentList = mutableListOf(
            TimeSlotSelectionFragment(), PositionsSelectionFragment(),
            NetworkSelectionFragment(), BluetoothSelectionFragment(), BatterySelectionFragment()
        )

        val adapter = FormPagerAdapter(this, fragmentList)

        viewPager.adapter = adapter
        dotsIndicator.attachTo(viewPager)

        // Manage forward button
        val forwardButton = findViewById<Button>(R.id.forward_button)
        val backButton = findViewById<Button>(R.id.back_button)
        // First fragment -> go back
        // Manage back button
        backButton.text = resources.getString(R.string.cancel_button)
        backButton.setOnClickListener { manageBackNavigation() }

        // Change button based on current fragment
        // Register a callback to listen for page changes
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (error.isVisible) {
                    error.visibility = GONE
                }

                // Update button based on the current Fragment
                // First fragment -> go back
                if (position == 0) {
                    forwardButton.text = resources.getString(R.string.forward_button)
                    forwardButton.setOnClickListener { navigateToNextFragment() }
                    backButton.text = resources.getString(R.string.cancel_button)
                    backButton.setOnClickListener { manageBackNavigation() }
                }
                // Last fragment -> save rule
                else if (position == fragmentList.lastIndex) {
                    forwardButton.text = resources.getString(R.string.save)
                    backButton.text = resources.getString(R.string.back_button)
                    backButton.setOnClickListener { navigateToPreviousFragment() }

                    forwardButton.setOnClickListener { v -> showPopupSaveRule(v) }
                }
                // Other fragments -> go to next fragment
                else {
                    forwardButton.text = resources.getString(R.string.forward_button)
                    forwardButton.setOnClickListener { navigateToNextFragment() }
                    backButton.text = resources.getString(R.string.back_button)
                    backButton.setOnClickListener { navigateToPreviousFragment() }
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()

        clearAll()
    }

    private fun clearAll() {
        nameIntent = null
        permissionsIntent = null
        appsIntent = null
        pkgsIntent = null
        permissions!!.clear()
        actionIntent = null

        retrievedRule = null

        //apps = null
        //packageNames = null
        timeSlot = null
        positions = null
        networks = null
        bt = null
        battery = null
        //action = null
        //name = null
        listBluetooth.clear()

        savedApps.clear()
        savedPkg.clear()
        savedSlot = TimeSlot()
        savedPositions.clear()
        savedNetworks.clear()
        savedMobile.clear()
        savedBT.clear()
        savedBattery = null
        savedActionNoNotification = "signal_app"
        savedActionWithNotification = "obscure_notification"
    }

    // It is no longer a popup, it just navigates back to parameters definition activity
    @SuppressLint("ClickableViewAccessibility")
    private fun showPopupSaveRule(view: View) {
        // Check if inserted time slot is correct
        // Signal error on time slot if one of this condition is verified (considering a defined time slot):
        // - no day defined
        // - "from" is null
        // - "to" is null
        // - "from" (hour) is later than "to" (hour)
        // - "from" and "to" have the same hour, but "from" (minutes) is later than "to" (minutes)
        var timeSlotError = false

        if (timeSlot != null) {
            if (timeSlot!!.days.isEmpty() && timeSlot!!.time.first == "" && timeSlot!!.time.second == "") {
                timeSlotError = false
            }
            else if (timeSlot!!.days.isEmpty() || timeSlot!!.time.first == "" || timeSlot!!.time.second == "") {
                timeSlotError = true
            }
            else {
                val from = timeSlot!!.time.first
                val to = timeSlot!!.time.second

                val fromHour = from.split(":")[0].toInt()
                val toHour = to.split(":")[0].toInt()

                val fromMinutes = from.split(":")[1].toInt()
                val toMinutes = to.split(":")[1].toInt()

                if (fromHour > toHour || (fromHour == toHour && fromMinutes >= toMinutes)) {
                    timeSlotError = true
                }
            }
        }

        if (timeSlotError) {
            viewPager.currentItem = 0

            error.text = resources.getString(R.string.error_time)
            error.visibility = VISIBLE

            return
        }

        // NAVIGATE TO PARAMETERS DEFINITION
        val intent = Intent(this, ParametersDefinitionActivity::class.java)

        if (nameIntent != null) {
            intent.putExtra("name", nameIntent)
        }

        intent.putExtra("permissions", permissions)
        intent.putExtra("action", actionIntent)
        intent.putExtra("apps", ArrayList(appsIntent as ArrayList<String>))
        intent.putExtra("pkgs", ArrayList(pkgsIntent as ArrayList<String>))

        // Save the inserted conditions in a Rule object
        val rule = Rule()
        rule.timeSlot = timeSlot
        rule.positions = positions?.filter { it.latitude != null }
        rule.networks = networks
        rule.bt = bt
        rule.battery = battery

        // Convert rule object to JSON string
        val ruleJSON = Json.encodeToString(Rule.serializer(), rule)

        intent.putExtra("rule", ruleJSON)

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }

    private fun navigateToNextFragment() {
        if (viewPager.currentItem < fragmentList.size - 1) {
            viewPager.currentItem = viewPager.currentItem + 1
        }
    }

    private fun navigateToPreviousFragment() {
        if (viewPager.currentItem > 0) {
            viewPager.currentItem = viewPager.currentItem - 1
        }
    }

    private fun manageBackNavigation() {

        val intent = Intent(this, ParametersDefinitionActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)

        if (nameIntent != null) {
            intent.putExtra("name", nameIntent)
        }

        intent.putExtra("permissions", ArrayList(permissions!!))

        if (appsIntent != null) {
            intent.putExtra("apps", ArrayList(appsIntent as ArrayList<String>))
            intent.putExtra("pkgs", ArrayList(pkgsIntent as ArrayList<String>))
        }

        if (actionIntent != null) {
            intent.putExtra("action", actionIntent)
        }

        if (retrievedRule != null) {
            intent.putExtra("rule", Json.encodeToString(retrievedRule))
        }

        clearAll()

        startActivity(intent)
        finish()
    }

    // When entering data in fragments, save them in activity
    override fun onParameterEntered(parameter: String, data: Any?) {
        when (parameter) {
            "time_slot" -> {
                timeSlot = data as TimeSlot?
            }
            "positions" -> {
                positions = data as List<CustomAddress>
            }
            "networks" -> {
                networks = data as List<String>
            }
            "bt" -> {
                bt = data as List<String>
            }
            "battery" -> {
                battery = data as Int?
            }
        }
    }
}

class FormPagerAdapter(activity: FragmentActivity, private val pages: List<Fragment>) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = pages.size

    override fun createFragment(position: Int): Fragment = pages[position]
}

// ViewPager animation

private const val MIN_SCALE = 0.85f
private const val MIN_ALPHA = 0.5f

class ZoomOutPageTransformer : ViewPager2.PageTransformer {

    override fun transformPage(view: View, position: Float) {
        view.apply {
            val pageWidth = width
            val pageHeight = height
            when {
                position < -1 -> { // [-Infinity,-1)
                    // This page is way off-screen to the left.
                    alpha = 0f
                }
                position <= 1 -> { // [-1,1]
                    // Modify the default slide transition to shrink the page as well.
                    val scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position))
                    val vertMargin = pageHeight * (1 - scaleFactor) / 2
                    val horzMargin = pageWidth * (1 - scaleFactor) / 2
                    translationX = if (position < 0) {
                        horzMargin - vertMargin / 2
                    } else {
                        horzMargin + vertMargin / 2
                    }

                    // Scale the page down (between MIN_SCALE and 1).
                    scaleX = scaleFactor
                    scaleY = scaleFactor

                    // Fade the page relative to its size.
                    alpha = (MIN_ALPHA +
                            (((scaleFactor - MIN_SCALE) / (1 - MIN_SCALE)) * (1 - MIN_ALPHA)))
                }
                else -> { // (1,+Infinity]
                    // This page is way off-screen to the right.
                    alpha = 0f
                }
            }
        }
    }
}