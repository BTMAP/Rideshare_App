package com.uwi.btmap.BLL

import androidx.lifecycle.ViewModel
import java.sql.Time
import java.util.*

class CommuteViewModel : ViewModel() {
    var commute = Commute()
    var commuteType = 0
    var commuteDate = Date()
    var commuteArrivalTime : Time? = null
}