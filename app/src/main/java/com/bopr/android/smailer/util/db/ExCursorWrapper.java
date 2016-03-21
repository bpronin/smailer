package com.bopr.android.smailer.util.db;

import android.database.Cursor;
import android.database.CursorWrapper;

import java.util.Date;

/**
 * Class ExCursorWrapper.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class ExCursorWrapper extends CursorWrapper {

    /**
     * Creates a cursor wrapper.
     *
     * @param cursor The underlying cursor to wrap.
     */
    public ExCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public String getString(String columnName) {
        return getString(getColumnIndex(columnName));
    }

    public boolean getBoolean(String columnName) {
        return getInt(columnName) != 0;
    }

    public int getInt(String columnName) {
        return getInt(getColumnIndex(columnName));
    }

    public long getLong(String columnName) {
        return getLong(getColumnIndex(columnName));
    }

    public double getDouble(String columnName) {
        return getDouble(getColumnIndex(columnName));
    }

    public Date getDate(String columnName) {
        return new Date(getLong(getColumnIndex(columnName)));
    }

    public <T> T getAndClose(String columnName, ValueReader<T> reader) {
        try {
            moveToFirst();
            if (!isBeforeFirst() && !isAfterLast()) {
                return reader.read(columnName, this);
            }
            return null;
        } finally {
            close();
        }
    }

    public String getStringAndClose(String columnName) {
        return getAndClose(columnName, new ValueReader<String>() {

            @Override
            public String read(String columnName, ExCursorWrapper cursor) {
                return getString(columnName);
            }
        });
    }

    private interface ValueReader<T> {
        T read(String columnName, ExCursorWrapper cursor);
    }

}
