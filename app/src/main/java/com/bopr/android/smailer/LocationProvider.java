package com.bopr.android.smailer;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

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
@SuppressWarnings("ResourceType")
public class LocationProvider {

    private static final String TAG = "LocationProvider";

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
     * Tries to retrieve last known device location from different providers.
     *
     * @return last known device location or null when it cannot be found
     */
    @Nullable
    public GeoCoordinates getLocation() {
        if (!hasPermissions()) {
            return null;
        }

        GeoCoordinates coordinates = getGoogleLocation();
        if (coordinates == null) {
            coordinates = getProviderLocation(GPS_PROVIDER);
            if (coordinates == null) {
                coordinates = getProviderLocation(NETWORK_PROVIDER);
                if (coordinates == null) {
                    coordinates = getProviderLocation(PASSIVE_PROVIDER);
                }
            }
        }

        if (coordinates != null) {
            database.saveLastLocation(coordinates);
        } else {
            coordinates = database.getLastLocation();
            if (coordinates != null) {
                Log.d(TAG, "Using internal database location");
            } else {
                Log.w(TAG, "Unable detect location");
            }
        }

        return coordinates;
    }

    @Nullable
    private GeoCoordinates getGoogleLocation() {
        if (client.isConnected()) {
            Log.d(TAG, "Using Google API location");
            Location location = LocationServices.FusedLocationApi.getLastLocation(client);
            if (location != null) {
                return new GeoCoordinates(location);
            }
        }
        return null;
    }

    @Nullable
    private GeoCoordinates getProviderLocation(String provider) {
        if (locationManager.isProviderEnabled(provider)) {
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                Log.d(TAG, "Using " + provider + " location");
                return new GeoCoordinates(location);
            }
        }
        return null;
    }

    /**
     * Requests provider for location updates and waits until first update.
     * If timeout expired returns null.
     *
     * @param provider location provider name
     * @param timeout  max await time in seconds
     * @return location or null if timeout expired
     */
    @SuppressWarnings("ResourceType")
    public GeoCoordinates requestLocation(final String provider, long timeout) {
        if (!hasPermissions()) {
            return null;
        }

        Log.d(TAG, "Requesting location from: " + provider);

        final AtomicReference<GeoCoordinates> result = new AtomicReference<>();
        final CountDownLatch competeSignal = new CountDownLatch(1);

//        Executors.newSingleThreadExecutor().execute(new Runnable() {
//
//            @Override
//            public void run() {
//                Looper.prepare();
//
//                final LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
//                manager.requestSingleUpdate(provider, new LocationListener() {
//
//                    @Override
//                    public void onLocationChanged(Location location) {
//                        Log.d(TAG, "Received location: " + location);
//                        manager.removeUpdates(this);
//                        result.set(new GeoCoordinates(location));
//                        competeSignal.countDown();
////                        Looper.myLooper().quit();
//                    }
//
//                    @Override
//                    public void onStatusChanged(String provider, int status, Bundle extras) {
//                        /* do nothing*/
//                    }
//
//                    @Override
//                    public void onProviderEnabled(String provider) {
//                        /* do nothing*/
//                    }
//
//                    @Override
//                    public void onProviderDisabled(String provider) {
//                        /* do nothing*/
//                    }
//                }, null);
//
//                Looper.loop();
//            }
//        });

        try {
            if (!competeSignal.await(timeout, TimeUnit.SECONDS)) {
                Log.w(TAG, "Location request timeout expired");
            }
        } catch (InterruptedException x) {
            Log.w(TAG, "Location request interrupted", x);
        }

        return result.get();
    }

    private boolean hasPermissions() {
        if (PermissionsChecker.isPermissionsDenied(context, ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION)) {
            Log.w(TAG, "Unable read location. Permission denied.");
            return false;
        }
        return true;
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
