package com.bopr.android.smailer.util;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;
import static android.location.LocationManager.PASSIVE_PROVIDER;

/**
 * Provides device location.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class LocationProvider {

    private static final String TAG = "bopr.LocationProvider";

    private GoogleApiClient client;
    private Context context;

    public LocationProvider(Context context) {
        this.context = context;
        ClientListener listener = new ClientListener();
        client = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(listener)
                .addOnConnectionFailedListener(listener)
                .addApi(LocationServices.API)
                .build();
    }

    public void start() {
        client.connect();
    }

    public void stop() {
        client.disconnect();
    }

    @SuppressWarnings("ResourceType")
    public Location getLocation() {
        Location location = null;
        if (!PermissionUtil.isLocationPermissionDenied(context)) {
            if (client.isConnected()) {
                location = LocationServices.FusedLocationApi.getLastLocation(client);
            }

            if (location == null) {
                LocationManager lm = (LocationManager) client.getContext().getSystemService(Context.LOCATION_SERVICE);

                if (lm.getProvider(GPS_PROVIDER) != null) {
                    location = lm.getLastKnownLocation(GPS_PROVIDER);
                } else {
                    Log.d(TAG, "Using GPS_PROVIDER location");
                }

                if (location == null && lm.getProvider(NETWORK_PROVIDER) != null) {
                    location = lm.getLastKnownLocation(NETWORK_PROVIDER);
                } else {
                    Log.d(TAG, "Using NETWORK_PROVIDER location");
                }

                if (location == null && lm.getProvider(PASSIVE_PROVIDER) != null) {
                    location = lm.getLastKnownLocation(PASSIVE_PROVIDER);
                } else {
                    Log.d(TAG, "Using PASSIVE_PROVIDER location");
                }

                if (location == null) {
                    Log.d(TAG, "Unable to retrieve location");
                }
            } else {
                Log.d(TAG, "Using Google API location");
            }
        }

        return location;
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
