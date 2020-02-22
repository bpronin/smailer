package com.bopr.android.smailer

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.location.Location
import android.os.Looper
import com.bopr.android.smailer.util.Mockable
import com.bopr.android.smailer.util.checkPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices.getFusedLocationProviderClient
import org.slf4j.LoggerFactory
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Provides last device location.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
@Mockable
class GeoLocator(private val context: Context, private val database: Database) {

    private val client: FusedLocationProviderClient = getFusedLocationProviderClient(context)

    /**
     * Retrieve device location.
     *
     * @return last known device location or null when it cannot be found
     */
    fun getLocation(): GeoCoordinates? = getLocation(DEFAULT_TIMEOUT.toLong())

    /**
     * Retrieve device location.
     *
     * @return last known device location or null when it cannot be found
     */
    fun getLocation(timeout: Long): GeoCoordinates? {
        var coordinates = getCurrentLocation(timeout)

        if (coordinates == null) {
            coordinates = getLastLocation(timeout)
        }

        if (coordinates == null) {
            coordinates = database.lastLocation
            if (coordinates != null) {
                log.debug("Using location from local database")
            } else {
                log.warn("Unable to obtain location from database")
            }
        } else {
            database.putLastLocation(coordinates)
        }

        return coordinates
    }

    private fun getCurrentLocation(timeout: Long): GeoCoordinates? {
        if (!isLocationPermissionsGranted(context)) {
            log.warn("Unable to read current location. Permission denied.")
            return null
        }

        var coordinates: GeoCoordinates? = null
        val latch = CountDownLatch(1)

        val callback: LocationCallback = object : LocationCallback() {

            override fun onLocationResult(result: LocationResult?) {
                result?.let {
                    val location = it.lastLocation

                    log.debug("Received current location: $location")
                    coordinates = GeoCoordinates(location)
                }
                latch.countDown()
            }
        }

        client.requestLocationUpdates(LocationRequest(), callback, Looper.getMainLooper())
        awaitLatch(latch, timeout, "Current location request")
        client.removeLocationUpdates(callback)

        return coordinates
    }

    private fun getLastLocation(timeout: Long): GeoCoordinates? {
        if (!isLocationPermissionsGranted(context)) {
            log.warn("Unable to read last location. Permission denied.")
            return null
        }

        var coordinates: GeoCoordinates? = null
        val latch = CountDownLatch(1)

        client.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                log.debug("Received last location: $location")

                coordinates = GeoCoordinates(location)
                latch.countDown()
            }
        }
        awaitLatch(latch, timeout, "Last location request")

        return coordinates
    }

    private fun awaitLatch(latch: CountDownLatch, timeout: Long, requestName: String) {
        try {
            if (!latch.await(timeout, TimeUnit.MILLISECONDS)) {
                log.warn("$requestName timeout expired")
            }
        } catch (x: InterruptedException) {
            log.warn("$requestName interrupted", x)
        }
    }

    companion object {

        private val log = LoggerFactory.getLogger("GeoLocator")
        private const val DEFAULT_TIMEOUT = 1000

        fun isLocationPermissionsGranted(context: Context): Boolean {
            return checkPermission(context, ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION)
        }
    }

}