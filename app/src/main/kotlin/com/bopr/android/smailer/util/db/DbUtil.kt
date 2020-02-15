package com.bopr.android.smailer.util.db

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

/**
 * Database utilities.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
object DbUtil {

    inline fun SQLiteDatabase.batch(action: (db: SQLiteDatabase) -> Unit) {
        this.beginTransaction()
        try {
            action(this)
            this.setTransactionSuccessful()
        } finally {
            this.endTransaction()
        }
    }

    @JvmStatic
    fun copyTable(db: SQLiteDatabase, tableFrom: String, tableTo: String,
                  convert: (column: String, cursor: Cursor) -> String?) {
        val dst = db.query(tableTo, null, null, null, null, null, null)
        val src = db.query(tableFrom, null, null, null, null, null, null)
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
                db.insert(tableTo, null, values)
                src.moveToNext()
            }
        } finally {
            src.close()
            dst.close()
        }
    }

    @JvmStatic
    fun replaceTable(db: SQLiteDatabase, table: String, createSql: String,
                     convert: (column: String, cursor: Cursor) -> String?) {
        db.beginTransaction()
        try {
            val old = table + "_old"
            db.execSQL("DROP TABLE IF EXISTS $old")
            db.execSQL("ALTER TABLE $table RENAME TO $old")
            db.execSQL(createSql)
            copyTable(db, old, table, convert)
            db.execSQL("DROP TABLE $old")
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

}