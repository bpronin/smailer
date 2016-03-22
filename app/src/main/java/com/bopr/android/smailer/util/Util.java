package com.bopr.android.smailer.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Utility class to handle strings.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class Util {

    public static String formatLocation(double latitude, double longitude, String degreeSymbol,
                                        String minuteSymbol,
                                        String secondSymbol, String northSymbol, String southSymbol,
                                        String westSymbol,
                                        String eastSymbol) {
        return decimalToDMS(latitude, degreeSymbol, minuteSymbol, secondSymbol)
                + (latitude > 0 ? northSymbol : southSymbol)
                + ", " +
                decimalToDMS(longitude, degreeSymbol, minuteSymbol, secondSymbol)
                + (longitude > 0 ? westSymbol : eastSymbol);
    }

    public static String formatLocation(double latitude, double longitude) {
        return formatLocation(latitude, longitude, "°", "\'", "\"", "N", "S", "W", "E");
    }

    public static String decimalToDMS(double coordinate, String degreeSymbol, String minuteSymbol,
                                      String secondSymbol) {
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
        if (Util.isEmpty(text)) {
            return text;
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    public static String formatDuration(long duration) {
        long seconds = duration / 1000;
        return String.format("%d:%02d:%02d", seconds / 3600, (seconds % 3600) / 60, seconds % 60);
    }

    /**
     * Returns true if string is empty or null.
     */
    public static boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }

    public static boolean isAllEmpty(String... ss) {
        for (String s : ss) {
            if (!isEmpty(s)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isAnyEmpty(String... ss) {
        for (String s : ss) {
            if (isEmpty(s)) {
                return true;
            }
        }
        return false;
    }

    public static String listOf(String divider, Object... values) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0, end = values.length - 1; i < values.length; i++) {
            builder.append(values[i]);
            if (i < end) {
                builder.append(divider);
            }
        }
        return builder.toString();
    }

    @SafeVarargs
    public static <T> Set<T> asSet(T... values) {
        return Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(values)));
    }

    public static String localeToString(Locale locale) {
        return locale.getLanguage() + "_" + locale.getCountry();
    }

    public static Locale stringToLocale(String s) {
        try {
            String[] ss = s.split("_");
            return new Locale(ss[0], ss[1]);
        } catch (Exception e) {
            return null;
        }
    }

}
