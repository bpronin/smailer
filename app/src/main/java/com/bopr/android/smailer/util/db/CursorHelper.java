package com.bopr.android.smailer.util.db;

import android.database.Cursor;
import android.database.CursorWrapper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static com.bopr.android.smailer.util.Util.requireNonNull;

/**
 * Extended {@link CursorWrapper}.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
@SuppressWarnings("WeakerAccess")
public class CursorHelper {

    public static String getString(Cursor cursor, String columnName) {
        return cursor.getString(cursor.getColumnIndex(columnName));
    }

    @Nullable
    public static Boolean getBoolean(Cursor cursor, String columnName) {
        Integer value = getInt(cursor, columnName);
        return value != null ? value != 0 : null;
    }

    @Nullable
    public static Integer getInt(Cursor cursor, String columnName) {
        int index = cursor.getColumnIndex(columnName);
        return cursor.isNull(index) ? null : cursor.getInt(index);
    }

    @Nullable
    public static Long getLong(Cursor cursor, String columnName) {
        int index = cursor.getColumnIndex(columnName);
        return cursor.isNull(index) ? null : cursor.getLong(index);
    }

    @Nullable
    public static Double getDouble(Cursor cursor, String columnName) {
        int index = cursor.getColumnIndex(columnName);
        return cursor.isNull(index) ? null : cursor.getDouble(index);
    }

    @NonNull
    public static String requireString(Cursor cursor, String columnName) {
        return requireNonNull(getString(cursor, columnName));
    }

    public static boolean requireBoolean(Cursor cursor, String columnName) {
        return requireInt(cursor, columnName) != 0;
    }

    public static int requireInt(Cursor cursor, String columnName) {
        return requireNonNull(getInt(cursor, columnName));
    }

    public static long requireLong(Cursor cursor, String columnName) {
        return requireNonNull(getLong(cursor, columnName));
    }

    public static double requireDouble(Cursor cursor, String columnName) {
        return requireNonNull(getDouble(cursor, columnName));
    }

}
