package com.uwi.btmap.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.uwi.btmap.models.Commute

class PreviewCommuteViewModel:ViewModel() {
    var commute = MutableLiveData<Commute>()

    fun commute():LiveData<Commute>{ return commute }
}