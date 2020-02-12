package com.bopr.android.smailer

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.location.Location
import android.os.Looper
import com.bopr.android.smailer.util.AndroidUtil.isPermissionsDenied
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices.getFusedLocationProviderClient
import org.slf4j.LoggerFactory
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/**
 * Provides last device location.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
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
                log.error("Unable to obtain location from database")
            }
        } else {
            database.saveLastLocation(coordinates)
        }

        return coordinates
    }

    private fun getCurrentLocation(timeout: Long): GeoCoordinates? {
        if (isPermissionsDenied(context)) {
            log.warn("Unable to read current location. Permission denied.")
            return null
        }

        val coordinates = AtomicReference<GeoCoordinates>()
        val completeSignal = CountDownLatch(1)

        val callback: LocationCallback = object : LocationCallback() {

            override fun onLocationResult(result: LocationResult?) {
                result?.let {
                    val location = it.lastLocation
                    coordinates.set(GeoCoordinates(location))

                    log.debug("Received current location: $location")
                }
                completeSignal.countDown()
            }
        }

        client.requestLocationUpdates(LocationRequest(), callback, Looper.getMainLooper())
        awaitCompletion(completeSignal, timeout, "Current location request")
        client.removeLocationUpdates(callback)

        return coordinates.get()
    }

    private fun getLastLocation(timeout: Long): GeoCoordinates? {
        if (isPermissionsDenied(context)) {
            log.warn("Unable to read last location. Permission denied.")
            return null
        }

        val coordinates = AtomicReference<GeoCoordinates>()
        val completeSignal = CountDownLatch(1)

        client.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                coordinates.set(GeoCoordinates(location))
                completeSignal.countDown()

                log.debug("Received last location: $location")
            }
        }
        awaitCompletion(completeSignal, timeout, "Last location request")

        return coordinates.get()
    }

    private fun awaitCompletion(latch: CountDownLatch, timeout: Long, requestName: String) {
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

        fun isPermissionsDenied(context: Context?): Boolean {
            return isPermissionsDenied(context, ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION)
        }
    }

}