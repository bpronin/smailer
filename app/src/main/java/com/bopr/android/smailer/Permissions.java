package com.bopr.android.smailer;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.RECEIVE_SMS;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * Operations with permissions.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class Permissions {

    private Permissions() {
    }

    public static boolean isReadContactPermissionDenied(Context context) {
        return ContextCompat.checkSelfPermission(context, READ_CONTACTS) != PERMISSION_GRANTED;
    }

    public static boolean isSmsPermissionDenied(Context context) {
        return ContextCompat.checkSelfPermission(context, RECEIVE_SMS) != PERMISSION_GRANTED;
    }

    public static boolean isLocationPermissionDenied(Context context) {
        return ContextCompat.checkSelfPermission(context, ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED;
    }

    public static void requestSmsPermission(Activity activity, int requestCode) {
        if (isSmsPermissionDenied(activity)) {
            ActivityCompat.requestPermissions(activity, new String[]{RECEIVE_SMS},
                    requestCode);
        }
    }

    public static void requestReadContactPermission(Activity activity, int requestCode) {
        if (isReadContactPermissionDenied(activity)) {
            ActivityCompat.requestPermissions(activity, new String[]{READ_CONTACTS},
                    requestCode);
        }
    }

    public static void requestLocationPermission(Activity activity, int requestCode) {
        if (isLocationPermissionDenied(activity)) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION},
                    requestCode);
        }
    }

}
