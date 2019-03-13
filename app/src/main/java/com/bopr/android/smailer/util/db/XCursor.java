package com.bopr.android.smailer.util.db;

import android.database.Cursor;
import android.database.CursorWrapper;

import java.util.ArrayList;
import java.util.List;

import androidx.core.util.Consumer;

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

    public void iterate(Consumer<R> consumer) {
        try {
            moveToFirst();
            while (!isAfterLast()) {
                consumer.accept(getRow());
                moveToNext();
            }
        } finally {
            close();
        }
    }

    public R findFirst() {
        try {
            moveToFirst();
            if (!isBeforeFirst() && !isAfterLast()) {
                return getRow();
            }
            return null;
        } finally {
            close();
        }
    }

    public List<R> findAll() {
        final List<R> list = new ArrayList<>();
        iterate(new Consumer<R>() {

            @Override
            public void accept(R row) {
                list.add(row);
            }
        });
        return list;
    }

    public abstract R getRow();

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
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

    public static Long forLong(Cursor cursor) {
        return new XCursor<Long>(cursor) {

            @Override
            public Long getRow() {
                return getLong(0);
            }
        }.findFirst();
    }

}
