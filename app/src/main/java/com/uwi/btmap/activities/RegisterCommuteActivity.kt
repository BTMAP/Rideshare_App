package com.uwi.btmap.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.afollestad.viewpagerdots.DotsIndicator
import com.uwi.btmap.*
import com.uwi.btmap.bll.CommuteViewModel
import com.uwi.btmap.fragments.*

private const val NUM_PAGES = 5

class RegisterCommuteActivity : AppCompatActivity() {

    private lateinit var pager: ViewPager
    private lateinit var dots: DotsIndicator

    private var commuteType = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.setTitle("Register Commute")
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        setContentView(R.layout.activity_register_commute)

        pager = findViewById(R.id.selector_pager_view)

        pager.setPageTransformer(true, ZoomOutPageTransformer())

        val pagerAdapter = ScreenSlidePagerAdapter(supportFragmentManager)
        pager.adapter = pagerAdapter

        //dots.attachViewPager(pager)


        val commuteViewModel = ViewModelProvider(this).get(CommuteViewModel::class.java)
        commuteViewModel.token = getString(R.string.mapbox_access_token)
    }

    override fun onBackPressed() {
        if (pager.currentItem == 0) {
            super.onBackPressed()
        } else {
            pager.currentItem = pager.currentItem - 1
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun setNextPage(){
        pager.currentItem = pager.currentItem + 1
    }

    fun setPrevPage(){
        pager.currentItem = pager.currentItem - 1
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
