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

class Commute() : Parcelable{
    private var tag = "Commute"

    private var route: DirectionsRoute? = null
    private var origin: Point?  = null
    private var destination: Point?  = null
    private var pickup: Point?  = null
    private var dropOff: Point? = null

    constructor(parcel: Parcel) : this() {

    }

    fun setRoute(route:DirectionsRoute){
        this.route = route
    }
    fun getRoute(): DirectionsRoute {
        return this.route!!
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

    fun isValid(): Boolean{
        return origin!=null && destination!=null && pickup!=null && dropOff!=null
    }

    fun generatePickupPoint(passengerOrigin: Point?): Boolean{
        if (origin!=null && dropOff!=null && passengerOrigin!=null) {
            pickup = PickUpPointGenerator().generatePickupPoint(this.origin!!,this.dropOff!!,passengerOrigin!!)
            return true
        }
        return false
    }

    fun pairedRouteOptions(token: String): RouteOptions?{
            val wayPoints = listOf<Point>(pickup!!, dropOff!!)

            return RouteOptions.builder()
                    .applyDefaultParams()
                    .accessToken(token)
                    .coordinates(origin!!,wayPoints,destination!!)
                    .alternatives(true)
                    .profile(DirectionsCriteria.PROFILE_DRIVING)
                    .build()
    }



    override fun writeToParcel(parcel: Parcel, flags: Int) {

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Commute> {
        override fun createFromParcel(parcel: Parcel): Commute {
            return Commute(parcel)
        }

        override fun newArray(size: Int): Array<Commute?> {
            return arrayOfNulls(size)
        }
    }
}