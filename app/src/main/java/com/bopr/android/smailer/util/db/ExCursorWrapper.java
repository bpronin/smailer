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

    public <T> T getAndClose(ValueReader<T> reader) {
        try {
            moveToFirst();
            if (!isBeforeFirst() && !isAfterLast()) {
                return reader.read(this);
            }
            return null;
        } finally {
            close();
        }
    }

    public String getStringAndClose(final String columnName) {
        return getAndClose(new ValueReader<String>() {

            @Override
            public String read(ExCursorWrapper wrapper) {
                return wrapper.getString(columnName);
            }
        });
    }

    public Long getLongAndClose(final String columnName) {
        return getAndClose(new ValueReader<Long>() {

            @Override
            public Long read(ExCursorWrapper wrapper) {
                return wrapper.getLong(columnName);
            }
        });
    }

    public boolean isNull(String columnName) {
        return isNull(getColumnIndex(columnName));
    }

    public interface ValueReader<T> {

        T read(ExCursorWrapper wrapper);
    }

}
