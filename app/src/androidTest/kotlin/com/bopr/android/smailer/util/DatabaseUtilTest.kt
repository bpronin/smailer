package com.bopr.android.smailer.util

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.test.filters.SmallTest
import com.bopr.android.smailer.BaseTest
import com.bopr.android.smailer.data.alterTable
import com.bopr.android.smailer.data.copyTable
import com.bopr.android.smailer.data.drainToSet
import com.bopr.android.smailer.data.forEach
import com.bopr.android.smailer.data.getInt
import com.bopr.android.smailer.data.getString
import com.bopr.android.smailer.data.getStringIfExists
import com.bopr.android.smailer.data.getStringOrNull
import com.bopr.android.smailer.data.getTables
import com.bopr.android.smailer.data.queryRecords
import com.bopr.android.smailer.data.values
import com.bopr.android.smailer.data.write
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * AndroidUtil tester.
 *
 * @author Boris Pronin ([boris280471@gmail.com](mailto:boris280471@gmail.com))
 */
@SmallTest
class DatabaseUtilTest : BaseTest() {

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
        helper.write {
            assertTrue(getTables().isEmpty())

            execSQL(
                "CREATE TABLE TABLE_1 (" +
                        "ID INTEGER PRIMARY KEY, " +
                        "COLUMN_1 TEXT(25)" +
                        ")"
            )
            execSQL(
                "CREATE TABLE TABLE_2 (" +
                        "ID INTEGER PRIMARY KEY, " +
                        "COLUMN_1 TEXT(25)" +
                        ")"
            )

            assertEquals(setOf("TABLE_1", "TABLE_2"), getTables())

            execSQL("DROP TABLE TABLE_1")

            assertEquals(setOf("TABLE_2"), getTables())
        }
    }

    @Test
    fun testCopyTable() {
        helper.write {
            execSQL(
                "CREATE TABLE TABLE_1 (" +
                        "ID INTEGER PRIMARY KEY, " +
                        "COLUMN_1 TEXT(25)" +
                        ")"
            )
            execSQL(
                "CREATE TABLE TABLE_2 (" +
                        "ID INTEGER PRIMARY KEY, " +
                        "COLUMN_1 TEXT(25)" +
                        ")"
            )
            insert("TABLE_1", null, values { put("ID", 10); put("COLUMN_1", "A") })
            insert("TABLE_1", null, values { put("ID", 11); put("COLUMN_1", "B") })
            insert("TABLE_1", null, values { put("ID", 12); put("COLUMN_1", "C") })

            copyTable("TABLE_1", "TABLE_2")

            val ids = mutableListOf<Int>()
            val values = mutableListOf<String?>()
            queryRecords("TABLE_2").forEach {
                ids.add(getInt("ID"))
                values.add(getString("COLUMN_1"))
            }

            assertEquals(listOf(10, 11, 12), ids)
            assertEquals(listOf("A", "B", "C"), values)
        }
    }

    @Test
    fun testCopyTableNoColumn() {
        helper.write {
            execSQL(
                "CREATE TABLE TABLE_1 (" +
                        "ID INTEGER PRIMARY KEY, " +
                        "COLUMN_1 TEXT(25)" +
                        ")"
            )
            execSQL(
                "CREATE TABLE TABLE_2 (" +
                        "ID INTEGER PRIMARY KEY, " +
                        "COLUMN_2 TEXT(25)" +
                        ")"
            )
            insert("TABLE_1", null, values { put("ID", 10); put("COLUMN_1", "A") })
            insert("TABLE_1", null, values { put("ID", 11); put("COLUMN_1", "B") })
            insert("TABLE_1", null, values { put("ID", 12); put("COLUMN_1", "C") })

            copyTable("TABLE_1", "TABLE_2")

            val ids = mutableListOf<Int>()
            val values = mutableListOf<String?>()
            queryRecords("TABLE_2").forEach {
                ids.add(getInt("ID"))
                values.add(getStringOrNull("COLUMN_2"))
            }

            assertEquals(listOf(10, 11, 12), ids)
            assertEquals(listOf<String?>(null, null, null), values)
        }
    }

    @Test
    fun testCopyTableTransform() {
        helper.write {
            execSQL(
                "CREATE TABLE TABLE_1 (" +
                        "ID INTEGER PRIMARY KEY, " +
                        "COLUMN_1 TEXT(25)" +
                        ")"
            )
            execSQL(
                "CREATE TABLE TABLE_2 (" +
                        "ID INTEGER PRIMARY KEY, " +
                        "COLUMN_2 TEXT(25)" +
                        ")"
            )
            insert("TABLE_1", null, values { put("ID", 10); put("COLUMN_1", "A") })
            insert("TABLE_1", null, values { put("ID", 11); put("COLUMN_1", "B") })
            insert("TABLE_1", null, values { put("ID", 12); put("COLUMN_1", "C") })

            copyTable("TABLE_1", "TABLE_2") { column: String ->
                if (column == "COLUMN_2")
                    getString("COLUMN_1") + "_" + getInt("ID")
                else
                    getStringIfExists(column)
            }

            val ids = mutableListOf<Int>()
            val values = mutableListOf<String?>()
            queryRecords("TABLE_2").forEach {
                ids.add(getInt("ID"))
                values.add(getString("COLUMN_2"))
            }

            assertEquals(listOf(10, 11, 12), ids)
            assertEquals(listOf("A_10", "B_11", "C_12"), values)
        }
    }

    @Test
    fun testAlterTable() {
        helper.write {
            execSQL(
                "CREATE TABLE TABLE_1 (" +
                        "ID INTEGER PRIMARY KEY, " +
                        "COLUMN_1 TEXT(25)" +
                        ")"
            )
            insert("TABLE_1", null, values { put("ID", 0); put("COLUMN_1", "A") })
            insert("TABLE_1", null, values { put("ID", 1); put("COLUMN_1", "B") })
            insert("TABLE_1", null, values { put("ID", 2); put("COLUMN_1", "C") })

            alterTable(
                "TABLE_1", "CREATE TABLE TABLE_1 (" +
                        "ID INTEGER PRIMARY KEY, " +
                        "COLUMN_2 TEXT(25)" +
                        ")"
            )
            { column: String ->
                if (column == "COLUMN_2")
                    getString("COLUMN_1") + "_PRIM"
                else
                    null
            }

            val values = queryRecords("TABLE_1").drainToSet {
                getString("COLUMN_2")
            }

            assertEquals(setOf("A_PRIM", "B_PRIM", "C_PRIM"), values)
            assertEquals(setOf("TABLE_1"), getTables()) /* ensure temp table is removed */
        }
    }

    @Test
    fun testAlterTableNoSource() {
        helper.write {
            alterTable(
                "TABLE_1", "CREATE TABLE TABLE_1 (" +
                        "ID INTEGER PRIMARY KEY, " +
                        "COLUMN_2 TEXT(25)" +
                        ")"
            )
            { column: String ->
                if (column == "COLUMN_2")
                    getString("COLUMN_1") + "_PRIM"
                else
                    null
            }

            val values = queryRecords("TABLE_1").drainToSet {
                getString("COLUMN_2")
            }

            assertTrue(values.isEmpty())
            assertEquals(setOf("TABLE_1"), getTables()) /* ensure temp table is removed */
        }
    }

    /*
        @Test
        fun testCursorIterator() {
            helper.writableDatabase.apply {
                execSQL("CREATE TABLE TABLE_1 (" +
                        "ID INTEGER PRIMARY KEY, " +
                        "COLUMN_1 TEXT(25)" +
                        ")")
                insert("TABLE_1", null, values { put("ID", 10); put("COLUMN_1", "A") })
                insert("TABLE_1", null, values { put("ID", 11); put("COLUMN_1", "B") })
                insert("TABLE_1", null, values { put("ID", 12); put("COLUMN_1", "C") })

                val cursor = query("TABLE_1")
                val iterator = CursorIterator(cursor) { it.getString("COLUMN_1") }

                assertEquals(listOf("A", "B", "C"), iterator.asSequence().toList())
                assertThrows(IllegalStateException::class.java) { */
    /* must be closed *//*

                cursor.moveToFirst()
            }
        }
    }

    @Test
    fun testCursorIteratorBreak() {
        helper.writableDatabase.apply {
            execSQL("CREATE TABLE TABLE_1 (" +
                    "ID INTEGER PRIMARY KEY, " +
                    "COLUMN_1 TEXT(25)" +
                    ")")
            insert("TABLE_1", null, values { put("ID", 10); put("COLUMN_1", "A") })
            insert("TABLE_1", null, values { put("ID", 11); put("COLUMN_1", "B") })
            insert("TABLE_1", null, values { put("ID", 12); put("COLUMN_1", "C") })

            val cursor = query("TABLE_1")

            cursor.iterator { it.getString("COLUMN_1") }.use {
                while (it.hasNext()) {
                    val s = it.next()
                    if (s == "B") break
                }
            }

            assertThrows(IllegalStateException::class.java) { */
    /* must be closed *//*

                cursor.moveToFirst()
            }
        }
    }

    @Test
    fun testCursorIteratorEmpty() {
        helper.writableDatabase.apply {
            execSQL("CREATE TABLE TABLE_1 (" +
                    "ID INTEGER PRIMARY KEY, " +
                    "COLUMN_1 TEXT(25)" +
                    ")")

            val cursor = query("TABLE_1")

            val iterator = cursor.iterator { it.getString("COLUMN_1") }

            assertEquals(emptyList<String>(), iterator.asSequence().toList())
            */
    /* NOTE: if cursor is empty and closed exception won't be thrown when we access it *//*

        }
    }

    @Test
    fun testMutableCursorIterator() {
        helper.writableDatabase.apply {
            execSQL("CREATE TABLE TABLE_1 (" +
                    "ID INTEGER PRIMARY KEY, " +
                    "COLUMN_1 TEXT(25)" +
                    ")")
            insert("TABLE_1", null, values { put("ID", 10); put("COLUMN_1", "A") })
            insert("TABLE_1", null, values { put("ID", 11); put("COLUMN_1", "B") })
            insert("TABLE_1", null, values { put("ID", 12); put("COLUMN_1", "C") })

            val cursor = query("TABLE_1")

            val iterator = cursor.mutableIterator(
                    get = {
                        it.getString("COLUMN_1")
                    },
                    remove = {
                        delete("TABLE_1", "COLUMN_1='$it'", null)
                        true
                    }
            )

            while (iterator.hasNext()) {
                if (iterator.next() == "B") {
                    iterator.remove()
                }
            }

            assertThrows(IllegalStateException::class.java) {  */
    /* must be closed *//*

                cursor.moveToFirst()
            }

            assertEquals(listOf("A", "C"), query("TABLE_1").useToList { getString("COLUMN_1") })
        }
    }
*/

}
