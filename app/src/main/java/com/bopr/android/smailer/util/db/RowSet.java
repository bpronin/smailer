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
@SuppressWarnings("WeakerAccess")
public abstract class RowSet<R> {

    protected final Cursor cursor;

    /**
     * Creates a cursor wrapper.
     *
     * @param cursor The underlying cursor to wrap.
     */
    public RowSet(Cursor cursor) {
        this.cursor = cursor;
    }

    protected abstract R get();

    public void forEach(Consumer<? super R> action) {
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                action.accept(get());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }
    }

    public R findFirst() {
        try {
            cursor.moveToFirst();
            if (!cursor.isBeforeFirst() && !cursor.isAfterLast()) {
                return get();
            }
            return null;
        } finally {
            cursor.close();
        }
    }

    public long getCount() {
        try {
            return cursor.getCount();
        } finally {
            cursor.close();
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

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected boolean isNull(String columnName) {
        return cursor.isNull(cursor.getColumnIndex(columnName));
    }

    protected String getString(String columnName) {
        return cursor.getString(cursor.getColumnIndex(columnName));
    }

    protected boolean getBoolean(String columnName) {
        return getInt(columnName) != 0;
    }

    protected int getInt(String columnName) {
        return cursor.getInt(cursor.getColumnIndex(columnName));
    }

    protected long getLong(String columnName) {
        return cursor.getLong(cursor.getColumnIndex(columnName));
    }

    protected double getDouble(String columnName) {
        return cursor.getDouble(cursor.getColumnIndex(columnName));
    }

    public static Long forLong(Cursor cursor) {
        return new RowSet<Long>(cursor) {

            @Override
            protected Long get() {
                return cursor.getLong(0);
            }
        }.findFirst();
    }

}
