package com.bopr.android.smailer.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bopr.android.smailer.GeoCoordinates;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

@SuppressWarnings("WeakerAccess")
public abstract class TextUtil {

    private static final String QUOTED_TEXT_REGEX = "\"([^\"]*)\"";
    private static final String REGEX_ = "REGEX:";
    public static final Pattern QUOTED_TEXT_PATTERN = Pattern.compile(QUOTED_TEXT_REGEX);

    private TextUtil() {
    }

    /**
     * Returns true if string is not empty or null.
     */
    public static boolean isNotEmpty(@Nullable CharSequence s) {
        return !isNullOrEmpty(s);
    }

    /**
     * Returns true if string is empty or null.
     */
    public static boolean isNullOrEmpty(@Nullable CharSequence s) {
        return s == null || s.length() == 0;
    }

    /**
     * Returns true if string filled with spaces or empty or null.
     */
    public static boolean isNullOrBlank(@Nullable String s) {
        return isNullOrEmpty(s) || s.trim().length() == 0;
    }

    @NonNull
    public static String formatCoordinates(@NonNull GeoCoordinates location, String degreeSymbol,
                                           String minuteSymbol, String secondSymbol,
                                           String northSymbol, String southSymbol,
                                           String westSymbol, String eastSymbol) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        return decimalToDMS(latitude, degreeSymbol, minuteSymbol, secondSymbol)
                + (latitude > 0 ? northSymbol : southSymbol)
                + ", " +
                decimalToDMS(longitude, degreeSymbol, minuteSymbol, secondSymbol)
                + (longitude > 0 ? westSymbol : eastSymbol);
    }

    @NonNull
    public static String formatCoordinates(@NonNull GeoCoordinates location) {
        return formatCoordinates(location, "°", "\'", "\"", "N", "S", "W", "E");
    }

    @NonNull
    public static String decimalToDMS(double coordinate, String degreeSymbol, String minuteSymbol,
                                      String secondSymbol) {
        double crd = coordinate;
        double mod = crd % 1;
        int degrees = Math.abs((int) crd);

        crd = mod * 60;
        mod = crd % 1;
        int minutes = Math.abs((int) crd);

        crd = mod * 60;
        int seconds = Math.abs((int) crd);

        return degrees + degreeSymbol + minutes + minuteSymbol + seconds + secondSymbol;
    }

    @Nullable
    public static String capitalize(@Nullable String text) {
        if (isNullOrEmpty(text)) {
            return text;
        }
        return text.substring(0, 1).toUpperCase(Locale.getDefault()) + text.substring(1);
    }

    @NonNull
    public static String formatDuration(long duration) {
        long seconds = duration / 1000;
        return String.format(Locale.US, "%d:%02d:%02d", seconds / 3600, (seconds % 3600) / 60, seconds % 60);
    }

    @SafeVarargs
    @NonNull
    public static <T> String join(@NonNull String divider, T... values) {
        return join(divider, asList(values));
    }

    @NonNull
    public static String join(@NonNull String divider, @NonNull Collection values) {
        StringBuilder builder = new StringBuilder();
        for (Iterator iterator = values.iterator(); iterator.hasNext(); ) {
            builder.append(iterator.next());
            if (iterator.hasNext()) {
                builder.append(divider);
            }
        }
        return builder.toString();
    }

    @NonNull
    public static List<String> split(@Nullable String value, @NonNull String divider, boolean trim) {
        if (!isNullOrEmpty(value)) {
            String s = value;
            if (trim) {
                s = value.replaceAll(" ", "");
            }
            if (!isNullOrEmpty(s)) {
                return asList(s.split(divider));
            }
        }
        return emptyList();
    }

    @NonNull
    public static String commaJoin(@NonNull Collection values) {
        return join(",", values);
    }

    @NonNull
    public static List<String> commaSplit(@NonNull String s) {
        return split(s, ",", true);
    }

    @NonNull
    public static String escapeRegex(@NonNull String s) {
        return REGEX_ + s;
    }

    @Nullable
    public static String unescapeRegex(@Nullable String s) {
        if (s != null) {
            int ix = s.indexOf(REGEX_);
            if (ix != -1) {
                return s.substring(ix + REGEX_.length());
            }
        }
        return null;
    }

    public static boolean isQuoted(@NonNull String s) {
        return QUOTED_TEXT_PATTERN.matcher(s).matches();
    }

}
