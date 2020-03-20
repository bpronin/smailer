@file:Suppress("unused")

package com.bopr.android.smailer.util.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.bopr.android.smailer.util.strings

/**
 * Database utilities.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
fun SQLiteDatabase.query(table: String, projection: Array<String>? = null, selection: String? = null,
                         selectionArgs: Array<String>? = null, groupBy: String? = null,
                         having: String? = null, order: String? = null, limit: String? = null): Cursor {
    return query(table, projection, selection, selectionArgs, groupBy, having, order, limit)
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
fun SQLiteDatabase.update(table: String, values: ContentValues, where: String? = null,
                          whereArgs: Array<String>? = null): Int {
    return update(table, values, where, whereArgs)
}

inline fun <T> SQLiteDatabase.batch(action: SQLiteDatabase.() -> T): T {
    beginTransaction()
    try {
        val result = action()
        setTransactionSuccessful()
        return result
    } finally {
        endTransaction()
    }
}

@SuppressLint("Recycle")
fun SQLiteDatabase.getTables(): Set<String> {
    return query("sqlite_master", strings("name"), "type='table' AND name<>'android_metadata'")
            .toSet { getString(0) }
}

@SuppressLint("Recycle")
fun SQLiteDatabase.isTableExists(name: String): Boolean {
    return query("sqlite_master", strings("COUNT(*)"), "type='table' AND name='$name'")
            .useFirst { getLong(0) } == 1L
}

inline fun SQLiteDatabase.alterTable(table: String, schemaSql: String,
                                     transform: Cursor.(String) -> String? = { getStringIfExists(it) }) {
    val old = table + "_old"
    dropTable(old) /* ensure temp table is not exists */
    if (isTableExists(table)) {
        execSQL("ALTER TABLE $table RENAME TO $old")
        execSQL(schemaSql)
        copyTable(old, table, transform)
        dropTable(old)
    } else {
        execSQL(schemaSql)
    }
}

@SuppressLint("Recycle")
inline fun SQLiteDatabase.copyTable(srcTable: String, dstTable: String,
                                    transform: Cursor.(String) -> String? = { getStringIfExists(it) }) {
    val columns = query(table = dstTable, limit = "1").use {
        it.columnNames
    }

    query(srcTable).useAll {
        val values = ContentValues()
        for (column in columns) {
            values.put(column, transform(column))
        }
        insert(dstTable, null, values)
    }
}

fun SQLiteDatabase.dropTable(table: String) {
    execSQL("DROP TABLE IF EXISTS $table")
}

fun Cursor.getString(columnName: String): String? {
    return getString(getColumnIndex(columnName))
}

fun Cursor.getStringIfExists(columnName: String): String? {
    val columnIndex = getColumnIndex(columnName)
    return if (columnIndex != -1) getString(columnIndex) else null
}

fun Cursor.getInt(columnName: String): Int {
    return getInt(getColumnIndex(columnName))
}

fun Cursor.getLong(columnName: String): Long {
    return getLong(getColumnIndex(columnName))
}

fun Cursor.getDouble(columnName: String): Double {
    return getDouble(getColumnIndex(columnName))
}

fun Cursor.getBoolean(columnName: String): Boolean {
    return getInt(columnName) != 0
}

fun Cursor.getIntOrNull(columnName: String): Int? {
    val index = getColumnIndex(columnName)
    return if (isNull(index)) null else getInt(index)
}

fun Cursor.getLongOrNull(columnName: String): Long? {
    val index = getColumnIndex(columnName)
    return if (isNull(index)) null else getLong(index)
}

fun Cursor.getDoubleOrNull(columnName: String): Double? {
    val index = getColumnIndex(columnName)
    return if (isNull(index)) null else getDouble(index)
}

fun Cursor.getBooleanOrNull(columnName: String): Boolean? {
    val index = getColumnIndex(columnName)
    return if (isNull(index)) null else {
        getInt(index) != 0
    }
}

inline fun <T> Cursor.useFirst(action: Cursor.() -> T): T {
    return use {
        moveToFirst()
        if (!isAfterLast) action() else throw NoSuchElementException("Row set is empty.")
    }
}

inline fun <T> Cursor.useLast(action: Cursor.() -> T): T {
    return use {
        moveToLast()
        if (!isAfterLast) action() else throw NoSuchElementException("Row set is empty.")
    }
}

inline fun Cursor.useAll(action: Cursor.() -> Unit) {
    use {
        moveToFirst()
        while (!isAfterLast) {
            action()
            moveToNext()
        }
    }
}

inline fun <T> Cursor.toSet(get: Cursor.() -> T): Set<T> {
    val set = mutableSetOf<T>()
    useAll {
        set.add(get())
    }
    return set
}

inline fun values(action: ContentValues.() -> Unit): ContentValues = ContentValues().apply(action)

