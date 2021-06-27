package com.uwi.btmap.models

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

data class PassengerCommuteEstimate(
    val time: String,
    val eta: String,
    val walkingDistance: Float,
    val walkingDuration: Float
) {
    fun getDate(): String {
        val date = parseIsoDateTime(time)
        return formatDateToDateString(date)
    }

    fun getStartTime(): String {
        val date = parseIsoDateTime(time)
        return formatDateToTimeString(date)
    }

    fun getETA(): String {
        val date = parseIsoDateTime(eta)
        return formatDateToTimeString(date)
    }

    fun getDistanceInKm(): Float {
        return String.format("%.1f", this.walkingDistance / 1000).toFloat()
    }

    fun getDurationInMinutes(): Float {
        return String.format("%.5f", this.walkingDuration / 60).toFloat()
    }

    fun getTimeCalendar():Calendar{
        var timeCal = Calendar.getInstance()
        val date = parseIsoDateTime(time)
        Log.d("estimate", "getTimeCalendar: $time")
        Log.d("estimate", "getDateCalendar: $date")
        timeCal.time = date
        val timeZone = TimeZone.getTimeZone("GMT-4")
        timeCal.timeZone = timeZone
        Log.d("estimate", "getTimeCalendar: ${timeCal.get(Calendar.DAY_OF_MONTH)}")
        return timeCal
    }

    fun getEtaCalendar():Calendar{
        var etaCal = Calendar.getInstance()
        val date = parseIsoDateTime(eta)
        etaCal.time = date
        val timeZone = TimeZone.getTimeZone("GMT-4")
        etaCal.timeZone = timeZone
        return etaCal
    }

    private fun parseIsoDateTime(datetime: String): Date {
        val inFormat = SimpleDateFormat("yyyy-mm-dd'T'HH:mm:ss.SSSSSSZ")
        return inFormat.parse(datetime)
    }

    private fun formatDateToTimeString(date: Date): String {
        val outFormat = SimpleDateFormat("hh:mm a")
        return outFormat.format(date)
    }

    private fun formatDateToDateString(date: Date): String {
        val outFormat = SimpleDateFormat("mm/dd/yyyy")
        return outFormat.format(date)
    }
}
