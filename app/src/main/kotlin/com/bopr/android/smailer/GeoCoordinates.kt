package com.bopr.android.smailer

import android.location.Location
import android.os.Parcelable
import com.bopr.android.smailer.util.decimalToDMS
import kotlinx.parcelize.Parcelize

/**
 * Geolocation coordinates.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
@Parcelize
data class GeoCoordinates(
        val latitude: Double = 0.0,
        val longitude: Double = 0.0) : Parcelable {

    fun format(degreeSymbol: String = "°",
               minuteSymbol: String = "\'",
               secondSymbol: String = "\"",
               northSymbol: String = "N",
               southSymbol: String = "S",
               westSymbol: String = "W",
               eastSymbol: String = "E",
               separator: String = ", "): String {
        return StringBuilder()
                .append(decimalToDMS(latitude, degreeSymbol, minuteSymbol, secondSymbol))
                .append(if (latitude > 0) northSymbol else southSymbol)
                .append(separator)
                .append(decimalToDMS(longitude, degreeSymbol, minuteSymbol, secondSymbol))
                .append(if (longitude > 0) westSymbol else eastSymbol)
                .toString()
    }

    companion object {

        fun coordinatesOf(location: Location?): GeoCoordinates? {
            return location?.run {
                coordinatesOf(latitude, longitude)
            }
        }

        fun coordinatesOf(latitude: Double?, longitude: Double?): GeoCoordinates? {
            return if (latitude != null && longitude != null) {
                GeoCoordinates(latitude, longitude)
            } else {
                null
            }
        }
    }
}
