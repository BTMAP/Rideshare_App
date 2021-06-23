package com.uwi.btmap.models

import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.geojson.Point

import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.navigation.base.internal.extensions.applyDefaultParams
import com.mapbox.navigation.base.internal.extensions.coordinates
import java.io.Serializable

class Commute() : Serializable {
    /* Stores and generated commute route information  */
    private var TAG = "Commute"

    var driverRoute: DirectionsRoute? = null
    var passengerRoute: DirectionsRoute? = null
    var origin: Point? = null
    var destination: Point? = null
    var pickup: Point? = null
    var dropOff: Point? = null
    var passenger: Point? = null

    fun isValid(): Boolean {
        return origin != null && destination != null && pickup != null && dropOff != null
    }

    fun generatePickupPoint(): Boolean {
//        if (origin!=null && dropOff!=null && passenger!=null) {
//            pickup = PickUpPointGenerator().generatePickupPoint(this.origin!!,this.dropOff!!,passenger!!)
//            return true
//        }
        if (passenger != null) {
            pickup = passenger
            return true
        }
        return false
    }

    fun pairedRouteOptions(token: String): RouteOptions {
        val wayPoints = listOf<Point>(pickup!!, dropOff!!)

        return RouteOptions.builder()
            .applyDefaultParams()
            .accessToken(token)
            .coordinates(origin!!, wayPoints, destination!!)
            .alternatives(true)
            .profile(DirectionsCriteria.PROFILE_DRIVING)
            .voiceInstructions(true)
            .steps(true)
            .build()
    }

    fun passengerRouteOptions(token: String): RouteOptions {
        return RouteOptions.builder()
            .applyDefaultParams()
            .accessToken(token)
            .coordinates(passenger!!, listOf<Point>(), pickup!!)
            .alternatives(true)
            .profile(DirectionsCriteria.PROFILE_WALKING)
            .voiceInstructions(true)
            .steps(true)
            .build()
    }
}