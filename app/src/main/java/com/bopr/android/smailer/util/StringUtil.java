package com.bopr.android.smailer.util;

import android.location.Location;
import android.text.TextUtils;

/**
 * Class StringUtil.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class StringUtil {

    public static String formatLocation(Location location) {
        double lt = location.getLatitude();
        double lg = location.getLongitude();
        return decimalToDMS(lt) + (lt > 0 ? "N" : "S") + ", " +
                decimalToDMS(lg) + (lg > 0 ? "W" : "E");
    }

    public static String decimalToDMS(double coordinate) {
        double mod = coordinate % 1;
        int intPart = (int) coordinate;
        int degrees = intPart;

        coordinate = mod * 60;
        mod = coordinate % 1;
        intPart = (int) coordinate;
        int minutes = intPart;

        coordinate = mod * 60;
        intPart = (int) coordinate;
        int seconds = intPart;

        return degrees + "°" + minutes + "'" + seconds + "\" ";
    }

    public static String capitalize(String text) {
        if (TextUtils.isEmpty(text)) {
            return text;
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }
}
