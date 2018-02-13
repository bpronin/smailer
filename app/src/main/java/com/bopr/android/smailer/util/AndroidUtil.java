package com.bopr.android.smailer.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import com.bopr.android.smailer.R;
import com.bopr.android.smailer.util.draw.WavyUnderlineSpan;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
     * If {@code valid} parameter is false returns text underlined with wavy red line.
     */
    public static Spannable validatedUnderlinedText(Context context, String value, boolean valid) {
        Spannable result = new SpannableString(value);
        if (!valid) {
            WavyUnderlineSpan span = new WavyUnderlineSpan(context);
            result.setSpan(span, 0, result.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return result;
    }

    /**
     * If {@code valid} parameter is false returns red text.
     */
    public static Spannable validatedColoredText(Context context, String value, boolean valid) {
        Spannable result = new SpannableString(value);
        if (!valid) {
            ForegroundColorSpan span = new ForegroundColorSpan(ContextCompat.getColor(context, R.color.colorAccent2));
            result.setSpan(span, 0, result.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return result;
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

    public static void putPreferencesOptional(SharedPreferences preferences,
                                              Map<String, Object> data) {
        SharedPreferences.Editor editor = preferences.edit();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String key = entry.getKey();
            if (!preferences.contains(key)) {
                Object value = entry.getValue();
                Class<?> c = value.getClass();
                if (Boolean.class.isAssignableFrom(c)) {
                    editor.putBoolean(key, (Boolean) value);
                } else if (Float.class.isAssignableFrom(c) || Double.class.isAssignableFrom(c) || Short.class.isAssignableFrom(c)) {
                    editor.putFloat(key, ((Number) value).floatValue());
                } else if (Long.class.isAssignableFrom(c)) {
                    editor.putLong(key, (long) value);
                } else if (Integer.class.isAssignableFrom(c) || Byte.class.isAssignableFrom(c)) {
                    editor.putInt(key, ((Number) value).intValue());
                } else if (Set.class.isAssignableFrom(c)) {
                    Set<String> set = new HashSet<>();
                    for (Object v : (Set) value) {
                        set.add(String.valueOf(v));
                    }
                    editor.putStringSet(key, set);
                } else {
                    editor.putString(key, String.valueOf(value));
                }
            }
        }
        editor.apply();
    }
}
