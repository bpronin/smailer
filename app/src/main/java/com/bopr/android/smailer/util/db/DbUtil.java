package com.bopr.android.smailer.util.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static com.bopr.android.smailer.util.Util.requireNonNull;

/**
 * Database utilities.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
@SuppressWarnings("WeakerAccess")
public class DbUtil {

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

    public static String backupTable(SQLiteDatabase db, String tableName) {
        String newTableName = tableName + "_back";
        db.execSQL("ALTER TABLE " + tableName + " RENAME TO " + newTableName);
        return newTableName;
    }

    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    public static void copyTable(SQLiteDatabase db, String tableFrom, String tableTo) {
        Cursor cursor = db.query(tableFrom, null, null, null, null, null, null);
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                ContentValues values = new ContentValues();
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    // TODO: 07.05.2019 check column exists 
//                    if () {
                        values.put(cursor.getColumnName(i), cursor.getString(i));
//                    }
                }
                db.insert(tableTo, null, values);

                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }
    }

    public static void replaceTable(SQLiteDatabase db, String table, String createSql) {
        db.beginTransaction();
        try {
            String old = table + "_old";
            db.execSQL("ALTER TABLE " + table + " RENAME TO " + old);
            db.execSQL(createSql);
            copyTable(db, old, table);
            db.execSQL("DROP TABLE " + old);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

}
