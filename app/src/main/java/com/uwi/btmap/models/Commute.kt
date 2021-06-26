package com.uwi.btmap.models

data class Commute(
    val userId:String,
    val commuteId:String,
    val commuteType:Int,
    val origin:Coord,
    val destination:Coord,
    val pickupPoint:Coord?,
    val dropoffPoint:Coord?,
    val time:String,
    val eta:String,
    val isPaired:Boolean
)
