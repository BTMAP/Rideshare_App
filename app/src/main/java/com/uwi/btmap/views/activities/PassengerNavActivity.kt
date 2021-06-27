package com.uwi.btmap.views.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.uwi.btmap.R
import com.uwi.btmap.models.Commute
import com.uwi.btmap.viewmodels.PreviewCommuteViewModel
import com.uwi.btmap.views.fragments.passengerNav.PassengerNavMapFragment
import com.uwi.btmap.views.fragments.previewCommute.PreviewCommuteFragment

class PassengerNavActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_passenger_nav)
        val viewModel = ViewModelProvider(this).get(PreviewCommuteViewModel::class.java)

        //get commute and routes from intent
        viewModel.drivingDirectionsRoute.value = intent.getSerializableExtra("DrivingRoute") as DirectionsRoute
        viewModel.firstLegDirectionsRoute.value = intent.getSerializableExtra("FirstLeg") as DirectionsRoute
        viewModel.lastLegDirectionsRoute.value = intent.getSerializableExtra("LastLeg") as DirectionsRoute
        viewModel.commute.value = intent.getSerializableExtra("Commute") as Commute

        //set default fragment
        val fragment = PassengerNavMapFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.passenger_nav_frame,fragment)
        transaction.commit()
    }
}