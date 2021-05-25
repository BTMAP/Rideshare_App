package com.uwi.btmap.BLL

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.sql.Time
import java.util.*

class CommuteViewModel : ViewModel() {
    var commute = Commute()
    var commuteType = MutableLiveData<Int>()

    var calendar: Calendar = Calendar.getInstance(TimeZone.getDefault())

    var dateString : MutableLiveData<String> = MutableLiveData()
    var timeString : MutableLiveData<String> = MutableLiveData()

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
        return hour.toString() + ":" + minute.toString()
    }
}