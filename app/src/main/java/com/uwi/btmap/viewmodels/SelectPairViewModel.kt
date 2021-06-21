package com.uwi.btmap.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mapbox.geojson.Point
import com.uwi.btmap.models.CommuteOptions

class SelectPairViewModel: ViewModel() {
    var commuteOptions = MutableLiveData<CommuteOptions>()
    var origin = MutableLiveData<Point>()
    var destination = MutableLiveData<Point>()

    var currentFragment = MutableLiveData<Int>()

    init {
        currentFragment.value = 0
    }

    fun commuteOptions(): LiveData<CommuteOptions>{
        return  commuteOptions
    }

    fun currentFragment(): LiveData<Int>{
        return  currentFragment
    }
}