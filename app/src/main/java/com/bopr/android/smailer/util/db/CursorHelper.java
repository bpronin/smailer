package com.bopr.android.smailer.util.db;

import android.database.Cursor;

public class CursorHelper {

    public static void iterate(Cursor cursor, RowMapper mapper) {
        if (cursor == null) {
            return;
        }

        mapper.init(cursor);
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                if (!mapper.found()) {
                    break;
                }
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }
    }

}
