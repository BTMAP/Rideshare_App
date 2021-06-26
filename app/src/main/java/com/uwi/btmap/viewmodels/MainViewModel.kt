package com.uwi.btmap.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.GsonBuilder
import com.uwi.btmap.models.CommuteOptions
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.*

private const val TAG = "MainViewModel"

class MainViewModel: ViewModel() {

    var commutes = MutableLiveData<String>()

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
//                commuteOptions = GsonBuilder().create().fromJson(body,
//                    CommuteOptions::class.java
//                )
            }

            override fun onFailure(call: okhttp3.Call, e: IOException) {
                //log error
            }
        })
    }
}