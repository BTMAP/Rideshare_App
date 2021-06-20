package com.uwi.btmap.views.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Observer
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

        val viewModel = ViewModelProvider(this).get(RegisterCommuteViewModel::class.java)
        viewModel.token = getString(R.string.mapbox_access_token)

        //add submit commute success livedata observer
        //switch activity
        viewModel.commuteSaveSuccess().observe(this, Observer{
            if(it){
                //move this from the fragment ot the view model
                val intent = Intent(this,MainActivity::class.java)
                startActivity(intent)
            }
        })

        //add pair commute success livedata observer
        //switch activity
        viewModel.findPairSuccess().observe(this, Observer{
            if(it){
                //move this from the fragment ot the view model
                val intent = Intent(this,SelectPairActivity::class.java)
                    .putExtra("CommuteOptions",viewModel.commuteOptions)
                startActivity(intent)
            }
        })
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
