package com.bopr.android.smailer.util

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.location.Location
import android.os.Parcelable
import com.bopr.android.smailer.data.Database
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices.getFusedLocationProviderClient
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.parcelize.Parcelize

/**
 * Geolocation coordinates.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
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
        return StringBuilder()
            .append(decimalToDMS(latitude, degreeSymbol, minuteSymbol, secondSymbol))
            .append(if (latitude > 0) northSymbol else southSymbol)
            .append(separator)
            .append(decimalToDMS(longitude, degreeSymbol, minuteSymbol, secondSymbol))
            .append(if (longitude > 0) westSymbol else eastSymbol)
            .toString()
    }

    companion object {

        fun Location?.toGeoLocation(): GeoLocation? {
            return this?.run {
                fromCoordinates(latitude, longitude)
            }
        }

        fun fromCoordinates(latitude: Double?, longitude: Double?): GeoLocation? {
            return if (latitude != null && longitude != null) {
                GeoLocation(latitude, longitude)
            } else null
        }

        /**
         * Obtains geolocation synchronously.
         */
        fun Context.getGeoLocation(database: Database? = null): GeoLocation? {
            val location = getCurrentGeoLocation()
            database?.lastLocation = location
            return location
        }

        /**
         * Obtains geolocation asynchronously.
         */
        fun Context.requestGeoLocation(
            database: Database? = null,
            onSuccess: (GeoLocation?) -> Unit,
            onError: (Throwable) -> Unit
        ) {
            requestCurrentGeoLocation(
                onSuccess = { result ->
                    result?.apply {
                        database?.lastLocation = this
                    }
                    onSuccess(result)
                },
                onError
            )
        }

        private fun Context.getCurrentGeoLocation(): GeoLocation? {
            if (!checkPermission(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION))
                throw IllegalStateException("Geolocation permissions required")

            val client = getFusedLocationProviderClient(this)
            val request = CurrentLocationRequest.Builder().build()
            return runBlocking {
                client.getCurrentLocation(request, null).await().toGeoLocation()
            }
        }

        private fun Context.requestCurrentGeoLocation(
            onSuccess: (GeoLocation?) -> Unit,
            onError: (Throwable) -> Unit
        ) {
            if (!checkPermission(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION)) {
                onError(IllegalStateException("Required geolocation permissions"))
                return
            }

            val request = CurrentLocationRequest.Builder().build()
            getFusedLocationProviderClient(this)
                .getCurrentLocation(request, null)
                .addOnCompleteListener {
                    onSuccess(it.result.toGeoLocation())
                }
                .addOnFailureListener { error ->
                    onError(error)
                }
        }

    }
}
