package com.bopr.android.smailer.util.db;

import android.database.Cursor;

public abstract class FieldDataConverter {

    public String convert(String column, Cursor cursor) {
        return cursor.getString(cursor.getColumnIndex(column));
    }

}
