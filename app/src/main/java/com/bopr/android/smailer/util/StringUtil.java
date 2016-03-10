package com.bopr.android.smailer.util;

import android.text.TextUtils;

/**
 * Class StringUtil.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class StringUtil {

    public static String formatLocation(double latitude, double longitude, String degreeSymbol,
                                        String minuteSymbol, String secondSymbol,
                                        String northSymbol, String southSymbol,
                                        String westSymbol, String eastSymbol
    ) {
        return decimalToDMS(latitude, degreeSymbol, minuteSymbol, secondSymbol)
                + (latitude > 0 ? northSymbol : southSymbol)
                + ", " +
                decimalToDMS(longitude, degreeSymbol, minuteSymbol, secondSymbol)
                + (longitude > 0 ? westSymbol : eastSymbol);
    }

    public static String formatLocation(double latitude, double longitude) {
        return formatLocation(latitude, longitude, "°", "\'", "\"", "N", "S", "W", "E");
    }

    public static String decimalToDMS(double coordinate, String degreeSymbol,
                                      String minuteSymbol, String secondSymbol
    ) {
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
        if (StringUtil.isEmpty(text)) {
            return text;
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }


    public static String formatDuration(long duration) {
        long seconds = duration / 1000;
        return String.format("%d:%02d:%02d", seconds / 3600, (seconds % 3600) / 60, seconds % 60);
    }

    public static boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }
}
