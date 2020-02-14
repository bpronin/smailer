package com.bopr.android.smailer.util.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Set;

import static com.bopr.android.smailer.util.Util.asSet;

/**
 * Database utilities.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
@SuppressWarnings("WeakerAccess")
public abstract class DbUtil {

    private DbUtil() {
    }

    public static String getString(@NonNull Cursor cursor, @NonNull String columnName) {
        return cursor.getString(cursor.getColumnIndex(columnName));
    }

    @Nullable
    public static Boolean getBoolean(@NonNull Cursor cursor, @NonNull String columnName) {
        Integer value = getInt(cursor, columnName);
        return value != null ? value != 0 : null;
    }

    @Nullable
    public static Integer getInt(@NonNull Cursor cursor, @NonNull String columnName) {
        int index = cursor.getColumnIndex(columnName);
        return cursor.isNull(index) ? null : cursor.getInt(index);
    }

    public static void copyTable(SQLiteDatabase db, String tableFrom, String tableTo,
                                 FieldDataConverter converter) {
        Cursor dest = db.query(tableTo, null, null, null, null, null, null);
        Cursor src = db.query(tableFrom, null, null, null, null, null, null);
        Set<String> srcColumns = asSet(src.getColumnNames());
        try {
            src.moveToFirst();
            while (!src.isAfterLast()) {
                ContentValues values = new ContentValues();
                for (String column : dest.getColumnNames()) {
                    if (srcColumns.contains(column)) {
                        values.put(column, converter.convert(column, src));
                    }
                }

                db.insert(tableTo, null, values);

                src.moveToNext();
            }
        } finally {
            src.close();
            dest.close();
        }
    }

    public static void replaceTable(SQLiteDatabase db, String table, String createSql,
                                    FieldDataConverter converter) {
        db.beginTransaction();
        try {
            String old = table + "_old";
            db.execSQL("DROP TABLE IF EXISTS " + old);
            db.execSQL("ALTER TABLE " + table + " RENAME TO " + old);
            db.execSQL(createSql);
            copyTable(db, old, table, converter);
            db.execSQL("DROP TABLE " + old);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

}
