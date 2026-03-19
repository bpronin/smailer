@file:Suppress("unused", "NOTHING_TO_INLINE")

package com.bopr.android.smailer.data

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE
import android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
import android.database.sqlite.SQLiteOpenHelper
import com.bopr.android.smailer.util.Logger
import com.bopr.android.smailer.util.stringArrayOf

/**
 * Database utilities.
 *
 * @author Boris Pronin ([boris280471@gmail.com](mailto:boris280471@gmail.com))
 */

val databaseLog = Logger("Database")

val COUNT_SELECTION = stringArrayOf("COUNT(*)")

inline fun <R> SQLiteOpenHelper.read(action: SQLiteDatabase.() -> R): R = readableDatabase.action()

inline fun <R> SQLiteOpenHelper.write(action: SQLiteDatabase.() -> R): R = writableDatabase.action()

inline fun SQLiteDatabase.insertRecord(table: String, values: ContentValues): Boolean {
    databaseLog.debug("Insert into '$table' [$values]")

    return (insertWithOnConflict(table, null, values, CONFLICT_IGNORE) != -1L).also {
        if (!it) databaseLog.warn("Ignored")
    }
}

inline fun SQLiteDatabase.replaceRecord(table: String, values: ContentValues): Boolean {
    databaseLog.debug("Replace into '$table' [$values]")

    return (insertWithOnConflict(table, null, values, CONFLICT_REPLACE) != -1L).also {
        if (it) databaseLog.debug("Inserted") else databaseLog.debug("Replaced")
    }
}

inline fun SQLiteDatabase.deleteRecords(
    table: String,
    where: String? = null,
    whereArgs: Array<out String>? = null
): Boolean {
    databaseLog.debug(
        "Delete from '$table' " +
                "${where?.let { "where $it" } ?: "all"} ${whereArgs?.let { "(${it.joinToString()})" } ?: ""}")

    return (delete(table, where, whereArgs) != 0).also {
        if (!it) databaseLog.warn("Ignored")
    }
}

inline fun SQLiteDatabase.updateRecords(
    table: String,
    values: ContentValues,
    where: String? = null,
    whereArgs: Array<out String>? = null
): Boolean {
    databaseLog.debug(
        "Update '$table'" +
            " ${where?.let { "where $it" } ?: "all"} ${whereArgs?.let { "(${it.joinToString()})" } ?: ""}" +
            " [$values]")

    return (update(table, values, where, whereArgs) != 0).also {
        if (!it) databaseLog.warn("Ignored")
    }
}

inline fun SQLiteDatabase.queryRecords(
    table: String,
    projection: Array<out String>? = null,
    where: String? = null,
    whereArgs: Array<out String>? = null,
    groupBy: String? = null,
    having: String? = null,
    order: String? = null,
    limit: String? = null
): Cursor {
    databaseLog.debug(
        "Query '$table' " +
            "${where?.let { "where $it" } ?: "all"} ${whereArgs?.let { "(${it.joinToString()})" } ?: ""}")

    return query(table, projection, where, whereArgs, groupBy, having, order, limit)
}

inline fun SQLiteDatabase.getTables(): Set<String> {
    return queryRecords(
        "sqlite_master",
        stringArrayOf("name"),
        "type='table' AND name<>'android_metadata'"
    ).drainToSet { getString(0) }
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
    val columns = queryRecords(table = dest, limit = "1").use { it.columnNames }

    queryRecords(src).forEach {
        val values = ContentValues()
        for (column in columns) {
            values.put(column, transform(column))
        }
        insert(dest, null, values)
    }
}

fun SQLiteDatabase.dropTable(table: String) {
    databaseLog.debug("Drop [$table]")

    execSQL("DROP TABLE IF EXISTS $table")
}

inline fun SQLiteDatabase.count(
    table: String,
    selection: String? = null,
): Long {
    return queryRecords(table, COUNT_SELECTION, selection, null).withFirst { getLong(0) }
}

inline fun <T> SQLiteDatabase.batchUpdate(action: SQLiteDatabase.() -> T): T {
    beginTransaction()
    try {
        val result = action()
        setTransactionSuccessful()
        return result
    } finally {
        endTransaction()
    }
}

inline fun Cursor.getString(columnName: String): String {
    return getString(getColumnIndexOrThrow(columnName))
}

inline fun Cursor.getInt(columnName: String): Int {
    return getInt(getColumnIndexOrThrow(columnName))
}

inline fun Cursor.getLong(columnName: String): Long {
    return getLong(getColumnIndexOrThrow(columnName))
}

inline fun Cursor.getDouble(columnName: String): Double {
    return getDouble(getColumnIndexOrThrow(columnName))
}

inline fun Cursor.getBoolean(columnName: String): Boolean {
    return getInt(columnName) != 0
}

inline fun Cursor.getStringOrNull(columnName: String): String? =
    getNullable(columnName, Cursor::getString)

inline fun Cursor.getIntOrNull(columnName: String): Int? = getNullable(columnName, Cursor::getInt)

inline fun Cursor.getLongOrNull(columnName: String): Long? =
    getNullable(columnName, Cursor::getLong)

inline fun Cursor.getDoubleOrNull(columnName: String): Double? =
    getNullable(columnName, Cursor::getDouble)

inline fun Cursor.getBooleanOrNull(columnName: String): Boolean? = getNullable(columnName) {
    getInt(it) != 0
}

inline fun Cursor.getStringIfExists(columnName: String): String? {
    val columnIndex = getColumnIndex(columnName)
    return if (columnIndex != -1) getString(columnIndex) else null
}

inline fun <T> Cursor.getNullable(columnName: String, get: Cursor.(columnIndex: Int) -> T): T? {
    val columnIndex = getColumnIndexOrThrow(columnName)
    return if (isNull(columnIndex)) null else get(columnIndex)
}

inline fun <T> Cursor.withFirst(action: Cursor.() -> T): T = use {
    moveToFirst()
    if (!isAfterLast) action() else throw NoSuchElementException("Row set is empty.")
}

inline fun <T> Cursor.tryWithFirst(action: Cursor.() -> T): T? = use {
    moveToFirst()
    if (!isAfterLast) action() else null
}

inline fun Cursor.forEach(action: Cursor.() -> Unit) = use {
    moveToFirst()
    while (!isAfterLast) {
        action()
        moveToNext()
    }
}

//inline fun <T> Cursor.withLast(action: Cursor.() -> T): T {
//    return use {
//        moveToLast()
//        if (!isAfterLast) action() else throw NoSuchElementException("Row set is empty.")
//    }
//}

//fun <T> Cursor.iterator(get: Cursor.() -> T): Iterator<T> {
//    val cursor = this
//    moveToFirst()
//    return object : Iterator<T> {
//
//        override fun hasNext() = !isAfterLast
//
//        override fun next(): T {
//            val next = cursor.get()
//            moveToNext()
//            return next
//        }
//    }
//}

inline fun <T> Cursor.drainToSet(get: Cursor.() -> T): Set<T> {
    return mutableSetOf<T>().also {
        forEach { it.add(get()) }
    }
}

inline fun values(action: ContentValues.() -> Unit) = ContentValues().apply(action)
