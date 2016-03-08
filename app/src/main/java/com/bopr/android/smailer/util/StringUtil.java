package com.bopr.android.smailer.util;

import android.location.Location;
import android.text.TextUtils;

/**
 * Class StringUtil.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class StringUtil {

    public static String formatLocation(Location location, String degreeSymbol,
                                        String minuteSymbol, String secondSymbol,
                                        String northSymbol, String southSymbol,
                                        String westSymbol, String eastSymbol) {
        double lt = location.getLatitude();
        double lg = location.getLongitude();

        return decimalToDMS(lt, degreeSymbol, minuteSymbol, secondSymbol)
                + (lt > 0 ? northSymbol : southSymbol)
                + ", " +
                decimalToDMS(lg, degreeSymbol, minuteSymbol, secondSymbol)
                + (lg > 0 ? westSymbol : eastSymbol);
    }

    public static String formatLocation(Location location) {
        return formatLocation(location, "°", "\'", "\"", "N", "S", "W", "E");
    }

    public static String decimalToDMS(double coordinate, String degreeSymbol,
                                      String minuteSymbol, String secondSymbol) {
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

        return degrees + degreeSymbol + minutes + minuteSymbol + seconds + secondSymbol;
    }

    public static String capitalize(String text) {
        if (TextUtils.isEmpty(text)) {
            return text;
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }


    public static String formatDuration(long duration) {
        long seconds = duration / 1000;
        return String.format("%d:%02d:%02d", seconds / 3600, (seconds % 3600) / 60, seconds % 60);
    }
}
