package com.bopr.android.smailer.util

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.location.Location
import android.os.Parcelable
import com.google.android.gms.location.LocationServices.getFusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await
import kotlinx.parcelize.Parcelize

/**
 * Geolocation coordinates.
 *
 * @author Boris Pronin ([boris280471@gmail.com](mailto:boris280471@gmail.com))
 */
@Parcelize
data class GeoLocation(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
) : Parcelable {

    fun format(
        degreeSymbol: String = "°",
        minuteSymbol: String = "\'",
        secondSymbol: String = "\"",
        northSymbol: String = "N",
        southSymbol: String = "S",
        westSymbol: String = "W",
        eastSymbol: String = "E",
        separator: String = ", "
    ): String {
        return buildString {
            append(decimalToDMS(latitude, degreeSymbol, minuteSymbol, secondSymbol))
            append(if (latitude > 0) northSymbol else southSymbol)
            append(separator)
            append(decimalToDMS(longitude, degreeSymbol, minuteSymbol, secondSymbol))
            append(if (longitude > 0) westSymbol else eastSymbol)
        }
    }

    companion object {

        fun Location?.toGeoLocation() = this?.run { fromCoordinates(latitude, longitude) }

        fun fromCoordinates(latitude: Double?, longitude: Double?) =
            if (latitude != null && longitude != null) {
                GeoLocation(latitude, longitude)
            } else null

        /**
         * Obtains geolocation synchronously.
         */
        suspend fun Context.getGeoLocation(): GeoLocation? {
            if (!checkPermission(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION))
                throw IllegalStateException("Geolocation permissions required")

            val client = getFusedLocationProviderClient(this)
            return client.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                CancellationTokenSource().token
            ).await().toGeoLocation()
        }
        
    }
}
