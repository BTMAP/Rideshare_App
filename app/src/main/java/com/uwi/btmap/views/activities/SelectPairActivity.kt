package com.uwi.btmap.views.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mapbox.geojson.Point
import com.uwi.btmap.R
import com.uwi.btmap.models.CommuteOptions
import com.uwi.btmap.viewmodels.SelectPairViewModel
import com.uwi.btmap.views.fragments.commuteList.PreviewCommutePairFragment
import com.uwi.btmap.views.fragments.selectPair.ListCommutePairFragment

const val TAG = "ListCommutePairs"

class SelectPairActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_pair)

        val viewModel = ViewModelProvider(this).get(SelectPairViewModel::class.java)

        viewModel.commuteOptions.value = intent.getSerializableExtra("CommuteOptions") as CommuteOptions
//        viewModel.origin.value = intent.getSerializableExtra("PassengerOrigin") as Point
//        viewModel.destination.value = intent.getSerializableExtra("PassengerDestination") as Point

        val fragment = ListCommutePairFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.pair_select_fragment,fragment)
        transaction.commit()
    }

    override fun onBackPressed() {
        //check current fragment
        //switch fragment or
        super.onBackPressed()
    }

    private fun replaceFragment(fragment: Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.pair_select_fragment,fragment)
        fragmentTransaction.commit()
    }

}