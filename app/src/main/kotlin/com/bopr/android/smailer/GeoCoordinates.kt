package com.bopr.android.smailer

import android.location.Location
import android.os.Parcelable
import com.bopr.android.smailer.util.TextUtil.decimalToDMS
import kotlinx.android.parcel.Parcelize

/**
 * Geolocation coordinates.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
@Parcelize
data class GeoCoordinates(
        val latitude: Double = 0.0,
        val longitude: Double = 0.0) : Parcelable {

    constructor(location: Location) : this(location.latitude, location.longitude)

    fun format(degreeSymbol: String = "°",
               minuteSymbol: String = "\'",
               secondSymbol: String = "\"",
               northSymbol: String = "N",
               southSymbol: String = "S",
               westSymbol: String = "W",
               eastSymbol: String = "E"): String {
        return StringBuilder()
                .append(decimalToDMS(latitude, degreeSymbol, minuteSymbol, secondSymbol))
                .append(if (latitude > 0) northSymbol else southSymbol)
                .append(", ")
                .append(decimalToDMS(longitude, degreeSymbol, minuteSymbol, secondSymbol))
                .append(if (longitude > 0) westSymbol else eastSymbol)
                .toString()
    }

    fun format(): String = format("°", "\'", "\"", "N", "S", "W", "E")

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GeoCoordinates

        if (latitude != other.latitude) return false
        if (longitude != other.longitude) return false

        return true
    }

    override fun hashCode(): Int {
        var result = latitude.hashCode()
        result = 31 * result + longitude.hashCode()
        return result
    }

    companion object {
        fun geoCoordinatesOf(latitude: Double?, longitude: Double?): GeoCoordinates? {
            return if (latitude != null && longitude != null) {
                GeoCoordinates(latitude, longitude)
            } else {
                null
            }
        }
    }
}
