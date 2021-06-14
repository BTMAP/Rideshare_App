package com.uwi.btmap.models

data class User(
    val name: String? = null,
    val bio: String? = null,
    val address: String? = null,
    val email: String? = null,
    val profileImageUrl: String? = null
) {
    constructor() : this("", "", "", "", "")
}

