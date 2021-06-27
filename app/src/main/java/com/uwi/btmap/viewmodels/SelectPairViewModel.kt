package com.uwi.btmap.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.geojson.Point
import com.uwi.btmap.models.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.*

private const val TAG = "SelectPairViewModel"

class SelectPairViewModel: ViewModel() {
    var currentCommuteIndex = MutableLiveData<Int>()
    var commuteOptions = MutableLiveData<CommuteOptions>()

    var origin = MutableLiveData<Point>()
    var destination = MutableLiveData<Point>()

    var originAddress = MutableLiveData<String>()
    var destinationAddress = MutableLiveData<String>()

    var firstLegRoute = MutableLiveData<DirectionsRoute>()
    var drivingLegRoute = MutableLiveData<DirectionsRoute>()
    var lastLegRoute = MutableLiveData<DirectionsRoute>()

    var commuteEstimates = MutableLiveData<PassengerCommuteEstimate>()
    var commuteData = MutableLiveData<Commute>()

    var currentFragment = MutableLiveData<Int>()

    var getPairEstimatesSuccess = MutableLiveData<Boolean>()
    var pairSuccess = MutableLiveData<Boolean>()

    init {
        currentFragment.value = 0
    }

    /* ---------------------- GET LiveDate Functions ----------------------- */

    fun commuteOptions(): LiveData<CommuteOptions>{
        return  commuteOptions
    }

    fun currentFragment(): LiveData<Int>{
        return  currentFragment
    }

    fun getPairEstimatesSuccess():LiveData<Boolean>{
        return getPairEstimatesSuccess
    }

    fun pairSuccess():LiveData<Boolean>{
        return pairSuccess
    }


/* -------------------------- Server Functions ------------------------------ */
    fun pair(commuteId:String, origin:Point, destination:Point, pickupPoint: IndexedCoord, dropoffPoint: IndexedCoord){
        val mAuth = FirebaseAuth.getInstance()
        val userId = mAuth.currentUser?.uid

        val time = commuteEstimates.value?.getTimeCalendar()
        val eta = commuteEstimates.value?.getEtaCalendar()

        Log.d(TAG, "pair: $$time")
        Log.d(TAG, "pair: $eta")

        val year = time?.get(Calendar.YEAR)
        val month = time?.get(Calendar.MONTH)?.plus(1)
        val day = time?.get(Calendar.DAY_OF_MONTH)
        val hour = time?.get(Calendar.HOUR_OF_DAY)
        val minute = time?.get(Calendar.MINUTE)

        val etaYear = eta?.get(Calendar.YEAR)
        val etaMonth = eta?.get(Calendar.MONTH)?.plus(1)
        val etaDay = eta?.get(Calendar.DAY_OF_MONTH)
        val etaHour = eta?.get(Calendar.HOUR_OF_DAY)
        val etaMinute = eta?.get(Calendar.MINUTE)

        //TODO homogenize data retrieval method

        val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
        var url = "http://smallkins.pythonanywhere.com/add_pair"
        val json = "{\n" +
                "    \"passengerId\":\"$userId\",\n" +
                "    \"commuteId\":\"${commuteId}\",\n" +
                "    \"origin\":{\"lat\":${origin.latitude()},\"lng\":${origin.longitude()}},\n" +
                "    \"destination\":{\"lat\":${destination.latitude()},\"lng\":${destination.longitude()}},\n" +
                "    \"pickup\":{\"lat\":${pickupPoint.lat},\"lng\":${pickupPoint.lng}},\n" +
                "    \"dropoff\":{\"lat\":${dropoffPoint.lat},\"lng\":${dropoffPoint.lng}},\n" +
                "    \"time\":[$year,$month,$day,$hour,$minute],\n" +
                "    \"eta\":[$etaYear,$etaMonth,$etaDay,$etaHour,$etaMinute],\n" +
                "    \"originAddress\":\"${originAddress.value}\",\n" +
                "    \"destinationAddress\":\"${destinationAddress.value}\"\n" +
                "}"

        Log.d(TAG, "pair: $json")
        val rBody: RequestBody = json.toRequestBody(JSON)

        val request = Request.Builder()
            .url(url)
            .post(rBody)
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object: okhttp3.Callback {
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                val body = response.body?.string()
                Log.d(TAG, "onResponse: $body")
                val apiError = GsonBuilder().create().fromJson(body,
                    BtmapApiError::class.java
                )

                if(!apiError.error){
                    //trigger navigate back to main page
                    pairSuccess.postValue(true)
                }else{
                    pairSuccess.postValue(false)
                }

            }

            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.d(TAG, "onFailure: ${e.message}")
                pairSuccess.postValue(false)
            }
        })
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
                val apiError = GsonBuilder().create().fromJson(body,
                    BtmapApiError::class.java
                )

                commuteEstimates.postValue(
                    GsonBuilder().create().fromJson(
                        body,
                        PassengerCommuteEstimate::class.java
                    )
                )
                if(!apiError.error){
                    getPairEstimatesSuccess.postValue(true)
                }else{
                    getPairEstimatesSuccess.postValue(false)
                }
            }

            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.d(TAG, "onFailure: getEstimates failed - ${e.message}")
                getPairEstimatesSuccess.postValue(false)
            }
        })
    }
}