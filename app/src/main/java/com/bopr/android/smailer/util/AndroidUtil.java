package com.bopr.android.smailer.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;

import com.bopr.android.smailer.R;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.os.Build.MANUFACTURER;
import static android.os.Build.MODEL;
import static android.view.LayoutInflater.from;

/**
 * Utilities dependent of android app context .
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class AndroidUtil {

    private AndroidUtil() {
    }

//    /**
//     * Returns true if device is connected ty internet.
//     */
//    public static boolean hasInternetConnection(Context context) {
//        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//        if (cm != null) {
//            NetworkInfo info = cm.getActiveNetworkInfo();
//            if (info != null) {
//                return info.isConnected();
//            }
//        }
//        return false;
//    }

    /**
     * Checks if listed permissions denied.
     *
     * @param context     context
     * @param permissions permissions
     * @return true if any of listed permissions denied
     */
    public static boolean isPermissionsDenied(Context context, String... permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PERMISSION_GRANTED) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns denice name.
     */
    public static String deviceName() {
        return Util.capitalize(MANUFACTURER) + " " + MODEL;
    }

    public static boolean isValidEmailAddress(String text) {
        return Patterns.EMAIL_ADDRESS.matcher(text).matches();
    }

    public static boolean isValidEmailAddressList(String text) {
        for (String s : Util.commaSplit(text)) {
            if (!isValidEmailAddress(s)) {
                return false;
            }
        }
        return true;
    }

    @SuppressLint("InflateParams")
    public static View alertDialogView(View view) {
        ViewGroup container = (ViewGroup) from(view.getContext()).inflate(R.layout.alert_dialog_view_container, null);
        container.addView(view);
        return container;
    }

    //    @SuppressLint({"MissingPermission", "HardwareIds"})
//    public static String devicePhoneNumber(Context context) {
//        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//        return tm != null ? tm.getLine1Number() : null;
//    }

//    public static void startServiceCompat(Context context, Intent intent) {
//        /* this is to avoid https://stackoverflow.com/questions/46445265/android-8-0-java-lang-illegalstateexception-not-allowed-to-start-service-inten*/
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            context.startForegroundService(intent);
//        } else {
//            context.startService(intent);
//        }
//    }
}
