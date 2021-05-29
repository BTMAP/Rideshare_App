package com.uwi.btmap.bll

import com.mapbox.geojson.Point
import com.mapbox.turf.TurfMeasurement
import kotlin.math.*

class PickUpPointSuggester(){
    private val TAG : String = "PickUpPoint Generator";

    fun generatePickupPoint(origin : Point, dropOff : Point, passengerOrigin : Point) : Point{
        val distance = 1.0

        val closestPoint = generateClosetPoint(origin, dropOff, passengerOrigin);
        val pickUpPoint = generatePointBetween(passengerOrigin,closestPoint, distance)
        
        return pickUpPoint 
    }

    private fun generateClosetPoint(a : Point, b: Point, c : Point): Point {
        val threshold = 0.01
        val pointD = TurfMeasurement.midpoint(a,b)

        val distanceCA = getDistanceBetween(c,a)
        val distanceCB = getDistanceBetween(c,b)
        
        if (abs(distanceCA - distanceCB) <= threshold)
            
            return pointD
        if (distanceCA < distanceCB)
            return generateClosetPoint(a, pointD, c)
        else
            return generateClosetPoint(pointD, b, c)
    }

    private fun generatePointBetween(a : Point, b : Point, distance : Double) : Point {
        val bearing = TurfMeasurement.bearing(a,b)
        val c = TurfMeasurement.destination(a,distance,bearing,"kilometers")
        return c
    }

    private fun getDistanceBetween(a : Point, b : Point) : Double{
        val distance = TurfMeasurement.distance(a,b)
        return distance
    }
}