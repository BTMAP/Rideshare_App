package com.uwi.btmap.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.uwi.btmap.models.Commute

class PreviewCommuteViewModel : ViewModel() {
    var commute = MutableLiveData<Commute>()

    var drivingDirectionsRoute = MutableLiveData<DirectionsRoute>()
    var firstLegDirectionsRoute = MutableLiveData<DirectionsRoute>()
    var lastLegDirectionsRoute = MutableLiveData<DirectionsRoute>()

    fun commute(): LiveData<Commute> {
        return commute
    }

    fun drivingDirectionsRoute(): LiveData<DirectionsRoute> {
        return drivingDirectionsRoute
    }

    fun firstLegDirectionsRoute(): LiveData<DirectionsRoute> {
        return firstLegDirectionsRoute
    }

    fun lastLegDirectionsRoute(): LiveData<DirectionsRoute> {
        return lastLegDirectionsRoute
    }
}