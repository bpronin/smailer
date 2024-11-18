package com.bopr.android.smailer.data

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE
import android.database.sqlite.SQLiteOpenHelper
import com.bopr.android.smailer.util.Logger

/**
 * Convenience [Cursor] wrapper. Represents [Cursor] as [MutableSet].
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
abstract class Dataset<T>(
    protected val tableName: String,
    protected val helper: SQLiteOpenHelper,
    protected val modifications: MutableSet<String>
) : MutableSet<T> {

    protected abstract val keyColumns: Array<String>
    private val keyClause by lazy { keyColumns.joinToString(" AND ") { "$it=?" } }
    private val rowSet get() = query().toSet(::get)
    override val size get() = read { count(tableName).toInt() }

    override fun add(element: T): Boolean {
        val values = values(element)
        return insert(values) || update(values, element)
    }

    override fun addAll(elements: Collection<T>): Boolean {
        var affected = 0
        for (e in elements) {
            if (add(e)) affected++
        }

        log.debug("$affected items(s) added")
        return affected != 0
    }

    override fun remove(element: T): Boolean = write {
        delete(tableName, keyClause, keyOf(element)) != 0
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        var affected = 0
        for (e in elements) {
            if (remove(e)) affected++
        }

        log.debug("$affected items(s) removed")

        return affected != 0
    }

    override fun retainAll(elements: Collection<T>): Boolean {
        var affected = 0
        for (e in rowSet) {
            if (!elements.contains(e) && remove(e)) affected++
        }

        log.debug("$affected items(s) removed in retain")

        return affected != 0
    }

    override fun clear() {
        write {
            delete(tableName, null, null).also {
                log.debug("All items removed from $tableName")
            }
        }
    }

    override fun isEmpty(): Boolean {
        @Suppress("ReplaceSizeZeroCheckWithIsEmpty")
        return size == 0
    }

    override fun contains(element: T): Boolean = rowSet.contains(element)

    override fun containsAll(elements: Collection<T>): Boolean = rowSet.containsAll(elements)

    override fun iterator(): MutableIterator<T> {
        val iterator = rowSet.iterator()

        return object : MutableIterator<T> {

            var current: T? = null

            override fun hasNext(): Boolean = iterator.hasNext()

            override fun next(): T {
                return iterator.next().also {
                    current = it
                }
            }

            override fun remove() {
                remove(current)
            }
        }
    }

    fun put(element: T) = add(element)

    fun replaceAll(elements: Collection<T>): Boolean {
        clear()
        return addAll(elements)
    }

    open fun first(): T = query().useFirst(::get)

    open fun last(): T = query().useLast(::get)

    protected abstract fun get(cursor: Cursor): T

    protected abstract fun values(element: T): ContentValues

    protected abstract fun keyOf(element: T): Array<String>

    protected open fun query(): Cursor = read { query(tableName) }

    protected open fun insert(values: ContentValues): Boolean = write {
        insertWithOnConflict(tableName, null, values, CONFLICT_IGNORE) != -1L
    }.also {
        if (it) log.debug("Inserted").verb(values)
    }

    protected open fun update(values: ContentValues, element: T): Boolean = write {
        update(tableName, values, keyClause, keyOf(element)) != 0
    }.also {
        if (it) log.debug("Updated").verb(values)
    }

    protected inline fun <R> read(action: SQLiteDatabase.() -> R): R =
        helper.readableDatabase.action()

    protected inline fun <R> write(action: SQLiteDatabase.() -> R): R =
        helper.writableDatabase.action().also<R> {
            modifications.add(tableName)
        }

    companion object {

        private val log = Logger("Database")
    }

}