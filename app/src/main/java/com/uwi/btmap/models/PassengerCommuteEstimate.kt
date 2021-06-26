package com.uwi.btmap.models

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

    fun getDurationInMinutes(): Int {
        return String.format("%d", this.walkingDuration / 60).toInt()
    }

    private fun parseIsoDateTime(datetime: String): Date {
        val inFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
        return inFormat.parse(datetime)
    }

    private fun formatDateToTimeString(date: Date): String {
        val outFormat = SimpleDateFormat("hh:mm a")
        return outFormat.format(date)
    }

    private fun formatDateToDateString(date: Date): String {
        val outFormat = SimpleDateFormat("dd/mm/yyyy")
        return outFormat.format(date)
    }
}
