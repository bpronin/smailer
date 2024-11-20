@file:Suppress("unused", "NOTHING_TO_INLINE")

package com.bopr.android.smailer.data

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
import android.database.sqlite.SQLiteOpenHelper
import com.bopr.android.smailer.util.Logger
import com.bopr.android.smailer.util.stringArrayOf

/**
 * Database utilities.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */

private val log = Logger("Database")

val COUNT_SELECTION = stringArrayOf("COUNT(*)")

fun <R> SQLiteOpenHelper.read(action: SQLiteDatabase.() -> R): R = readableDatabase.action()

fun <R> SQLiteOpenHelper.write(action: SQLiteDatabase.() -> R): R = writableDatabase.action()

fun SQLiteDatabase.replace(table: String, values: ContentValues): Boolean {
    log.debug("Insert into [$table]").verb(values)

    val result = insertWithOnConflict(table, null, values, CONFLICT_REPLACE) != -1L

    if (!result) log.debug("Replaced")

    return result
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
fun SQLiteDatabase.update(
    table: String,
    values: ContentValues,
    where: String? = null,
    whereArgs: Array<out String>? = null
): Boolean {
    log.debug("Update [$table]").verb(values)

    return update(table, values, where, whereArgs) != 0
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
fun SQLiteDatabase.query(
    table: String,
    projection: Array<out String>? = null,
    selection: String? = null,
    selectionArgs: Array<out String>? = null,
    groupBy: String? = null,
    having: String? = null,
    order: String? = null,
    limit: String? = null
): Cursor {
    log.debug("Query [$table]")

    return query(table, projection, selection, selectionArgs, groupBy, having, order, limit)
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
fun SQLiteDatabase.delete(
    table: String,
    selection: String? = null,
    selectionArgs: Array<out String>? = null
) {
    log.debug("Delete from [$table]")

    delete(table, selection, selectionArgs)
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

inline fun SQLiteDatabase.getTables(): Set<String> {
    return query(
        "sqlite_master",
        stringArrayOf("name"),
        "type='table' AND name<>'android_metadata'"
    )
        .toSet { getString(0) }
}

inline fun SQLiteDatabase.isTableExists(name: String): Boolean {
    return count("sqlite_master", "type='table' AND name='$name'") == 1L
}

inline fun SQLiteDatabase.alterTable(
    table: String, schemaSql: String,
    transform: Cursor.(String) -> String? = { getStringIfExists(it) }
) {
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

inline fun SQLiteDatabase.copyTable(
    src: String, dest: String,
    transform: Cursor.(String) -> String? = { getStringIfExists(it) }
) {
    val columns = query(table = dest, limit = "1").use { it.columnNames }

    query(src).forEach {
        val values = ContentValues()
        for (column in columns) {
            values.put(column, transform(column))
        }
        insert(dest, null, values)
    }
}

fun SQLiteDatabase.dropTable(table: String) {
    log.debug("Drop [$table]")

    execSQL("DROP TABLE IF EXISTS $table")
}

inline fun SQLiteDatabase.count(
    table: String, selection: String? = null,
    selectionArgs: Array<out String>? = null
): Long {
    return query(table, COUNT_SELECTION, selection, selectionArgs).withFirst { getLong(0) }
}

inline fun Cursor.getString(columnName: String): String {
    return getString(getColumnIndexOrThrow(columnName))
}

inline fun Cursor.getInt(columnName: String): Int {
    return getInt(getColumnIndexOrThrow((columnName)))
}

inline fun Cursor.getLong(columnName: String): Long {
    return getLong(getColumnIndexOrThrow((columnName)))
}

inline fun Cursor.getDouble(columnName: String): Double {
    return getDouble(getColumnIndexOrThrow((columnName)))
}

inline fun Cursor.getBoolean(columnName: String): Boolean {
    return getInt(columnName) != 0
}

inline fun Cursor.getStringOrNull(columnName: String): String? {
    val index = getColumnIndex(columnName)
    return if (isNull(index)) null else getString(index)
}

inline fun Cursor.getIntOrNull(columnName: String): Int? {
    val index = getColumnIndex(columnName)
    return if (isNull(index)) null else getInt(index)
}

inline fun Cursor.getLongOrNull(columnName: String): Long? {
    val index = getColumnIndex(columnName)
    return if (isNull(index)) null else getLong(index)
}

inline fun Cursor.getDoubleOrNull(columnName: String): Double? {
    val index = getColumnIndex(columnName)
    return if (isNull(index)) null else getDouble(index)
}

inline fun Cursor.getBooleanOrNull(columnName: String): Boolean? {
    val index = getColumnIndex(columnName)
    return if (isNull(index)) null else {
        getInt(index) != 0
    }
}

inline fun Cursor.getStringIfExists(columnName: String): String? {
    val columnIndex = getColumnIndex(columnName)
    return if (columnIndex != -1) getString(columnIndex) else null
}

inline fun <T> Cursor.withFirst(action: Cursor.() -> T): T {
    return use {
        moveToFirst()
        if (!isAfterLast) action() else throw NoSuchElementException("Row set is empty.")
    }
}

inline fun <T> Cursor.withLast(action: Cursor.() -> T): T {
    return use {
        moveToLast()
        if (!isAfterLast) action() else throw NoSuchElementException("Row set is empty.")
    }
}

inline fun Cursor.forEach(action: Cursor.() -> Unit) {
    use {
        moveToFirst()
        while (!isAfterLast) {
            action()
            moveToNext()
        }
    }
}

//fun Cursor.iterator(): Iterator<Cursor> {
//    val me = this
//    moveToFirst()
//    return object : Iterator<Cursor> {
//
//        override fun hasNext() = !isAfterLast
//
//        override fun next(): Cursor {
//            moveToNext()
//            return me
//        }
//
//    }
//}

inline fun <T> Cursor.toSet(get: Cursor.() -> T): Set<T> {
    val set = mutableSetOf<T>()
    forEach { set.add(get()) }
    return set
}

inline fun values(action: ContentValues.() -> Unit): ContentValues = ContentValues().apply(action)

