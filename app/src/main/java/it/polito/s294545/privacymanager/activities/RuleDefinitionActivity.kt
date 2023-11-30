package it.polito.s294545.privacymanager.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
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
import it.polito.s294545.privacymanager.customDataClasses.CustomAddress
import it.polito.s294545.privacymanager.utilities.ParameterListener
import it.polito.s294545.privacymanager.utilities.PreferencesManager
import it.polito.s294545.privacymanager.R
import it.polito.s294545.privacymanager.customDataClasses.Rule
import it.polito.s294545.privacymanager.ruleDefinitionFragments.ActionNoNotificationSelectionFragment
import it.polito.s294545.privacymanager.ruleDefinitionFragments.ActionWithNotificationSelectionFragment
import it.polito.s294545.privacymanager.ruleDefinitionFragments.AppsSelectionFragment
import it.polito.s294545.privacymanager.ruleDefinitionFragments.BatterySelectionFragment
import it.polito.s294545.privacymanager.ruleDefinitionFragments.BluetoothSelectionFragment
import it.polito.s294545.privacymanager.ruleDefinitionFragments.NetworkSelectionFragment
import it.polito.s294545.privacymanager.ruleDefinitionFragments.PositionsSelectionFragment
import it.polito.s294545.privacymanager.ruleDefinitionFragments.TimeSlotSelectionFragment
import it.polito.s294545.privacymanager.customDataClasses.TimeSlot
import kotlinx.serialization.json.Json

class RuleDefinitionActivity : AppCompatActivity(), ParameterListener {

    private lateinit var viewPager : ViewPager2
    private lateinit var fragmentList : MutableList<Fragment>

    private lateinit var error : TextView

    // Rule parameters
    private lateinit var permissions : ArrayList<String>
    private var apps : List<String>? = null
    private var timeSlot : TimeSlot? = null
    private var positions : List<CustomAddress>? = null
    private var networks : List<String>? = null
    private var bt : List<String>? = null
    private var battery : Int? = null
    private var action : String? = null
    private var name : String? = null

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

        // Define rule parameters fragments
        viewPager = findViewById(R.id.view_pager)
        val dotsIndicator = findViewById<DotsIndicator>(R.id.dots_indicator)

        fragmentList = mutableListOf(
            AppsSelectionFragment(), TimeSlotSelectionFragment(), PositionsSelectionFragment(),
            NetworkSelectionFragment(), BluetoothSelectionFragment(), BatterySelectionFragment()
        )

        permissions = intent.extras?.get("permissions") as ArrayList<String>

        if (permissions.contains("notifications")) {
            action = "signal_app"
            fragmentList.add(ActionWithNotificationSelectionFragment())
        }
        else {
            action = "obscure_notification"
            fragmentList.add(ActionNoNotificationSelectionFragment())
        }

        val adapter = FormPagerAdapter(this, fragmentList)

        viewPager.adapter = adapter
        dotsIndicator.attachTo(viewPager)

        // Manage forward button
        val forwardButton = findViewById<Button>(R.id.forward_button)
        // Change button based on current fragment
        // Register a callback to listen for page changes
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                // Update button based on the current Fragment
                // Last fragment -> save rule
                if (position == fragmentList.lastIndex) {
                    forwardButton.text = resources.getString(R.string.save)
                    forwardButton.setOnClickListener { v -> showPopupSaveRule(v) }
                }
                // Other fragments -> go to next fragment
                else {
                    forwardButton.text = resources.getString(R.string.forward_button)
                    forwardButton.setOnClickListener { navigateToNextFragment() }
                }
            }
        })

        // Manage back button
        val backButton = findViewById<Button>(R.id.back_button)
        backButton.setOnClickListener { navigateToPreviousFragment() }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun showPopupSaveRule(view: View) {
        if (apps.isNullOrEmpty()) {
            viewPager.currentItem = fragmentList.indexOf(AppsSelectionFragment())

            error.text = resources.getString(R.string.error_app)
            error.visibility = VISIBLE

            return
        }

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
                name = ruleName.text.toString()

                if (PreferencesManager.ruleNameAlreadyExists(this, name!!)) {
                    errorName.visibility = VISIBLE
                }
                else {
                    // Save the inserted rule in a Rule object
                    val rule = Rule()
                    rule.name = name
                    rule.permissions = permissions
                    rule.apps = apps
                    rule.timeSlot = timeSlot
                    rule.positions = positions
                    rule.networks = networks
                    rule.bt = bt
                    rule.battery = battery
                    rule.action = action

                    // Convert rule object to JSON string
                    val ruleJSON = Json.encodeToString(Rule.serializer(), rule)

                    // Save privacy rule in shared preferences
                    PreferencesManager.savePrivacyRule(this, rule.name!!, ruleJSON)

                    // Navigate back to homepage
                    val intent = Intent(this@RuleDefinitionActivity, MainActivity::class.java)
                    startActivity(intent)
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
        val intent = Intent(this@RuleDefinitionActivity, PermissionsSelectionActivity::class.java)
        startActivity(intent)
    }

    // When entering data in fragments, save them in activity
    override fun onParameterEntered(parameter: String, data: Any?) {
        when (parameter) {
            "apps" -> {
                apps = data as List<String>

                if (error.isVisible) {
                    error.visibility = GONE
                }
            }
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
            "action" -> {
                action = data as String
            }
        }
    }
}

class FormPagerAdapter(activity: FragmentActivity, private val pages: List<Fragment>) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = pages.size

    override fun createFragment(position: Int): Fragment = pages[position]
}