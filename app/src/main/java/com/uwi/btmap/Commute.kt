package com.uwi.btmap

import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.geojson.Point

import android.os.Parcel
import android.os.Parcelable

class Commute() : Parcelable{
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