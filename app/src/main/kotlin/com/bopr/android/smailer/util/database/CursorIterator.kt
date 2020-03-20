package com.bopr.android.smailer.util.database

import android.database.Cursor
import java.io.Closeable

open class CursorIterator<T>(private val cursor: Cursor, private val get: (Cursor) -> T) : Iterator<T>, Closeable {

    protected var current: T? = null

    init {
        if (!cursor.moveToFirst()) {
            cursor.close()
        }
    }

    override fun hasNext(): Boolean {
        return !cursor.isAfterLast
    }

    override fun next(): T {
        current = get(cursor)
        if (!cursor.moveToNext()) {
            cursor.close()
        }
        return current!!
    }

    override fun close() {
        cursor.close()
    }
}