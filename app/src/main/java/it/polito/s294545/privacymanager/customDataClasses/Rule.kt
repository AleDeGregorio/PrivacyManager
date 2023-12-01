package it.polito.s294545.privacymanager.customDataClasses

import kotlinx.serialization.Serializable

@Serializable
class Rule {
    var name: String? = null
    var permissions: List<String>? = null
    var apps: List<String>? = null
    var timeSlot: TimeSlot? = null
    var positions: List<CustomAddress>? = null
    var networks: List<String>? = null
    var bt: List<String>? = null
    var battery: Int? = null
    var action: String? = null
    var active = false

    override fun toString(): String {

        return "Name:\t$name\nPermissions:\t$permissions\nApps:\t$apps\nTime slot:\t$timeSlot\n" +
                "Positions:\t$positions\nNetworks:\t$networks\nBluetooth:\t$bt\nBattery:\t$battery\n" +
                "Action:\t$action\nActive:\t$active\n"
    }
}