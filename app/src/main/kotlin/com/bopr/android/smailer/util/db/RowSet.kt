package com.bopr.android.smailer.util.db

import android.database.Cursor
import com.bopr.android.smailer.util.db.DbUtil.getDouble
import com.bopr.android.smailer.util.db.DbUtil.getInt
import com.bopr.android.smailer.util.db.DbUtil.getLong
import com.bopr.android.smailer.util.db.DbUtil.getString
import com.bopr.android.smailer.util.db.DbUtil.isNull

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
        return cursor.isNull(columnName)
    }

    protected fun getString(columnName: String): String? {
        return cursor.getString(columnName)
    }

    protected fun getBoolean(columnName: String): Boolean? {
        return getInt(columnName)?.let { it != 0 }
    }

    protected fun getInt(columnName: String): Int? {
        return cursor.getInt(columnName)
    }

    protected fun getLong(columnName: String): Long? {
        return cursor.getLong(columnName)
    }

    protected fun getDouble(columnName: String): Double? {
        return cursor.getDouble(columnName)
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