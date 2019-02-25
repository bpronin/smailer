package com.bopr.android.smailer.util;

import java.util.Collection;
import java.util.Locale;

import androidx.annotation.NonNull;

import static com.bopr.android.smailer.util.Util.safeEquals;

/**
 * Phone number utilities.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
@SuppressWarnings("WeakerAccess")
public class PhoneUtil {

    private PhoneUtil() {
    }

    public static String normalizePhone(@NonNull String phone) {
//        return phone.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
        return phone.replaceAll("[^A-Za-z0-9*.]", "").toUpperCase(Locale.ROOT);
    }

    public static int comparePhones(String p1, String p2) {
        return normalizePhone(p1).compareTo(normalizePhone(p2));
    }

    public static boolean phonesEqual(String p1, String p2) {
        return safeEquals(p1, p2) || comparePhones(p1, p2) == 0;
    }

    public static String findPhone(@NonNull Collection<String> list, String phone) {
        for (String n : list) {
            if (phonesEqual(n, phone)) {
                return n;
            }
        }
        return null;
    }

    public static boolean containsPhone(Collection<String> list, String phone) {
        return findPhone(list, phone) != null;
    }

    public static String phoneToRegEx(@NonNull String phone) {
        return normalizePhone(phone).replaceAll("\\*", "(.*)");
    }

}
