package com.bopr.android.smailer.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.provider.Settings;

import static android.os.Build.MANUFACTURER;
import static android.os.Build.MODEL;

/**
 * Class DeviceUtil.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class DeviceUtil {

    public static String getAndroidId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

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

    public static String getDeviceName() {
        return StringUtil.capitalize(MANUFACTURER) + " " + MODEL;
    }

    public static String getReleaseVersion(Activity context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException x) {
            throw new Error(x);
        }
    }
}
