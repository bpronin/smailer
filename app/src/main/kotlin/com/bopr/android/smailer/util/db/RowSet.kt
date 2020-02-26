package com.bopr.android.smailer.util.db

import android.database.Cursor

/**
 * [Cursor] wrapper.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
abstract class RowSet<R>(private val cursor: Cursor) {

    protected abstract fun get(): R

    fun first(): R? {
        return cursor.use { cursor ->
            cursor.moveToFirst()
            if (!cursor.isAfterLast) get() else null
        }
    }

    fun count(): Int {
        return cursor.use { it.count }
    }

    fun list(): List<R> {
        return map { it }
    }

    fun <T> map(transform: (R) -> T): List<T> {
        val list = mutableListOf<T>()
        this.forEach { list.add(transform(it)) }
        return list
    }

    private fun forEach(action: (R) -> Unit) {
        cursor.use { cursor ->
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                action(get())
                cursor.moveToNext()
            }
        }
    }

    protected fun isNull(columnName: String): Boolean {
        return cursor.isNull(cursor.getColumnIndex(columnName))
    }

    protected fun getString(columnName: String): String? {
        return cursor.getString(cursor.getColumnIndex(columnName))
    }

    protected fun getBoolean(columnName: String): Boolean? {
        return getInt(columnName)?.let { it != 0 }
    }

    protected fun getInt(columnName: String): Int? {
        return cursor.getInt(cursor.getColumnIndex(columnName))
    }

    protected fun getLong(columnName: String): Long? {
        return cursor.getLong(cursor.getColumnIndex(columnName))
    }

    protected fun getDouble(columnName: String): Double? {
        return cursor.getDouble(cursor.getColumnIndex(columnName))
    }

    companion object {

        fun forLong(cursor: Cursor): Long? {
            return object : RowSet<Long>(cursor) {

                override fun get(): Long {
                    return cursor.getLong(0)
                }

            }.first()
        }
    }

}