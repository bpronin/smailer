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
        private val modifications: MutableSet<String>
) : MutableSet<T> {

    private val log = LoggerFactory.getLogger("Database")

    override val size: Int
        get() = read { query(tableName, strings(COLUMN_COUNT)).useFirst { getInt(0) } }

    override fun addAll(elements: Collection<T>): Boolean {
        write {
            for (e in elements) {
                add(e)
            }
        }
        return true
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        var affected = 0
        write {
            for (e in elements) {
                if (remove(e)) affected++
            }
        }

        log.debug("$affected items(s) removed")
        return affected != 0
    }

    override fun retainAll(elements: Collection<T>): Boolean {
        var affected = 0
        write {
            for (e in rowSet()) {
                if (!elements.contains(e) && remove(e)) affected++
            }
        }

        log.debug("$affected items(s) removed")
        return affected != 0
    }

    override fun iterator(): MutableIterator<T> {
        val iterator = rowSet().iterator()

        return object : MutableIterator<T> {

            var current: T? = null

            override fun hasNext(): Boolean = iterator.hasNext()

            override fun next(): T {
                current = iterator.next()
                return current!!
            }

            override fun remove() {
                remove(current)
            }
        }
    }

    override fun clear() {
        write {
            delete(tableName, null, null)
        }
        log.debug("All items removed from $tableName")
    }

    override fun isEmpty(): Boolean {
        return size == 0
    }

    override fun contains(element: T): Boolean {
        return rowSet().contains(element)
    }

    override fun containsAll(elements: Collection<T>): Boolean {
        return rowSet().containsAll(elements)
    }

    fun first(): T {
        return queryAll().useFirst(::get)
    }

    fun last(): T {
        return queryAll().useLast(::get)
    }

    fun replaceAll(elements: Collection<T>) {
        write {
            clear()
            addAll(elements)
        }
    }

    protected abstract fun queryAll(): Cursor

    protected abstract fun get(cursor: Cursor): T

    protected fun <R> read(action: SQLiteDatabase.() -> R): R {
        return helper.readableDatabase.run(action)
    }

    protected fun <R> write(action: SQLiteDatabase.() -> R): R {
        val result = helper.writableDatabase.batch(action)
        modifications.add(tableName)
        return result
    }

    private fun rowSet() = queryAll().toSet(::get)
}