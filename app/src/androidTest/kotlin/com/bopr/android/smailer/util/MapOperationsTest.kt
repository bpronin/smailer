package com.bopr.android.smailer.util

import androidx.test.filters.SmallTest
import com.bopr.android.smailer.BaseTest
import com.bopr.android.smailer.util.MapOperation.Action.*
import com.bopr.android.smailer.util.MapOperation.Companion.applyUpdates
import com.bopr.android.smailer.util.MapOperation.Companion.getUpdates
import org.junit.Assert.assertEquals
import org.junit.Test

@SmallTest
class MapOperationsTest : BaseTest() {

    @Test
    fun testGetUpdates() {
        val ops = mapOf(1 to "A", 2 to "B", 3 to "C").getUpdates(
                mapOf(1 to "D", 4 to "E", 5 to "F"))

        assertEquals(5, ops.size)
        assertEquals(MapOperation(ACTION_DELETE, 2, "B"), ops[0])
        assertEquals(MapOperation(ACTION_DELETE, 3, "C"), ops[1])
        assertEquals(MapOperation(ACTION_UPDATE, 1, "D"), ops[2])
        assertEquals(MapOperation(ACTION_ADD, 4, "E"), ops[3])
        assertEquals(MapOperation(ACTION_ADD, 5, "F"), ops[4])
    }

    @Test
    fun testApplyUpdates() {
        val map = mutableMapOf(1 to "A", 2 to "B", 3 to "C")
        map.applyUpdates(
                ACTION_DELETE of 2,
                ACTION_DELETE of 3,
                ACTION_UPDATE of (1 to "D"),
                ACTION_ADD of (4 to "E"),
                ACTION_ADD of (5 to "F")
        )
        assertEquals(mapOf(1 to "D", 4 to "E", 5 to "F"), map)
    }

}