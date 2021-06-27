package com.uwi.btmap.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.uwi.btmap.models.Commute

class PreviewCommuteViewModel:ViewModel() {
    var commute = MutableLiveData<Commute>()

    var drivingDirectionsRoute = MutableLiveData<DirectionsRoute>()

    fun commute():LiveData<Commute>{ return commute }
}