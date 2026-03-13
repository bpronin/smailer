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
    private val helper: SQLiteOpenHelper,
    protected val lastModified: MutableSet<String>
) {

    protected abstract val keyColumns: Array<String>
    private val keyClause by lazy { keyColumns.joinToString(" AND ") { "$it=?" } }
    val size get() = read { count(tableName).toInt() }

    open fun insert(element: T) = write {
        insertRecord(it, values(element))
    }

    fun insert(elements: Iterable<T>) =
        forAll(elements, ::insert).also { log.debug("Inserted $it item(s)") } != 0

    open fun replace(element: T) = write {
        replaceRecord(it, values(element))
    }

    open fun delete(element: T) = write {
        deleteRecords(it, keyClause, keyOf(element))
    }

    fun delete(elements: Iterable<T>) =
        forAll(elements, ::delete).also { log.debug("Deleted $it item(s)") } != 0

    open fun clear() = write {
        deleteRecords(it)
    }

    fun replaceAll(elements: Iterable<T>) = write {
        batchUpdate {
            clear()
            insert(elements)
        }
    }

    fun drain(): Set<T> = query().drainToSet(::get)

    protected abstract fun values(element: T): ContentValues

    protected abstract fun keyOf(element: T): Array<String>

    protected abstract fun get(cursor: Cursor): T

    protected open fun query() = read { queryRecords(tableName) }

    protected fun <R> read(action: SQLiteDatabase.() -> R): R =
        helper.read(action)

    protected fun <R> write(action: SQLiteDatabase.(table: String) -> R): R =
        helper.write { action(tableName) }.also { lastModified.add(tableName) }

    private fun forAll(elements: Iterable<T>, action: (T) -> Boolean): Int {
        var affected = 0
        for (element in elements) {
            if (action(element)) affected++
        }
        return affected
    }

    companion object {

        private val log = Logger("Database")
    }

}