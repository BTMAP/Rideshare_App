package com.uwi.btmap.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.uwi.btmap.models.CommuteOptions

class SelectPairViewModel: ViewModel() {
    val pairableCommutes = MutableLiveData<List<CommuteOptions>>()
}