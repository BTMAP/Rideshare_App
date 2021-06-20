package com.uwi.btmap.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.uwi.btmap.models.CommuteOptions

class SelectPairViewModel: ViewModel() {
    val commuteOptions = MutableLiveData<CommuteOptions>()


    fun commuteOptions(): LiveData<CommuteOptions>{
        return  commuteOptions
    }

}