package com.bopr.android.smailer.util

import android.annotation.SuppressLint
import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

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
            .useToList { getString(0) }.toSet()
}

inline fun SQLiteDatabase.alterTable(table: String, createSql: String,
                                     transform: Cursor.(String) -> String? = { getStringIfExist(it) }) {
    val old = table + "_old"
    batch {
        execSQL("DROP TABLE IF EXISTS $old")
        execSQL("ALTER TABLE $table RENAME TO $old")
        execSQL(createSql)
        copyTable(old, table, transform)
        execSQL("DROP TABLE $old")
    }
}

@SuppressLint("Recycle")
inline fun SQLiteDatabase.copyTable(srcTable: String, dstTable: String,
                                    transform: Cursor.(String) -> String? = { getStringIfExist(it) }) {
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

fun Cursor.getString(columnName: String): String? {
    return getString(getColumnIndex(columnName))
}

fun Cursor.getStringIfExist(columnName: String): String? {
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

inline fun <T> Cursor.useFirst(action: Cursor.() -> T): T? {
    return use {
        moveToFirst()
        if (!isAfterLast) action() else null
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

inline fun <T> Cursor.useToList(get: Cursor.() -> T): List<T> {
    val list = mutableListOf<T>()
    useAll {
        list.add(get())
    }
    return list
}

inline fun values(action: ContentValues.() -> Unit): ContentValues = ContentValues().apply(action)