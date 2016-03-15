package com.bopr.android.smailer.settings;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import com.bopr.android.smailer.util.StringUtil;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static android.content.Context.MODE_PRIVATE;
import static android.os.Build.MANUFACTURER;
import static android.os.Build.MODEL;

/**
 * Settings.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class Settings {

    public static final String PREFERENCES_STORAGE_NAME = "com.bopr.android.smailer_preferences";
    public static final String KEY_PREF_SERVICE_ENABLED = "service_enabled";
    public static final String KEY_PREF_SENDER_ACCOUNT = "sender_account";
    public static final String KEY_PREF_SENDER_PASSWORD = "sender_password";
    public static final String KEY_PREF_EMAIL_HOST = "sender_host";
    public static final String KEY_PREF_EMAIL_PORT = "sender_port";
    public static final String KEY_PREF_RECIPIENT_EMAIL_ADDRESS = "recipient_email_address";
    public static final String KEY_PREF_OUTGOING_SERVER = "outgoing_server";
    public static final String KEY_PREF_EMAIL_CONTENT = "email_content";
    public static final String KEY_PREF_EMAIL_SOURCE = "email_source";
    public static final String VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME = "time";
    public static final String VAL_PREF_EMAIL_CONTENT_DEVICE_NAME = "device_name";
    public static final String VAL_PREF_EMAIL_CONTENT_LOCATION = "location";
    public static final String VAL_PREF_EMAIL_CONTENT_CALLER = "contact_name";
    public static final String VAL_PREF_SOURCE_IN_SMS = "in_sms";
    public static final String VAL_PREF_SOURCE_IN_CALLS = "in_calls";
    public static final String VAL_PREF_SOURCE_OUT_CALLS = "out_calls";
    public static final String VAL_PREF_SOURCE_MISSED_CALLS = "missed_calls";

    public static final String DEFAULT_HOST = "smtp.gmail.com";
    public static final String DEFAULT_PORT = "465";
    public static final Set<String> DEFAULT_CONTENT = new HashSet<>(Arrays.asList(
            VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME,
            VAL_PREF_EMAIL_CONTENT_DEVICE_NAME,
            VAL_PREF_EMAIL_CONTENT_LOCATION,
            VAL_PREF_EMAIL_CONTENT_CALLER));
    public static final Set<String> DEFAULT_SOURCES = new HashSet<>(Arrays.asList(
            VAL_PREF_SOURCE_IN_SMS,
            VAL_PREF_SOURCE_MISSED_CALLS));

    public static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREFERENCES_STORAGE_NAME, MODE_PRIVATE);
    }

    public static String getDeviceName() {
        return StringUtil.capitalize(MANUFACTURER) + " " + MODEL;
    }

    public static String getReleaseVersion(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException x) {
            throw new Error(x);
        }
    }

/*
    public String getLocaleName(Context context){
        return context.getResources().getConfiguration().locale.getDisplayName();
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
