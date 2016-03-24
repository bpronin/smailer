package com.bopr.android.smailer.util;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;

import com.bopr.android.smailer.util.draw.WavyUnderlineSpan;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Utility class to handle strings.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class Util {

    public static final String DEFAULT = "default";

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

    public static boolean isTrimEmpty(String s) {
        return isEmpty(s) || isEmpty(s.trim());
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

    public static String stringOf(String divider, Collection values) {
        StringBuilder builder = new StringBuilder();
        for (Iterator iterator = values.iterator(); iterator.hasNext(); ) {
            builder.append(iterator.next());
            if (iterator.hasNext()) {
                builder.append(divider);
            }
        }
        return builder.toString();
    }

    public static String stringOf(String divider, Object... values) {
        return stringOf(divider, Arrays.asList(values));
    }

    public static List<String> listOf(String value, String divider, boolean trim) {
        String s = value;
        if (trim) {
            s = value.replaceAll(" ", "");
        }
        return Arrays.asList(s.split(divider));
    }

    @SafeVarargs
    public static <T> Set<T> asSet(T... values) {
        return Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(values)));
    }

    public static String localeToString(Locale locale) {
        if (locale == null) {
            return null;
        } else if (locale == Locale.getDefault()) {
            return DEFAULT;
        } else {
            return locale.getLanguage() + "_" + locale.getCountry();
        }
    }

    public static Locale stringToLocale(String s) {
        if (isEmpty(s)) {
            return null;
        } else if (s.equals(DEFAULT)) {
            return Locale.getDefault();
        } else {
            String[] ss = s.split("_");
            return new Locale(ss[0], ss[1]);
        }
    }

    public static Spannable validatedText(Context context, String value, boolean valid) {
        Spannable result = new SpannableString(value);
        if (!valid) {
            WavyUnderlineSpan span = new WavyUnderlineSpan(context);
            result.setSpan(span, 0, result.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return result;
    }
}
