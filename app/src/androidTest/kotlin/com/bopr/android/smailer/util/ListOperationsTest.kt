package com.bopr.android.smailer.util

import androidx.test.filters.SmallTest
import com.bopr.android.smailer.BaseTest
import com.bopr.android.smailer.util.ListOperation.Action.ACTION_DELETE
import com.bopr.android.smailer.util.ListOperation.Action.ACTION_UPDATE
import com.bopr.android.smailer.util.ListOperation.Companion.applyUpdates
import com.bopr.android.smailer.util.ListOperation.Companion.getUpdates
import org.junit.Assert.assertEquals
import org.junit.Test

@SmallTest
class ListOperationsTest : BaseTest() {

    @Test
    fun testGetUpdates() {
        val ops = listOf("A", "B", "C").getUpdates(listOf("A", "E", "F"))

        assertEquals(4, ops.size)
        assertEquals(ListOperation(ACTION_DELETE, "B"), ops[0])
        assertEquals(ListOperation(ACTION_DELETE, "C"), ops[1])
        assertEquals(ListOperation(ACTION_UPDATE, "E"), ops[2])
        assertEquals(ListOperation(ACTION_UPDATE, "F"), ops[3])
    }

    @Test
    fun testApplyUpdates() {
        val list = mutableListOf("A", "B", "C")
        list.applyUpdates(
                ACTION_DELETE of "A",
                ACTION_DELETE of "C",
                ACTION_UPDATE of "E",
                ACTION_UPDATE of "F"
        )
        assertEquals(listOf("B", "E","F"), list)
    }

}