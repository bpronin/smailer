package com.bopr.android.smailer.data

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.bopr.android.smailer.util.Logger

/**
 * Convenience [Cursor] wrapper. Represents [Cursor] as [MutableSet].
 *
 * @author Boris Pronin ([boris280471@gmail.com](mailto:boris280471@gmail.com))
 */
abstract class Dataset<T>(
    protected val tableName: String,
    protected val helper: SQLiteOpenHelper,
    protected val lastModified: MutableSet<String>
) : MutableSet<T> {

    protected abstract val keyColumns: Array<String>
    private val keyClause by lazy { keyColumns.joinToString(" AND ") { "$it=?" } }
    private val rowSet get() = query().drainToSet(::get) // TODO: get rid of it
    override val size get() = read { count(tableName).toInt() }

    override fun add(element: T) = write(tableName) { replaceRecords(it, values(element)) }

    override fun addAll(elements: Collection<T>): Boolean {
        var affected = 0
        for (e in elements) {
            if (add(e)) affected++
        }

        log.debug("$affected items(s) added")
        return affected != 0
    }

    override fun remove(element: T) = write(tableName) {
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

//        query().forEach {
//            val e = get(this)
//            if (!elements.contains(e) && remove(e)) affected++
//        }

        log.debug("$affected items(s) removed in retain")

        return affected != 0
    }

    override fun clear() = write(tableName) {
        deleteRecords(tableName)
    }

    override fun isEmpty(): Boolean {
        return size == 0
    }

    override fun contains(element: T): Boolean = rowSet.contains(element)

    override fun containsAll(elements: Collection<T>): Boolean = rowSet.containsAll(elements)

    override fun iterator(): MutableIterator<T> {
        val iterator = rowSet.iterator()

        return object : MutableIterator<T> {

            var current: T? = null

            override fun hasNext() = iterator.hasNext()

            override fun next() = iterator.next().also { current = it }

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

    fun first(): T = query().withFirst(::get)

    fun last(): T = query().withLast(::get)

    protected abstract fun values(element: T): ContentValues

    protected abstract fun keyOf(element: T): Array<String>

    protected abstract fun get(cursor: Cursor): T

    protected open fun query() = read { queryRecords(tableName) }

    protected fun <R> read(action: SQLiteDatabase.() -> R): R =
        helper.read(action)

    protected fun <R> write(table: String, action: SQLiteDatabase.(String) -> R): R =
        helper.write { action(table) }.also { lastModified.add(table) }

    companion object {

        private val log = Logger("Database")
    }

}