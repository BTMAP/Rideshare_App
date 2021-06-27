package com.uwi.btmap.models


import com.mapbox.geojson.Point
import java.io.Serializable

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

}
