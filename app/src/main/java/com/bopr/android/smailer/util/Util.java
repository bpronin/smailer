package com.bopr.android.smailer.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.collect.ImmutableSet;

import java.io.InputStream;
import java.util.Scanner;

/**
 * Miscellaneous utilities.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public abstract class Util {

    private Util() {
    }

    @SuppressWarnings("EqualsReplaceableByObjectsCall")
    public static boolean safeEquals(@Nullable Object a, @Nullable Object b) {
        return (a == b) || (a != null && a.equals(b));
    }

    @Nullable
    public static String readStream(@NonNull InputStream stream) {
        Scanner s = new Scanner(stream).useDelimiter("\\A");
        return s.hasNext() ? s.next() : null;
    }

    @SafeVarargs
    public static <T> ImmutableSet<T> setOf(T... values) {
        return ImmutableSet.copyOf(values);
    }

}
