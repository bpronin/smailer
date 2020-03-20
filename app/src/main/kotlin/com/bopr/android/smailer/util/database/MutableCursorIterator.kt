package com.bopr.android.smailer.util.database

import android.database.Cursor

class MutableCursorIterator<T>(cursor: Cursor, get: (Cursor) -> T, private val remove: (T) -> Boolean)
    : CursorIterator<T>(cursor, get), MutableIterator<T> {

    override fun remove() {
        remove(current!!)
    }
}