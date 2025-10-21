package com.bopr.android.smailer.data

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Convenience [Cursor] wrapper. Represents [Cursor] as [MutableSet].
 *
 * @author Boris Pronin ([boris280471@gmail.com](mailto:boris280471@gmail.com))
 */
abstract class ReadonlyDataset<T>(
    protected val tableName: String,
    protected val helper: SQLiteOpenHelper
) : Set<T> {

    protected abstract val keyColumns: Array<String>
    private val rowSet get() = query().drainToSet(::get)
    override val size get() = read { count(tableName).toInt() }

    override fun isEmpty(): Boolean {
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

    open fun last(): T = query().withLast(::get)

    protected abstract fun get(cursor: Cursor): T

    protected open fun query(): Cursor = read { queryRecords(tableName) }

    protected inline fun <R> read(action: SQLiteDatabase.() -> R): R =
        helper.readableDatabase.action()

}