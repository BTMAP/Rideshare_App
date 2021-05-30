package com.uwi.btmap.bll

import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.geojson.Point
import com.uwi.btmap.model.Commute
import java.util.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.auth.FirebaseAuth
import com.uwi.btmap.activities.ProfileActivity
import com.uwi.btmap.model.Trip

class CommuteViewModel : ViewModel() {

    var token = ""
    var commute = Commute()
    var commuteType = MutableLiveData<Int>()

    /* ------------------------ Location Information ----------------------- */
    var origin = MutableLiveData<Point>()
    var destination = MutableLiveData<Point>()

    var routePreview = MutableLiveData<DirectionsRoute>()

    /* --------------------- Date and Time Information --------------------- */
    var calendar: Calendar = Calendar.getInstance(TimeZone.getDefault())
    var dateString : MutableLiveData<String> = MutableLiveData()
    var timeString : MutableLiveData<String> = MutableLiveData()

    var locationSelectionMode = 0

    init {
        commuteType.value = 0

        var year = calendar.get(Calendar.YEAR)
        var month = calendar.get(Calendar.MONTH)
        var day = calendar.get(Calendar.DAY_OF_MONTH)
        var hour = calendar.get(Calendar.HOUR_OF_DAY)
        var minute = calendar.get(Calendar.MINUTE)

        dateString.value = makeDateString(day,month,year)
        timeString.value = makeTimeString(hour,minute)
    }

    fun origin(): LiveData<Point>{
        return origin
    }

    fun destination(): LiveData<Point>{
        return destination
    }

    fun routePreview(): LiveData<DirectionsRoute>{
        return routePreview
    }

    fun setCommuteType(i:Int){
        commuteType.value = i
    }

    fun dateString():LiveData<String>{
        return dateString
    }

    fun setDate(year:Int,month:Int,day:Int){
        var hour = calendar.get(Calendar.HOUR_OF_DAY)
        var minute = calendar.get(Calendar.MINUTE)

        this.calendar.set(year,month,day,hour,minute)
        this.dateString.value = makeDateString(day,month,year)
    }

    fun timeString():LiveData<String>{
        return timeString
    }

    fun setTime(hour:Int,minute:Int){
        var year = calendar.get(Calendar.YEAR)
        var month = calendar.get(Calendar.MONTH)
        var day = calendar.get(Calendar.SECOND)
        this.calendar.set(year,month,day,hour,minute)
        this.timeString.value = makeTimeString(hour,minute)
    }

    private fun makeDateString(day:Int,month:Int,year:Int): String{
        return formatMonth(month) + " " + day + " " + year
    }
    private fun formatMonth(month:Int):String{
        var monthString = ""
        when (month){
            0 -> monthString = "JAN"
            1 -> monthString = "FEB"
            2 -> monthString = "MAR"
            3 -> monthString = "APR"
            4 -> monthString = "MAY"
            5 -> monthString = "JUN"
            6 -> monthString = "JUL"
            7 -> monthString = "AUG"
            8 -> monthString = "SEP"
            9 -> monthString = "OCT"
            10 -> monthString = "NOV"
            11 -> monthString = "DEC"
        }
        return monthString
    }

    private fun makeTimeString(hour:Int,minute:Int):String{
        var hourString = hour.toString()
        var minuteString = minute.toString()

        //check if length of hour or min is 1
        //concatenate with '0'
        if(hourString.length < 2){
            hourString = "0$hourString"
        }

        if(minuteString.length < 2){
            minuteString = "0$minuteString"
        }

        return "$hourString:$minuteString"
    }

    fun saveCommute(){
        var mAuth = FirebaseAuth.getInstance()
        //If not work
        //move to top (line 21 ProfileActivity)
        //reference to Commutes collection in database
        var database = FirebaseDatabase.getInstance().getReference("CommutesTestCollection")

        //create object to store commute(trip) info
        var tripInfo = Trip("name", "bio", "address", "email")

        //set doc in collection
        database.push().setValue(tripInfo)
            .addOnSuccessListener {
            //Toast.makeText(this, "Successfully Saved", Toast.LENGTH_SHORT).show()
        }
            .addOnFailureListener {
            //Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
        }
    }
}