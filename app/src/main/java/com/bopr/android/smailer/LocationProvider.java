package com.bopr.android.smailer;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;
import static android.location.LocationManager.PASSIVE_PROVIDER;
import static com.bopr.android.smailer.PermissionsChecker.isPermissionsDenied;

/**
 * Provides last device location.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class LocationProvider {

    private static final String TAG = "LocationProvider";
    private static final int DEFAULT_TIMEOUT = 5000;

    private final Context context;
    private final Database database;
    private final GoogleApiClient client;
    private final LocationManager locationManager;

    public LocationProvider(Context context, Database database) {
        this.context = context;
        this.database = database;

        ClientListener listener = new ClientListener();
        client = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(listener)
                .addOnConnectionFailedListener(listener)
                .addApi(LocationServices.API)
                .build();

        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    /**
     * Starts google API client.
     */
    public void start() {
        client.connect();
    }

    /**
     * Stops google API client.
     */
    public void stop() {
        client.disconnect();
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
        if (isPermissionsDenied(context, ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION)) {
            Log.w(TAG, "Unable read location. Permission denied.");
            return null;
        }

        GeoCoordinates coordinates = getGoogleLocation(timeout);
        if (coordinates == null) {
            coordinates = getProviderLocation(GPS_PROVIDER, timeout);
            if (coordinates == null) {
                coordinates = getProviderLocation(NETWORK_PROVIDER, timeout);
                if (coordinates == null) {
                    coordinates = getLastProviderLocation(PASSIVE_PROVIDER);
                }
            }
        }

        if (coordinates != null) {
            database.saveLastLocation(coordinates);
        } else {
            coordinates = database.getLastLocation();
            if (coordinates != null) {
                Log.d(TAG, "Using local database location");
            } else {
                Log.w(TAG, "Unable obtain location");
            }
        }

        return coordinates;
    }

    @Nullable
    private GeoCoordinates getGoogleLocation(long timeout) {
        if (client.isConnected() && (locationManager.isProviderEnabled(GPS_PROVIDER)
                || locationManager.isProviderEnabled(NETWORK_PROVIDER))) {
            GeoCoordinates coordinates = requestGoogleLocation(timeout);
            if (coordinates != null) {
                Log.d(TAG, "Using Google API location");
                return coordinates;
            }
        }
        return null;
    }

    @Nullable
    private GeoCoordinates getProviderLocation(String provider, long timeout) {
        if (locationManager.isProviderEnabled(provider)) {
            GeoCoordinates coordinates = requestProviderLocation(provider, timeout);
            if (coordinates != null) {
                Log.d(TAG, "Using " + provider + " location");
                return coordinates;
            }
        }
        return null;
    }

    @SuppressWarnings("ResourceType")
    @Nullable
    private GeoCoordinates getLastGoogleLocation() {
        if (client.isConnected()) {
            Log.d(TAG, "Using last Google API location");
            Location location = LocationServices.FusedLocationApi.getLastLocation(client);
            if (location != null) {
                return new GeoCoordinates(location);
            }
        }
        return null;
    }

    @SuppressWarnings("ResourceType")
    @Nullable
    private GeoCoordinates getLastProviderLocation(String provider) {
        if (locationManager.isProviderEnabled(provider)) {
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                Log.d(TAG, "Using last " + provider + " location");
                return new GeoCoordinates(location);
            }
        }
        return null;
    }

    /**
     * Requests Google API for location updates and waits until first update.
     * If timeout expired returns null.
     * Caution: this method blocks caller thread for "timeout" time.
     *
     * @param timeout max await time in seconds
     * @return location or null if timeout expired
     */
    @SuppressWarnings("ResourceType")
    @Nullable
    private GeoCoordinates requestGoogleLocation(long timeout) {
        Log.d(TAG, "Requesting location from Google API");
        final AtomicReference<GeoCoordinates> result = new AtomicReference<>();
        final CountDownLatch completeSignal = new CountDownLatch(1);

        com.google.android.gms.location.LocationListener listener = new com.google.android.gms.location.LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                Log.d(TAG, "Received location: " + location);
                result.set(new GeoCoordinates(location));
                completeSignal.countDown();
            }
        };

        LocationRequest request = new LocationRequest();
        request.setNumUpdates(1);
        request.setFastestInterval(5000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        HandlerThread thread = new HandlerThread("location_request");
        thread.start();

        FusedLocationProviderApi api = LocationServices.FusedLocationApi;
        api.requestLocationUpdates(client, request, listener, thread.getLooper());

        try {
            if (!completeSignal.await(timeout, TimeUnit.MILLISECONDS)) {
                Log.w(TAG, "Location request timeout expired");
            }
        } catch (InterruptedException x) {
            Log.w(TAG, "Location request interrupted", x);
        } finally {
            api.removeLocationUpdates(client, listener);
            thread.quit();
        }

        return result.get();
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
    @Nullable
    private GeoCoordinates requestProviderLocation(String provider, long timeout) {
        Log.d(TAG, "Requesting location from: " + provider);

        final AtomicReference<GeoCoordinates> result = new AtomicReference<>();
        final CountDownLatch completeSignal = new CountDownLatch(1);

        LocationListener listener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                Log.d(TAG, "Received location: " + location);
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
                Log.w(TAG, "Location request timeout expired");
            }
        } catch (InterruptedException x) {
            Log.w(TAG, "Location request interrupted", x);
        } finally {
            locationManager.removeUpdates(listener);
            thread.quit();
        }

        return result.get();
    }

    private class ClientListener implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

        @Override
        public void onConnected(Bundle bundle) {
            Log.i(TAG, "Connected");
        }

        @Override
        public void onConnectionSuspended(int i) {
            Log.i(TAG, "Connection suspended");
        }

        @Override
        public void onConnectionFailed(@NonNull ConnectionResult result) {
            Log.i(TAG, "Connection failed: " + result);
        }
    }

}
