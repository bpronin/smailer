package com.bopr.android.smailer.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Scanner;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;

/**
 * Miscellaneous utilities.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public abstract class Util {

    private Util() {
    }

    public static boolean safeEquals(@Nullable Object a, @Nullable Object b) {
        return (a == b) || (a != null && a.equals(b));
    }

    public static <T> T requireNonNull(@Nullable T obj) {
        if (obj == null) {
            throw new NullPointerException();
        }
        return obj;
    }

    @Nullable
    public static String readStream(@NonNull InputStream stream) {
        Scanner s = new Scanner(stream).useDelimiter("\\A");
        return s.hasNext() ? s.next() : null;
    }

    @SafeVarargs
    public static <T> Set<T> asSet(T... values) {
        return unmodifiableSet(toSet(asList(values)));
    }

    @NonNull
    public static <T> Set<T> toSet(@NonNull Collection<T> collection) {
        return new LinkedHashSet<>(collection);
    }

    public static String[] toArray(@NonNull Collection<String> collection) {
        return collection.toArray(new String[0]);
    }

}
