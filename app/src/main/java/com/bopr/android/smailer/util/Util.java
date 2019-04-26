package com.bopr.android.smailer.util;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bopr.android.smailer.GeoCoordinates;

import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Miscellaneous utilities.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
@SuppressWarnings("WeakerAccess")
public class Util {

    public static final String DEFAULT = "default";
    public static final String QUOTED_TEXT_REGEX = "\"(.*?)\"";
    public static final Pattern QUOTED_TEXT_PATTERN = Pattern.compile(QUOTED_TEXT_REGEX);

    private Util() {
    }

    public static void registerUncaughtExceptionHandler() {
        final Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {
                try {
                    LoggerFactory.getLogger("application").error("Application crashed", throwable);
                } catch (Throwable x) {
                    Log.e("main", "Failed to handle uncaught exception");
                }
                defaultHandler.uncaughtException(thread, throwable);
            }
        });
    }

    public static String formatLocation(@NonNull GeoCoordinates location, String degreeSymbol,
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

    public static String formatLocation(@NonNull GeoCoordinates location) {
        return formatLocation(location, "°", "\'", "\"", "N", "S", "W", "E");
    }

    public static String decimalToDMS(double coordinate, String degreeSymbol, String minuteSymbol,
                                      String secondSymbol) {
        double mod = coordinate % 1;
        int intPart = (int) coordinate;
        int degrees = Math.abs(intPart);

        coordinate = mod * 60;
        mod = coordinate % 1;
        intPart = (int) coordinate;
        int minutes = Math.abs(intPart);

        coordinate = mod * 60;
        intPart = (int) coordinate;
        int seconds = Math.abs(intPart);

        return degrees + degreeSymbol + minutes + minuteSymbol + seconds + secondSymbol;
    }

    public static String capitalize(String text) {
        if (Util.isEmpty(text)) {
            return text;
        }
        return text.substring(0, 1).toUpperCase(Locale.getDefault()) + text.substring(1);
    }

    public static String formatDuration(long duration) {
        long seconds = duration / 1000;
        return String.format(Locale.US, "%d:%02d:%02d", seconds / 3600, (seconds % 3600) / 60, seconds % 60);
    }

    public static boolean isEmpty(Set set) {
        return set == null || set.isEmpty();
    }

    /**
     * Returns true if string is empty or null.
     */
    public static boolean isEmpty(CharSequence s) {
        return s == null || s.length() == 0;
    }

    public static boolean isTrimEmpty(String s) {
        return isEmpty(s) || isEmpty(s.trim());
    }

    public static boolean allIsEmpty(String... ss) {
        for (String s : ss) {
            if (!isEmpty(s)) {
                return false;
            }
        }
        return true;
    }

    public static boolean anyIsEmpty(String... ss) {
        for (String s : ss) {
            if (isEmpty(s)) {
                return true;
            }
        }
        return false;
    }

    @SafeVarargs
    public static <T> String join(String divider, T... values) {
        return join(divider, Arrays.asList(values));
    }

    public static String join(String divider, Collection values) {
        StringBuilder builder = new StringBuilder();
        for (Iterator iterator = values.iterator(); iterator.hasNext(); ) {
            builder.append(iterator.next());
            if (iterator.hasNext()) {
                builder.append(divider);
            }
        }
        return builder.toString();
    }

    public static List<String> split(String value, String divider, boolean trim) {
        if (!isEmpty(value)) {
            String s = value;
            if (trim) {
                s = value.replaceAll(" ", "");
            }
            if (!isEmpty(s)) {
                return Arrays.asList(s.split(divider));
            }
        }
        return Collections.emptyList();
    }

    public static String commaJoin(Collection values) {
        return join(",", values);
    }

    public static List<String> commaSplit(String s) {
        return split(s, ",", true);
    }

    @SafeVarargs
    public static <T> Set<T> asSet(T... values) {
        return Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(values)));
    }

    public static <T> Set<T> toSet(final Collection<T> collection) {
        return new LinkedHashSet<>(collection);
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
        if (!isEmpty(s)) {
            if (s.equals(DEFAULT)) {
                return Locale.getDefault();
            } else {
                String[] ss = s.split("_");
                if (ss.length == 2) {
                    return new Locale(ss[0], ss[1]);
                }
            }
        }
        return null;
    }

    public static String[] toArray(Collection<String> collection) {
        return collection.toArray(new String[0]);
    }

    public static boolean safeEquals(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }

    public static <T> T requireNonNull(T obj) {
        if (obj == null)
            throw new NullPointerException();
        return obj;
    }

    @NonNull
    public static String quoteRegex(String s) {
        return "REGEX:" + s;
    }

    @Nullable
    public static String unquoteRegex(String s) {
        if (!isEmpty(s)) {
            String[] ss = s.split("REGEX:");
            if (ss.length > 1) {
                return ss[1];
            }
        }
        return null;
    }

    @Nullable
    public static String extractQuoted(String text) {
        Matcher matcher = QUOTED_TEXT_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    public static boolean isQuoted(String s) {
        return QUOTED_TEXT_PATTERN.matcher(s).matches();
    }

    @Nullable
    public static String readStream(InputStream stream) {
        Scanner s = new Scanner(stream).useDelimiter("\\A");
        return s.hasNext() ? s.next() : null;
    }
}
