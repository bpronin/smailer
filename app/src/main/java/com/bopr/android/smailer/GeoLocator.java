package com.bopr.android.smailer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.HandlerThread;

import com.bopr.android.smailer.util.AndroidUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import androidx.annotation.Nullable;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;
import static android.location.LocationManager.PASSIVE_PROVIDER;

/**
 * Provides last device location.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class GeoLocator {

    private static Logger log = LoggerFactory.getLogger("GeoLocator");

    private static final int DEFAULT_TIMEOUT = 5000;

    private final Context context;
    private final Database database;
    private LocationManager locationManager;

    public GeoLocator(Context context, Database database) {
        this.context = context;
        this.database = database;

        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    void setLocationManager(LocationManager locationManager) {
        this.locationManager = locationManager;
    }

    /**
     * Tries to retrieve device location from different providers.
     *
     * @return last known device location or null when it cannot be found
     */
    @Nullable
    public GeoCoordinates getLocation() {
        return getLocation(DEFAULT_TIMEOUT);
    }

    /**
     * Tries to retrieve device location from different providers.
     *
     * @return last known device location or null when it cannot be found
     */
    @Nullable
    public GeoCoordinates getLocation(long timeout) {
        if (isPermissionsDenied(context)) {
            log.warn("Unable read location. Permission denied.");
            return null;
        }

        GeoCoordinates coordinates = getProviderLocation(GPS_PROVIDER, timeout);
        if (coordinates == null) {
            coordinates = getProviderLocation(NETWORK_PROVIDER, timeout);
            if (coordinates == null) {
                coordinates = getLastProviderLocation(PASSIVE_PROVIDER);
            }
        }

        if (coordinates != null) {
            database.saveLastLocation(coordinates);
        } else {
            coordinates = database.getLastLocation();
            if (coordinates != null) {
                log.debug("Using local database");
            } else {
                log.warn("Unable to obtain location from database");
            }
        }

        return coordinates;
    }

    @Nullable
    private GeoCoordinates getProviderLocation(String provider, long timeout) {
        if (locationManager.isProviderEnabled(provider)) {
            GeoCoordinates coordinates = requestProviderLocation(provider, timeout);
            if (coordinates != null) {
                log.debug("Using " + provider);
                return coordinates;
            }
        }
        return null;
    }

    @SuppressWarnings({"ResourceType", "SameParameterValue"})
    @SuppressLint("MissingPermission")
    @Nullable
    private GeoCoordinates getLastProviderLocation(String provider) {
        if (locationManager.isProviderEnabled(provider)) {
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                log.debug("Using last " + provider);
                return new GeoCoordinates(location);
            }
        }
        return null;
    }

    /**
     * Requests provider for location updates and waits until first update.
     * If timeout expired returns null.
     * Caution: this method blocks caller thread for "timeout" time.
     *
     * @param provider location provider name
     * @param timeout  max await time in seconds
     * @return location or null if timeout expired
     */
    @SuppressWarnings("ResourceType")
    @SuppressLint("MissingPermission")
    @Nullable
    private GeoCoordinates requestProviderLocation(String provider, long timeout) {
        log.debug("Requesting location from: " + provider);

        final AtomicReference<GeoCoordinates> result = new AtomicReference<>();
        final CountDownLatch completeSignal = new CountDownLatch(1);

        LocationListener listener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                log.debug("Received location: " + location);
                result.set(new GeoCoordinates(location));
                completeSignal.countDown();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                /* do nothing*/
            }

            @Override
            public void onProviderEnabled(String provider) {
                /* do nothing*/
            }

            @Override
            public void onProviderDisabled(String provider) {
                /* do nothing*/
            }
        };

        HandlerThread thread = new HandlerThread("location_request");
        thread.start();
        locationManager.requestSingleUpdate(provider, listener, thread.getLooper());

        try {
            if (!completeSignal.await(timeout, TimeUnit.MILLISECONDS)) {
                log.warn("Location request timeout expired");
            }
        } catch (InterruptedException x) {
            log.warn("Location request interrupted", x);
        } finally {
            locationManager.removeUpdates(listener);
            thread.quit();
        }

        return result.get();
    }

    static boolean isPermissionsDenied(Context context) {
        return AndroidUtil.isPermissionsDenied(context, ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION);
    }

}
