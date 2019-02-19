package com.bopr.android.smailer.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

import com.bopr.android.smailer.R;
import com.bopr.android.smailer.util.draw.WavyUnderlineSpan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.os.Build.MANUFACTURER;
import static android.os.Build.MODEL;

/**
 * Utilities dependent of android app context .
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class AndroidUtil {

    private AndroidUtil() {
    }

    /**
     * Returns text underlined with wavy red line.
     */
    public static Spannable underwivedText(Context context, String value) {
        Spannable spannable = new SpannableString(value);
        WavyUnderlineSpan span = new WavyUnderlineSpan(context);
        spannable.setSpan(span, 0, spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    /**
     * Returns text of accent color.
     */
    public static Spannable accentedText(Context context, String value) {
        Spannable spannable = new SpannableString(value);
        ForegroundColorSpan span = new ForegroundColorSpan(ContextCompat.getColor(context, R.color.colorAccent));
        spannable.setSpan(span, 0, spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    /**
     * Convenient method to avoid of mess in versions of AlertDialog.
     */
    @NonNull
    public static AlertDialog.Builder dialogBuilder(Context context) {
        return new AlertDialog.Builder(context);
    }

    /**
     * Returns true if device is connected ty internet.
     */
    public static boolean hasInternetConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo info = cm.getActiveNetworkInfo();
            if (info != null) {
                return info.isConnected();
            }
        }
        return false;
    }

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
    public static String getDeviceName() {
        return Util.capitalize(MANUFACTURER) + " " + MODEL;
    }

    /*
     */
/**
 * Helper method to determine if the device has an extra-large screen. For
 * example, 10" tablets are extra-large.
 *//*

    public static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }
*/

/*
    public static String getAndroidId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }
*/

/*
    public static String getDeviceUuid(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        String deviceId = tm.getDeviceId();
        String serialNumber = tm.getSimSerialNumber();
        String androidId = getAndroidId(context);

        UUID uuid = new UUID(androidId.hashCode(), (long) deviceId.hashCode() << 32 | serialNumber.hashCode());
        return uuid.toString();
    }
*/

}
