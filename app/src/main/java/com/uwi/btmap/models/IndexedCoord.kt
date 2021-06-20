package com.uwi.btmap.models

import java.io.Serializable

data class IndexedCoord(val chunk:String, val lat:Float, val lng:Float, val routeIndex:Int): Serializable
