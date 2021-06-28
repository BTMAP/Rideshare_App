package com.uwi.btmap.views.activities

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.afollestad.viewpagerdots.DotsIndicator
import com.uwi.btmap.MainActivity
import com.uwi.btmap.R
import com.uwi.btmap.activities.ZoomOutPageTransformer
import com.uwi.btmap.viewmodels.RegisterCommuteViewModel
import com.uwi.btmap.views.fragments.register.*

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

        val viewModel = ViewModelProvider(this).get(RegisterCommuteViewModel::class.java)
        viewModel.token = getString(R.string.mapbox_access_token)

        //add submit commute success livedata observer
        //switch activity
        val loading = Dialog(this)
        val cont = Dialog(this)


        viewModel.commuteSaveSuccess().observe(this, Observer {

            val mDriver = viewModel.commuteType.value
            val mPassenger = viewModel.commuteType.value

            if (it == true && mDriver == 0) {
                cont.setContentView(R.layout.redirect_view)
                cont.setCancelable(false)
                loading.dismiss()
                cont.show()

                val redirect = cont.findViewById(R.id.dialog_continue) as Button

                redirect.setOnClickListener {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
            }

            if (it == true && mPassenger == 1){
                cont.setContentView(R.layout.redirect_view)
                cont.setCancelable(false)
                loading.dismiss()
                cont.show()

                val redirect = cont.findViewById(R.id.dialog_continue) as Button

                redirect.setOnClickListener {
                    viewModel.findPairSuccess().observe(this, Observer { it1 ->
                        if (it1) {
                            //move this from the fragment ot the view model
                            val intent = Intent(this, SelectPairActivity::class.java)
                                .putExtra("CommuteOptions", viewModel.commuteOptions)
                                .putExtra("PassengerOrigin", viewModel.origin.value)
                                .putExtra("PassengerDestination", viewModel.destination.value)
                                .putExtra("OriginAddress", viewModel.originAddress.value)
                                .putExtra("DestinationAddress", viewModel.destinationAddress.value)
                            startActivity(intent)
                        }
                    })
                }
            }

        })

        viewModel.isLoading().observe(this, Observer { currLoading ->
            if (currLoading == true) {
                //loadingDialog()
                loading.setCancelable(false)
                loading.setContentView(R.layout.loading_view)
                loading.show()
            }
        })

        //add pair commute success livedata observer
        //switch activity
//        viewModel.findPairSuccess().observe(this, Observer {
//            if (it) {
//                //move this from the fragment ot the view model
//                val intent = Intent(this, SelectPairActivity::class.java)
//                    .putExtra("CommuteOptions", viewModel.commuteOptions)
//                    .putExtra("PassengerOrigin", viewModel.origin.value)
//                    .putExtra("PassengerDestination", viewModel.destination.value)
//                    .putExtra("OriginAddress", viewModel.originAddress.value)
//                    .putExtra("DestinationAddress", viewModel.destinationAddress.value)
//                startActivity(intent)
//            }
//        })


    }


    private fun loadingDialog() {
        val loading = Dialog(this)
        loading.setContentView(R.layout.loading_view)
        loading.show()
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

    fun setNextPage() {
        pager.currentItem = pager.currentItem + 1
    }

    fun setPrevPage() {
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
