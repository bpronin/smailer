package com.bopr.android.smailer;

import android.content.Context;
import android.location.Location;
import android.os.Looper;

import androidx.annotation.Nullable;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.bopr.android.smailer.PermissionsHelper.isLocationPermissionsGranted;
import static com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY;

/**
 * Provides last device location.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class GeoLocator {

    private static Logger log = LoggerFactory.getLogger("GeoLocator");

    private static final int DEFAULT_TIMEOUT = 2000;

    private final Context context;
    private final FusedLocationProviderClient client;
    private final Database database;

    public GeoLocator(Context context, Database database) {
        this.context = context;
        this.database = database;
        client = LocationServices.getFusedLocationProviderClient(context);
    }

    /**
     * Retrieve device location.
     *
     * @return last known device location or null when it cannot be found
     */
    @Nullable
    public GeoCoordinates getLocation() {
        return getLocation(DEFAULT_TIMEOUT);
    }

    /**
     * Retrieve device location.
     *
     * @return last known device location or null when it cannot be found
     */
    @Nullable
    public GeoCoordinates getLocation(long timeout) {
        GeoCoordinates coordinates = getCurrentLocation(timeout);
        if (coordinates == null) {
            coordinates = getLastLocation(timeout);
        }

        if (coordinates != null) {
            database.saveLastLocation(coordinates);
        } else {
            coordinates = database.getLastLocation();
            if (coordinates != null) {
                log.debug("Using location from local database");
            } else {
                log.error("Unable to obtain location from database");
            }
        }

        return coordinates;
    }

    @Nullable
    private GeoCoordinates getCurrentLocation(long timeout) {
        if (!isLocationPermissionsGranted(context)) {
            log.warn("Unable to read current location. Permission denied.");
            return null;
        }

        final AtomicReference<GeoCoordinates> coordinates = new AtomicReference<>();
        final CountDownLatch completeSignal = new CountDownLatch(1);

        final LocationRequest request = new LocationRequest()
                .setFastestInterval(1500)
                .setInterval(3000)
                .setPriority(PRIORITY_HIGH_ACCURACY);

        LocationCallback callback = new LocationCallback() {

            @Override
            public void onLocationResult(LocationResult result) {
                if (result != null) {
                    Location location = result.getLastLocation();
                    coordinates.set(new GeoCoordinates(location));

                    log.debug("Received current location: " + location);
                }
                completeSignal.countDown();
            }
        };

        client.requestLocationUpdates(request, callback, Looper.getMainLooper());
        awaitCompletion(completeSignal, timeout, "Current location request");
        client.removeLocationUpdates(callback);

        return coordinates.get();
    }

    @Nullable
    private GeoCoordinates getLastLocation(long timeout) {
        if (!isLocationPermissionsGranted(context)) {
            log.warn("Unable to read last location. Permission denied.");
            return null;
        }

        final AtomicReference<GeoCoordinates> coordinates = new AtomicReference<>();
        final CountDownLatch completeSignal = new CountDownLatch(1);

        client.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                coordinates.set(new GeoCoordinates(location));
                completeSignal.countDown();

                log.debug("Received last location: " + location);
            }
        });

        awaitCompletion(completeSignal, timeout, "Last location request");

        return coordinates.get();
    }

    private void awaitCompletion(CountDownLatch latch, long timeout, String requestName) {
        try {
            if (!latch.await(timeout, TimeUnit.MILLISECONDS)) {
                log.warn(requestName + " timeout expired");
            }
        } catch (InterruptedException x) {
            log.warn(requestName + " interrupted", x);
        }
    }

}
