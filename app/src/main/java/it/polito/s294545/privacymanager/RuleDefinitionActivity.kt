package it.polito.s294545.privacymanager

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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

class RuleDefinitionActivity : AppCompatActivity() {

    private lateinit var viewPager : ViewPager2
    private lateinit var fragmentList : List<Fragment>

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

        fragmentList = listOf(AppsSelectionFragment(), TimeSlotSelectionFragment(), PositionsSelectionFragment())
        val adapter = FormPagerAdapter(this, fragmentList)

        viewPager.adapter = adapter
        dotsIndicator.attachTo(viewPager)

        // Manage forward button
        val forwardButton = findViewById<Button>(R.id.forward_button)
        forwardButton.setOnClickListener { navigateToNextFragment() }

        // Manage back button
        val backButton = findViewById<Button>(R.id.back_button)
        backButton.setOnClickListener { navigateToPreviousFragment() }
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
}

class FormPagerAdapter(activity: FragmentActivity, private val pages: List<Fragment>) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = pages.size

    override fun createFragment(position: Int): Fragment = pages[position]
}