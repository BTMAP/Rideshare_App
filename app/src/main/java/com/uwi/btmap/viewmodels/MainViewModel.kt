package com.uwi.btmap.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.GsonBuilder
import com.uwi.btmap.models.Commute
import com.uwi.btmap.models.CommuteOptions
import com.uwi.btmap.models.UserCommutes
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.*

private const val TAG = "MainViewModel"

class MainViewModel: ViewModel() {

    var commutes = MutableLiveData<UserCommutes>()
    var getCommutesSuccess = MutableLiveData<Boolean>()

    /* ---------------------- GET LiveDate Functions ----------------------- */

    fun commutes():LiveData<UserCommutes>{ return commutes }

    fun getCommutesSuccess(): LiveData<Boolean>{ return getCommutesSuccess }

    /* -------------------------- API Functions --------------------------- */
    fun getUserCommutes(userId:String){
        var url = "http://smallkins.pythonanywhere.com/get_user_commutes"
        val query = "?userId=$userId"

        Log.d(TAG, "findSuitableCommutePairs: $query")

        val request = Request.Builder()
            .url(url+query)
            .build()
        val client = OkHttpClient()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {

                val body = response.body?.string()

                //handle error server error??
                Log.d(TAG, "onResponse: ${ body }")
                val commutesResponse = GsonBuilder().create().fromJson(body,
                    UserCommutes::class.java
                )
                Log.d(TAG, "onResponse: ${commutesResponse}")
                commutes.postValue(commutesResponse)
                getCommutesSuccess.postValue(true)
            }

            override fun onFailure(call: okhttp3.Call, e: IOException) {
                //TODO log error
                getCommutesSuccess.postValue(false)
            }
        })
    }
}