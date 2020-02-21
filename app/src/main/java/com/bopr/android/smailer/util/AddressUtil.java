package com.bopr.android.smailer.util;

import android.util.Patterns;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collection;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static androidx.core.util.PatternsCompat.EMAIL_ADDRESS;
import static com.bopr.android.smailer.util.TextUtil.commaSplit;
import static com.bopr.android.smailer.util.TextUtil.isQuoted;

/**
 * Phone number utilities.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
@SuppressWarnings("WeakerAccess")
public abstract class AddressUtil {

    @SuppressWarnings("RegExpRedundantEscape")
    public static final Pattern PHONE_PATTERN = Pattern.compile(    // sdd = space, dot, or dash
            "(\\+[0-9]+[\\- \\.]*)?"                                // +<digits><sdd>*
                    + "(\\([0-9]+\\)[\\- \\.]*)?"                   // (<digits>)<sdd>*
                    + "([0-9][0-9\\- \\.]+[0-9])");                 // <digit><digit|sdd>+<digit>

    private AddressUtil() {
    }

    @NonNull
    public static String normalizePhone(@NonNull String phone) {
        return phone.replaceAll("[^A-Za-z0-9*.]", "").toUpperCase(Locale.ROOT);
    }

    public static int comparePhones(@NonNull String p1, @NonNull String p2) {
        return normalizePhone(p1).compareTo(normalizePhone(p2));
    }

    public static boolean samePhones(@NonNull String p1, @NonNull String p2) {
        return p1.equals(p2) || comparePhones(p1, p2) == 0;
    }

    @Nullable
    public static String findPhone(@NonNull Collection<String> list, @NonNull String phone) {
        for (String p : list) {
            if (samePhones(p, phone)) {
                return p;
            }
        }
        return null;
    }

    public static boolean containsPhone(@NonNull Collection<String> list, @NonNull String phone) {
        return findPhone(list, phone) != null;
    }

    @NonNull
    public static String phoneToRegEx(@NonNull String phone) {
        return normalizePhone(phone).replaceAll("\\*", "(.*)");
    }

    @Nullable
    public static String extractPhone(@NonNull String text) {
        Matcher matcher = PHONE_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    /**
     * Returns phone as is if it is regular or quoted otherwise
     */
    @NonNull
    public static String escapePhone(@NonNull String phone) {
        return PHONE_PATTERN.matcher(phone).matches()
                ? phone
                : ("\"" + phone + "\"");
    }

    @NonNull
    public static String normalizeEmail(@NonNull String email) {
        String localPart = email.split("@")[0];
        String part = isQuoted(localPart) ? localPart : localPart.replaceAll("\\.", "");
        return email.replaceFirst(localPart, part).toLowerCase(Locale.ROOT);
    }

    public static int compareEmails(@NonNull String e1, @NonNull String e2) {
        return normalizeEmail(e1).compareTo(normalizeEmail(e2));
    }

    public static boolean sameEmails(@NonNull String e1, @NonNull String e2) {
        return e1.equals(e2) || compareEmails(e1, e2) == 0;
    }

    @Nullable
    public static String findEmail(@NonNull Collection<String> list, @NonNull String email) {
        for (String m : list) {
            if (sameEmails(m, email)) {
                return m;
            }
        }
        return null;
    }

    public static boolean containsEmail(@NonNull Collection<String> list,@NonNull String email) {
        return findEmail(list, email) != null;
    }

    @Nullable
    public static String extractEmail(@NonNull String text) {
        Matcher matcher = EMAIL_ADDRESS.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    public static boolean isValidEmailAddress(@NonNull String text) {
        return Patterns.EMAIL_ADDRESS.matcher(text).matches();
    }

    public static boolean isValidEmailAddressList(@NonNull String text) {
        for (String s : commaSplit(text)) {
            if (!isValidEmailAddress(s)) {
                return false;
            }
        }
        return true;
    }
}
