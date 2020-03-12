package com.bopr.android.smailer.util

import android.database.Cursor

/**
 * Convenience [Cursor] wrapper.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
abstract class RowSet<R>(private val cursor: Cursor) {

    /**
     * Transforms column values into object.
     */
    protected abstract fun get(cursor: Cursor): R

    fun first(): R? {
        return cursor.useFirst { get(it) }
    }

    fun count(): Int {
        return cursor.use { it.count }
    }

    fun list(): List<R> {
        return map { it }
    }

    fun <T> map(transform: (R) -> T): List<T> {
        return cursor.useToList { transform(get(it)) }
    }

}