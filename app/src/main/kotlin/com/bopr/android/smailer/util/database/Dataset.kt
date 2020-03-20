package com.bopr.android.smailer.util.database

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.bopr.android.smailer.Database.Companion.COLUMN_COUNT
import com.bopr.android.smailer.util.strings
import org.slf4j.LoggerFactory

/**
 * Convenience [Cursor] wrapper.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
abstract class Dataset<T>(
        private val tableName: String,
        private val helper: SQLiteOpenHelper,
        private val modifications: MutableCollection<String>
) : MutableSet<T> {

    private val log = LoggerFactory.getLogger("Database")
    protected val readable: SQLiteDatabase get() = helper.readableDatabase
    protected val writable: SQLiteDatabase get() = helper.writableDatabase

    override val size: Int
        get() = readable.query(tableName, strings(COLUMN_COUNT)).useFirst { getInt(0) }

    override fun clear() {
        val affected = writable.batch {
            delete(tableName, null, null)
        }
        if (affected != 0) {
            modified()
            log.debug("All items removed from $tableName")
        }
    }

    override fun addAll(elements: Collection<T>): Boolean {
        writable.batch {
            for (e in elements) {
                add(e)
            }
        }
        return true
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        var modified = false
        writable.batch {
            for (e in elements) {
                modified = remove(e)
            }
        }
        if (modified) {
            modified()
            log.debug("$modified items(s) removed")
        }
        return modified
    }

    override fun iterator(): MutableCursorIterator<T> {
        return query().mutableIterator(::get, ::remove)
    }

    override fun isEmpty(): Boolean {
        return size == 0
    }

    fun first(): T {
        return query().useFirst(::get)
    }

    fun replaceAll(elements: Collection<T>) {
        writable.batch {
            clear()
            addAll(elements)
        }
    }

    protected abstract fun query(): Cursor

    protected abstract fun get(cursor: Cursor): T

    protected fun modified() {
        modifications.add(tableName)
    }

    override fun contains(element: T): Boolean {
        TODO("Not yet implemented")
    }

    override fun containsAll(elements: Collection<T>): Boolean {
        TODO("Not yet implemented")
    }

    override fun retainAll(elements: Collection<T>): Boolean {
        TODO("Not yet implemented")
    }

}