package com.uwi.btmap.models

data class UserVehicle(
    val vehicleModel: String? = null,
    val vehicleLicensePlate: String? = null,
    val vehicleImageUrl: String? = null,
    val vehicleColor: String? = null,
    val vehicleMake: String? = null
) {
    constructor() : this("", "", "", "", "")
}
