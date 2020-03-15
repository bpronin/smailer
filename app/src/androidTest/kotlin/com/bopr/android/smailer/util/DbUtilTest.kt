package com.bopr.android.smailer.util

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.test.filters.SmallTest
import com.bopr.android.smailer.BaseTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * AndroidUtil tester.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
@SmallTest
class DbUtilTest : BaseTest() {

    private lateinit var helper: SQLiteOpenHelper

    @Before
    fun setup() {
        targetContext.deleteDatabase("test.sqlite")
        helper = object : SQLiteOpenHelper(targetContext, "test.sqlite", null, 1) {

            override fun onCreate(db: SQLiteDatabase) {
            }

            override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
            }
        }
    }

    @After
    fun teardown() {
        helper.close()
    }

    @Test
    fun testIsTableExists() {
        helper.writableDatabase.apply {
            assertEquals(emptySet<String>(), getTables())

            execSQL("CREATE TABLE TABLE_1 (" +
                    "ID INTEGER PRIMARY KEY, " +
                    "COLUMN_1 TEXT(25)" +
                    ")")
            execSQL("CREATE TABLE TABLE_2 (" +
                    "ID INTEGER PRIMARY KEY, " +
                    "COLUMN_1 TEXT(25)" +
                    ")")

            assertEquals(setOf("TABLE_1", "TABLE_2"), getTables())

            execSQL("DROP TABLE TABLE_1")

            assertEquals(setOf("TABLE_2"), getTables())
        }
    }

    @Test
    fun testCopyTable() {
        helper.writableDatabase.apply {
            execSQL("CREATE TABLE TABLE_1 (" +
                    "ID INTEGER PRIMARY KEY, " +
                    "COLUMN_1 TEXT(25)" +
                    ")")
            execSQL("CREATE TABLE TABLE_2 (" +
                    "ID INTEGER PRIMARY KEY, " +
                    "COLUMN_1 TEXT(25)" +
                    ")")
            insert("TABLE_1", null, values { put("ID", 10); put("COLUMN_1", "A") })
            insert("TABLE_1", null, values { put("ID", 11); put("COLUMN_1", "B") })
            insert("TABLE_1", null, values { put("ID", 12); put("COLUMN_1", "C") })

            copyTable("TABLE_1", "TABLE_2")

            val ids = mutableListOf<Int>()
            val values = mutableListOf<String?>()
            query("TABLE_2").useAll {
                ids.add(getInt("ID"))
                values.add(getString("COLUMN_1"))
            }

            assertEquals(listOf(10, 11, 12), ids)
            assertEquals(listOf("A", "B", "C"), values)
        }
    }

    @Test
    fun testCopyTableNoColumn() {
        helper.writableDatabase.apply {
            execSQL("CREATE TABLE TABLE_1 (" +
                    "ID INTEGER PRIMARY KEY, " +
                    "COLUMN_1 TEXT(25)" +
                    ")")
            execSQL("CREATE TABLE TABLE_2 (" +
                    "ID INTEGER PRIMARY KEY, " +
                    "COLUMN_2 TEXT(25)" +
                    ")")
            insert("TABLE_1", null, values { put("ID", 10); put("COLUMN_1", "A") })
            insert("TABLE_1", null, values { put("ID", 11); put("COLUMN_1", "B") })
            insert("TABLE_1", null, values { put("ID", 12); put("COLUMN_1", "C") })

            copyTable("TABLE_1", "TABLE_2")

            val ids = mutableListOf<Int>()
            val values = mutableListOf<String?>()
            query("TABLE_2").useAll {
                ids.add(getInt("ID"))
                values.add(getString("COLUMN_2"))
            }

            assertEquals(listOf(10, 11, 12), ids)
            assertEquals(listOf<String?>(null, null, null), values)
        }
    }

    @Test
    fun testCopyTableTransform() {
        helper.writableDatabase.apply {
            execSQL("CREATE TABLE TABLE_1 (" +
                    "ID INTEGER PRIMARY KEY, " +
                    "COLUMN_1 TEXT(25)" +
                    ")")
            execSQL("CREATE TABLE TABLE_2 (" +
                    "ID INTEGER PRIMARY KEY, " +
                    "COLUMN_2 TEXT(25)" +
                    ")")
            insert("TABLE_1", null, values { put("ID", 10); put("COLUMN_1", "A") })
            insert("TABLE_1", null, values { put("ID", 11); put("COLUMN_1", "B") })
            insert("TABLE_1", null, values { put("ID", 12); put("COLUMN_1", "C") })

            copyTable("TABLE_1", "TABLE_2") { column: String ->
                if (column == "COLUMN_2")
                    getString("COLUMN_1") + "_" + getInt("ID")
                else
                    getStringIfExist(column)
            }

            val ids = mutableListOf<Int>()
            val values = mutableListOf<String?>()
            query("TABLE_2").useAll {
                ids.add(getInt("ID"))
                values.add(getString("COLUMN_2"))
            }

            assertEquals(listOf(10, 11, 12), ids)
            assertEquals(listOf("A_10", "B_11", "C_12"), values)
        }
    }

    @Test
    fun testAlterTable() {
        helper.writableDatabase.apply {
            execSQL("CREATE TABLE TABLE_1 (" +
                    "ID INTEGER PRIMARY KEY, " +
                    "COLUMN_1 TEXT(25)" +
                    ")")
            insert("TABLE_1", null, values { put("ID", 0); put("COLUMN_1", "A") })
            insert("TABLE_1", null, values { put("ID", 1); put("COLUMN_1", "B") })
            insert("TABLE_1", null, values { put("ID", 2); put("COLUMN_1", "C") })

            alterTable("TABLE_1", "CREATE TABLE TABLE_1 (" +
                    "ID INTEGER PRIMARY KEY, " +
                    "COLUMN_2 TEXT(25)" +
                    ")") { column: String ->
                if (column == "COLUMN_2")
                    getString("COLUMN_1") + "_PRIM"
                else
                    null
            }

            val values = query("TABLE_1").useToList {
                getString("COLUMN_2")
            }

            assertEquals(listOf("A_PRIM", "B_PRIM", "C_PRIM"), values)
            assertEquals(setOf("TABLE_1"), getTables()) /* ensure temp table is removed */
        }

    }

}