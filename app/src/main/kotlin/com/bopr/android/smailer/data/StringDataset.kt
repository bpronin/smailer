package com.bopr.android.smailer.data

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteOpenHelper
import com.bopr.android.smailer.data.Database.Companion.COLUMN_VALUE
import com.bopr.android.smailer.util.stringArrayOf

class StringDataset(tableName: String, helper: SQLiteOpenHelper, modifications: MutableSet<String>)
    : Dataset<String>(tableName, helper, modifications) {

    override val keyColumns = stringArrayOf(COLUMN_VALUE)

    override fun keyOf(element: String): Array<String> {
        return stringArrayOf(element)
    }

    override fun get(cursor: Cursor): String {
        return cursor.getString(COLUMN_VALUE)
    }

    override fun values(element: String): ContentValues {
        return values {
            put(COLUMN_VALUE, element)
        }
    }
}