package com.bopr.android.smailer.util.db;

import android.database.Cursor;

public abstract class RowMapper {

    private Cursor cursor;

    void init(Cursor cursor) {
        this.cursor = cursor;
    }

    protected abstract boolean found();

    boolean isNull(String columnName) {
        return cursor.isNull(cursor.getColumnIndex(columnName));
    }

    String getString(String columnName) {
        return cursor.getString(cursor.getColumnIndex(columnName));
    }

    boolean getBoolean(String columnName) {
        return getInt(columnName) != 0;
    }

    int getInt(String columnName) {
        return cursor.getInt(cursor.getColumnIndex(columnName));
    }

    long getLong(String columnName) {
        return cursor.getLong(cursor.getColumnIndex(columnName));
    }

    double getDouble(String columnName) {
        return cursor.getDouble(cursor.getColumnIndex(columnName));
    }
}
