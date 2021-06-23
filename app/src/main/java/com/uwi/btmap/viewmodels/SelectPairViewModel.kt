package com.uwi.btmap.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.geojson.Point
import com.uwi.btmap.models.CommuteOptions
import com.uwi.btmap.models.IndexedCoord
import com.uwi.btmap.models.PassengerCommuteEstimate
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

private const val TAG = "SelectPairViewModel"

class SelectPairViewModel: ViewModel() {
    var currentCommuteIndex = MutableLiveData<Int>()
    var commuteOptions = MutableLiveData<CommuteOptions>()

    var origin = MutableLiveData<Point>()
    var destination = MutableLiveData<Point>()

    var firstLegRoute = MutableLiveData<DirectionsRoute>()
    var drivingLegRoute = MutableLiveData<DirectionsRoute>()
    var lastLegRoute = MutableLiveData<DirectionsRoute>()

    var commuteEstimates = MutableLiveData<PassengerCommuteEstimate>()

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

/* ---------------- Server Functions ---------------- */
    fun pair(){
        /*val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
        var url = "http://smallkins.pythonanywhere.com/add_commute"
        val json = "{\n" +
                "    \"driverId\":\"$driverId\",\n" +
                "    \"polyline\":\"${polyline}\",\n" +
                "    \"time\":[$year,$month,$day,$hour,$minute],\n" +
                "    \"eta\":[$etaYear,$etaMonth,$etaDay,$etaHour,$etaMinute]\n" +
                "}"

        val rBody: RequestBody = json.toRequestBody(JSON)

        val request = Request.Builder()
            .url(url)
            .post(rBody)
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object: okhttp3.Callback {
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                Log.d(TAG, "onResponse: ${response.body?.string()} ")
                commuteSaveSuccess.postValue(true)
            }

            override fun onFailure(call: okhttp3.Call, e: IOException) {
                //log error
            }
        })*/
    }

    fun getPairEstimates(commuteId:String, origin:Point, destination:Point, pickupPoint: IndexedCoord, dropoffPoint: IndexedCoord){
        var url = "http://smallkins.pythonanywhere.com/get_pair_estimates"
        val query = "?commuteId=$commuteId" +
                "&origin={\"lat\":${origin.latitude()},\"lng\":${origin.longitude()}}"+
                "&destination={\"lat\":${destination.latitude()},\"lng\":${destination.longitude()}}"+
                "&pickupPoint={\"lat\":${pickupPoint.lat},\"lng\":${pickupPoint.lng}}"+
                "&dropoffPoint={\"lat\":${dropoffPoint.lat},\"lng\":${dropoffPoint.lng}}"+
                "&dropoffPoint={\"lat\":${dropoffPoint.lat},\"lng\":${dropoffPoint.lng}}"

        Log.d(TAG, "getPairEstimates: $query")

        val request = Request.Builder()
            .url(url+query)
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object: okhttp3.Callback {
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                val body = response.body?.string()
                Log.d(TAG, "onResponse: $body")
                commuteEstimates.value = GsonBuilder().create().fromJson(
                    body,
                    PassengerCommuteEstimate::class.java)

            }

            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.d(TAG, "onFailure: getEstimates failed - ${e.message}")
            }
        })
    }
}