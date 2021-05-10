package com.uwi.btmap

import android.content.Context
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.geojson.Point

import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.LineString
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.navigation.base.internal.extensions.applyDefaultParams
import com.mapbox.navigation.base.internal.extensions.coordinates
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.directions.session.RoutesRequestCallback
import java.io.Serializable

class Commute() : Serializable{
    /* Stores and generated commute route information  */
    private var tag = "Commute"

    private var driverRoute: DirectionsRoute? = null
    private var passengerRoute: DirectionsRoute? = null
    private var origin: Point?  = null
    private var destination: Point?  = null
    private var pickup: Point?  = null
    private var dropOff: Point? = null
    private var passenger: Point? = null

    fun setDriverRoute(route:DirectionsRoute){
        this.driverRoute = route
    }
    fun getDriverRoute(): DirectionsRoute {
        return this.driverRoute!!
    }

    fun setPassengerRoute(route:DirectionsRoute){
        this.passengerRoute = route
    }
    fun getPassengerRoute(): DirectionsRoute {
        return this.passengerRoute!!
    }

    fun setOrigin(origin: Point){
        this.origin = origin
    }
    fun getOrigin(): Point{
        return this.origin!!
    }

    fun setDestination(destination: Point){
        this.destination = destination
    }
    fun getDestination(): Point{
        return this.destination!!
    }

    fun setPickup(pickup: Point){
        this.pickup = pickup
    }
    fun getPickup(): Point{
        return this.pickup!!
    }

    fun setDropOff(dropOff: Point){
        this.dropOff = dropOff
    }
    fun getDropOff(): Point{
        return this.dropOff!!
    }

    fun setPassenger(passenger: Point){
        this.passenger = passenger
    }
    fun getPassenger(): Point{
        return this.passenger!!
    }

    fun isValid(): Boolean{
        return origin!=null && destination!=null && pickup!=null && dropOff!=null
    }

    fun generatePickupPoint(): Boolean{
        if (origin!=null && dropOff!=null && passenger!=null) {
            pickup = PickUpPointGenerator().generatePickupPoint(this.origin!!,this.dropOff!!,passenger!!)
            return true
        }
        return false
    }

    fun pairedRouteOptions(token: String): RouteOptions{
            val wayPoints = listOf<Point>(pickup!!, dropOff!!)

            return RouteOptions.builder()
                    .applyDefaultParams()
                    .accessToken(token)
                    .coordinates(origin!!,wayPoints,destination!!)
                    .alternatives(true)
                    .profile(DirectionsCriteria.PROFILE_DRIVING)
                    .build()
    }

    fun passengerRouteOptions(token: String): RouteOptions{
        return RouteOptions.builder()
                .applyDefaultParams()
                .accessToken(token)
                .coordinates(passenger!!, listOf<Point>(), pickup!!)
                .alternatives(true)
                .profile(DirectionsCriteria.PROFILE_DRIVING)
                .build()
    }
}