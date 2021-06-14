package com.uwi.btmap.views.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.uwi.btmap.*
import com.uwi.btmap.viewmodels.RegisterCommuteViewModel

import com.uwi.btmap.views.fragments.register.*

private const val NUM_PAGES = 5

class RegisterCommuteActivity : AppCompatActivity() {

    private lateinit var pager: ViewPager

    private var commuteType = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_commute)

        pager = findViewById(R.id.selector_pager_view)

        val pagerAdapter = ScreenSlidePagerAdapter(supportFragmentManager)
        pager.adapter = pagerAdapter

        val commuteViewModel = ViewModelProvider(this).get(RegisterCommuteViewModel::class.java)
        commuteViewModel.token = getString(R.string.mapbox_access_token)
    }

    override fun onBackPressed() {
        if (pager.currentItem == 0) {
            super.onBackPressed()
        } else {
            pager.currentItem = pager.currentItem - 1
        }
    }

    private inner class ScreenSlidePagerAdapter(fm: FragmentManager) :
        FragmentStatePagerAdapter(fm) {
        override fun getCount(): Int = NUM_PAGES

        override fun getItem(position: Int): Fragment {
            var fragment: Fragment = TypeSelectorFragment()
            when (position) {
                0 -> fragment = TypeSelectorFragment()
                1 -> fragment = LocationSelectionFragment()
                2 -> fragment = DateSelectorFragment()
                3 -> fragment = TimeSelectorFragment()
                4 -> fragment = SubmitCommuteFragment()
            }
            return fragment
        }

    }
}
