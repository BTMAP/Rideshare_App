package com.uwi.btmap.models

import java.io.Serializable

data class PairableCommute(val commuteId:String, val time:String, val eta:String, val pickupPoints:List<IndexedCoord>, val dropoffPoints:List<IndexedCoord>):
    Serializable
