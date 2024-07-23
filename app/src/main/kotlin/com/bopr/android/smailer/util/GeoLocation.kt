package com.bopr.android.smailer.util

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.location.Location
import android.location.LocationRequest.PASSIVE_INTERVAL
import android.os.Build
import android.os.Looper
import android.os.Parcelable
import com.bopr.android.smailer.data.Database
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices.getFusedLocationProviderClient
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
            } else {
                null
            }
        }

        fun Context.requestGeoLocation(
            database: Database? = null,
            onComplete: (GeoLocation?) -> Unit
        ) {
            requestCurrentLocation { result ->
                result?.apply {
                    database?.lastLocation = this
                }
                onComplete(result)
            }
        }

        private fun Context.requestCurrentLocation(onComplete: (GeoLocation?) -> Unit) {
            if (!checkPermission(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION)) {
                onComplete(null)
                return
            }

            val request = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                LocationRequest.Builder(PASSIVE_INTERVAL).build()
            } else {
                @Suppress("DEPRECATION")
                (LocationRequest())
            }

            val client = getFusedLocationProviderClient(this)
            client.requestLocationUpdates(request, object : LocationCallback() {

                override fun onLocationResult(result: LocationResult) {
                    client.removeLocationUpdates(this)
                    onComplete(result.lastLocation.toGeoLocation())
                }
            }, Looper.getMainLooper())
        }

    }
}
