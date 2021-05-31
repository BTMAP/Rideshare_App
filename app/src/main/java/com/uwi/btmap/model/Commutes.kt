package com.uwi.btmap.model

data class Commutes(
    var driverName: String? = null,
    var passengerName: String? = null,
    val commuteDate: String? = null,
    val commuteTime: String? = null
)
