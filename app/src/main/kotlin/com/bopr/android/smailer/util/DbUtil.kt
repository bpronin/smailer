package com.bopr.android.smailer.util

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

/**
 * Database utilities.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */

inline fun SQLiteDatabase.batch(action: SQLiteDatabase.() -> Unit) {
    this.beginTransaction()
    try {
        action()
        this.setTransactionSuccessful()
    } finally {
        this.endTransaction()
    }
}

fun SQLiteDatabase.replaceTable(table: String, createSql: String,
                                convert: (column: String, cursor: Cursor) -> String?) {
    batch {
        val old = table + "_old"
        execSQL("DROP TABLE IF EXISTS $old")
        execSQL("ALTER TABLE $table RENAME TO $old")
        execSQL(createSql)
        copyTable(old, table, convert)
        execSQL("DROP TABLE $old")
        setTransactionSuccessful()
    }
}

private fun SQLiteDatabase.copyTable(tableFrom: String, tableTo: String,
                                     convert: (column: String, cursor: Cursor) -> String?) {
    val dst = query(tableTo, null, null, null, null, null, null)
    val src = query(tableFrom, null, null, null, null, null, null)
    val srcColumns = src.columnNames
    val dstColumns = dst.columnNames
    try {
        src.moveToFirst()
        while (!src.isAfterLast) {
            val values = ContentValues()
            for (column in dstColumns) {
                if (srcColumns.contains(column)) {
                    values.put(column, convert(column, src))
                }
            }
            insert(tableTo, null, values)
            src.moveToNext()
        }
    } finally {
        src.close()
        dst.close()
    }
}

fun Cursor.isNull(columnName: String): Boolean {
    return isNull(getColumnIndex(columnName))
}

fun Cursor.getString(columnName: String): String? {
    return getString(getColumnIndex(columnName))
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

fun <T> Cursor.useFirst(action: (Cursor) -> T): T? {
    use {
        moveToFirst()
        return if (!isAfterLast) {
            action(it)
        } else {
            null
        }
    }
}

fun Cursor.useAll(action: (Cursor) -> Unit) {
    use {
        moveToFirst()
        while (!isAfterLast) {
            action(it)
            moveToNext()
        }
    }
}

fun <T> Cursor.useToList(get: (Cursor) -> T): List<T> {
    val list = mutableListOf<T>()
    useAll {
        list.add(get(it))
    }
    return list
}