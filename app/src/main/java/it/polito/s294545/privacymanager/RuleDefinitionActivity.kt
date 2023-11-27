package it.polito.s294545.privacymanager

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.MaterialToolbar
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator

class RuleDefinitionActivity : AppCompatActivity(), ParameterListener {

    private lateinit var viewPager : ViewPager2
    private lateinit var fragmentList : MutableList<Fragment>
    private lateinit var permissions : ArrayList<*>
    private lateinit var apps : List<*>

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

        // Define rule parameters fragments
        viewPager = findViewById(R.id.view_pager)
        val dotsIndicator = findViewById<DotsIndicator>(R.id.dots_indicator)

        fragmentList = mutableListOf(
            AppsSelectionFragment(), TimeSlotSelectionFragment(), PositionsSelectionFragment(),
            NetworkSelectionFragment(), BluetoothSelectionFragment(), BatterySelectionFragment())

        permissions = intent.extras?.get("permissions") as ArrayList<*>

        if (permissions.contains("notifications")) {
            fragmentList.add(ActionWithNotificationSelectionFragment())
        }
        else {
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
        val (popupView, popupWindow) = managePopup(view, R.layout.popup_save_rule)

        // Initialize the elements of our window, install the handler
        val buttonConfirm = popupView.findViewById<Button>(R.id.confirm_save_button)
        buttonConfirm.setOnClickListener {
            // Save rule
        }

        val buttonCancel = popupView.findViewById<Button>(R.id.cancel_save_button)
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

    override fun onParameterEntered(parameter: String, data: Any) {
        when (parameter) {
            "apps" -> {
                apps = data as List<*>
            }
        }
    }
}

class FormPagerAdapter(activity: FragmentActivity, private val pages: List<Fragment>) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = pages.size

    override fun createFragment(position: Int): Fragment = pages[position]
}