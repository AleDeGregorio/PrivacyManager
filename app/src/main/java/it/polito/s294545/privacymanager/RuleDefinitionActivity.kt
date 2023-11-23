package it.polito.s294545.privacymanager

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator

class RuleDefinitionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rule_definition)

        val viewPager = findViewById<ViewPager2>(R.id.view_pager)
        val dotsIndicator = findViewById<DotsIndicator>(R.id.dots_indicator)

        val fragmentList = listOf(PositionsSelectionFragment(), AppsSelectionFragment())
        val adapter = FormPagerAdapter(this, fragmentList)

        viewPager.adapter = adapter
        dotsIndicator.attachTo(viewPager)
    }
}

class FormPagerAdapter(activity: FragmentActivity, private val pages: List<Fragment>) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = pages.size

    override fun createFragment(position: Int): Fragment = pages[position]
}