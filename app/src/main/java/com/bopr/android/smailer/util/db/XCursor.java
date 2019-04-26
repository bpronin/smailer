package com.bopr.android.smailer.util.db;

import android.database.Cursor;
import android.database.CursorWrapper;

import androidx.core.util.Consumer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Extended {@link CursorWrapper}.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public abstract class XCursor<R> extends CursorWrapper {

    /**
     * Creates a cursor wrapper.
     *
     * @param cursor The underlying cursor to wrap.
     */
    public XCursor(Cursor cursor) {
        super(cursor);
    }

    public void forEach(Consumer<? super R> action) {
        try {
            moveToFirst();
            while (!isAfterLast()) {
                action.accept(get());
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
                return get();
            }
            return null;
        } finally {
            close();
        }
    }

    public <C extends Collection<R>> C collect(final C collection) {
        forEach(new Consumer<R>() {

            @Override
            public void accept(R row) {
                collection.add(row);
            }
        });
        return collection;
    }

    public List<R> toList() {
        return collect(new ArrayList<R>());
    }

    public abstract R get();

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
            public Long get() {
                return getLong(0);
            }
        }.findFirst();
    }

}
