package com.bopr.android.smailer.util

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.location.Location
import android.os.Parcelable
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices.getFusedLocationProviderClient
import kotlinx.coroutines.runBlocking
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
        fun Context.getGeoLocation(): GeoLocation? {
            if (!checkPermission(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION))
                throw IllegalStateException("Geolocation permissions required")

            val client = getFusedLocationProviderClient(this)
            val request = CurrentLocationRequest.Builder()
                .setDurationMillis(5000)
                .build()

            return runBlocking {
                client.getCurrentLocation(request, null).await()
            }.toGeoLocation()
        }

        /**
         * Obtains geolocation asynchronously.
         */
        fun Context.requestGeoLocation(
            onSuccess: (GeoLocation?) -> Unit,
            onError: (Throwable) -> Unit
        ) {
            if (!checkPermission(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION)) {
                onError(IllegalStateException("Geolocation permissions required"))
                return
            }

            val request = CurrentLocationRequest.Builder().build()
            getFusedLocationProviderClient(this)
                .getCurrentLocation(request, null)
                .addOnCompleteListener {
                    onSuccess(it.result.toGeoLocation())
                }
                .addOnFailureListener {
                    onError(it)
                }
        }
    }
}
