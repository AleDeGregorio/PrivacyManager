package it.polito.s294545.privacymanager.customDataClasses

import kotlinx.serialization.Serializable

@Serializable
class CustomAddress {
    var address: String? = null
    var latitude: Double? = null
    var longitude: Double? = null

    override fun toString(): String {
        return "(\"$address\", $latitude, $longitude)"
    }
}