package com.bopr.android.smailer.data

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Convenience [Cursor] wrapper. Represents [Cursor] as [MutableSet].
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
abstract class ReadonlyDataset<T>(
    protected val tableName: String,
    protected val helper: SQLiteOpenHelper
) : Set<T> {

    protected abstract val keyColumns: Array<String>
    private val rowSet get() = query().toSet(::get)
    override val size get() = read { count(tableName).toInt() }

    override fun isEmpty(): Boolean {
        @Suppress("ReplaceSizeZeroCheckWithIsEmpty")
        return size == 0
    }

    override fun contains(element: T): Boolean = rowSet.contains(element)

    override fun containsAll(elements: Collection<T>): Boolean = rowSet.containsAll(elements)

    override fun iterator(): Iterator<T> {
        val iterator = rowSet.iterator()

        return object : Iterator<T> {

            var current: T? = null

            override fun hasNext(): Boolean = iterator.hasNext()

            override fun next(): T {
                return iterator.next().also {
                    current = it
                }
            }
        }
    }

    open fun first(): T = query().withFirst(::get)

    open fun last(): T = query().withLast(::get)

    protected abstract fun get(cursor: Cursor): T

    protected open fun query(): Cursor = read { query(tableName) }

    protected inline fun <R> read(action: SQLiteDatabase.() -> R): R =
        helper.readableDatabase.action()

}