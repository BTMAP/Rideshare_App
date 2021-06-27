package com.uwi.btmap.models


import com.mapbox.geojson.Point
import java.io.Serializable
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

data class Commute(
    val userId:String,
    val commuteId:String,
    val commuteType:Int,
    val origin:Coord,
    val destination:Coord,
    val pickupPoint:Coord?,
    val dropoffPoint:Coord?,
    val originAddress:String,
    val destinationAddress:String,
    val time:String,
    val eta:String,
    val isPaired:Boolean
):Serializable{

    fun getOriginPoint():Point{
        return Point.fromLngLat(origin.lng,origin.lat)
    }

    fun getDestinationPoint():Point{
        return Point.fromLngLat(destination.lng,destination.lat)
    }

    fun getPickupPoint():Point?{
        return if (pickupPoint != null) {
            Point.fromLngLat(pickupPoint.lng,pickupPoint.lat)
        }else{
            return null
        }
    }

    fun getDropoffPoint():Point?{
        return if (dropoffPoint != null) {
            Point.fromLngLat(dropoffPoint.lng,dropoffPoint.lat)
        }else{
            return null
        }
    }

    fun getETA(): String {
        val date = parseIsoDateTime(eta)
        return formatDateToTimeString(date)
    }

    fun getCommuteTime(): String {
        val date = parseIsoDateTime(time)
        return formatDateToTimeString(date)
    }

    fun getCommuteDate(): String {
        val date = parseIsoDateTime(time)
        return formatDateToDateString(date)
    }

    @JvmName("getOriginAddress1")
    fun getOriginAddress(): String {
        return originAddress
    }

    @JvmName("getDestinationAddress1")
    fun getDestinationAddress(): String {
        return destinationAddress
    }

    @JvmName("getCommuteType1")
    fun getCommuteType(): Int {
        return commuteType
    }

    fun getIsPaired(): Boolean{
        return isPaired
    }

    private fun parseIsoDateTime(datetime: String): Date {
        val inFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        return inFormat.parse(datetime)
    }

    private fun formatDateToTimeString(date: Date): String {
        val outFormat = SimpleDateFormat("hh:mm a")
        return outFormat.format(date)
    }

    private fun formatDateToDateString(date: Date): String {
        val outFormat = SimpleDateFormat("dd/MM/yyyy")
        return outFormat.format(date)
    }

}
