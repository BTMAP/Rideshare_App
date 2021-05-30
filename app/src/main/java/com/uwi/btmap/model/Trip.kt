package com.uwi.btmap.model

data class Trip(
    val name: String? = null,
    val bio: String? = null,
    val address: String? = null,
    val email: String? = null
) {
    constructor() : this("", "", "", "")
}
