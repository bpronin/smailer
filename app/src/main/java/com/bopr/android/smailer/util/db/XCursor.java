package com.bopr.android.smailer.util.db;

import android.database.Cursor;
import android.database.CursorWrapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Extended {@link CursorWrapper}.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
@SuppressWarnings("WeakerAccess")
public abstract class XCursor<R> extends CursorWrapper {

    /**
     * Creates a cursor wrapper.
     *
     * @param cursor The underlying cursor to wrap.
     */
    public XCursor(Cursor cursor) {
        super(cursor);
    }

    public abstract R get();

    public R getAndClose() {
        try {
            moveToFirst();
            if (!isBeforeFirst() && !isAfterLast()) {
                return get();
            }
            return null;
        } finally {
            close();
        }
    }

    public List<R> getAll() {
        try {
            List<R> list = new ArrayList<>();
            moveToFirst();
            while (!isAfterLast()) {
                list.add(get());
                moveToNext();
            }
            return list;
        } finally {
            close();
        }
    }

    public boolean isNull(String columnName) {
        return isNull(getColumnIndex(columnName));
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

    public static XCursor<Long> forLong(Cursor cursor) {
        return new XCursor<Long>(cursor) {

            @Override
            public Long get() {
                return getLong(0);
            }
        };
    }
}
