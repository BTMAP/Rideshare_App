package com.uwi.btmap.models

import java.util.*

data class Trip(
    val userId: String? = null,
    val dateTime: Date? = null,
    val originName: String? = null,
    val destinationName: String? = null,
    val originLat: Double? = null,
    val originLng: Double? = null,
    val destinationLat: Double? = null,
    val destinationLng: Double? = null
) {
}
