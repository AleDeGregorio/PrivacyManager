package it.polito.s294545.privacymanager

import kotlinx.serialization.Serializable

@Serializable
class TimeSlot {
    var days = mutableListOf<String>()
    var time = Pair("", "")

    override fun toString(): String {
        return "days: $days\ttime: $time"
    }
}