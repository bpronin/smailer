package com.bopr.android.smailer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;
import static android.location.LocationManager.PASSIVE_PROVIDER;
import static android.location.LocationManager.PROVIDERS_CHANGED_ACTION;

/**
 * Class LocationReceiver.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */

@SuppressWarnings("ResourceType")
public class LocationReceiver extends BroadcastReceiver {

    private static final String TAG = "LocationReceiver";
    private static final String LOCATION_RECEIVED_ACTION = "com.bopr.android.smailer.LOCATION_RECEIVED_ACTION";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.w(TAG, "Received location provider intent: " + intent);

        if (intent.getAction().equals(PROVIDERS_CHANGED_ACTION)) {
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (lm.isProviderEnabled(GPS_PROVIDER)) {
                requestLocation(context, lm, GPS_PROVIDER);
            } else if (lm.isProviderEnabled(NETWORK_PROVIDER)) {
                requestLocation(context, lm, NETWORK_PROVIDER);
            } else if (lm.isProviderEnabled(PASSIVE_PROVIDER)) {
                requestLocation(context, lm, PASSIVE_PROVIDER);
            }
        } else if (intent.getAction().equals(LOCATION_RECEIVED_ACTION)) {
            String provider = intent.getStringExtra("provider");
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            Location location = lm.getLastKnownLocation(provider);
            new Database(context).saveLastLocation(new GeoCoordinates(location));
            Log.d(TAG, "Location: " + location);
        }
    }


    private void requestLocation(Context context, LocationManager lm, String provider) {
//        Intent intent = new Intent(LOCATION_RECEIVED_ACTION);
//        intent.putExtra("provider", provider);
//        PendingIntent locationIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
////        lm.requestLocationUpdates(provider, 0, 0, locationIntent);
//        lm.requestSingleUpdate(provider, locationIntent);
//        Log.i(TAG, "Using " + provider);
    }

}
