package com.uwi.btmap.models

import java.io.Serializable

data class CommuteOptions(val pairs:List<PairableCommute>): Serializable {

    constructor() : this(listOf<PairableCommute>())
}
