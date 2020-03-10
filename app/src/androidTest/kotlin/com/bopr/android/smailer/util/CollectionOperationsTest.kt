package com.bopr.android.smailer.util

import androidx.test.filters.SmallTest
import com.bopr.android.smailer.BaseTest
import com.bopr.android.smailer.util.CollectionOperation.Action.ACTION_ADD
import com.bopr.android.smailer.util.CollectionOperation.Action.ACTION_DELETE
import com.bopr.android.smailer.util.CollectionOperation.Companion.applyUpdates
import com.bopr.android.smailer.util.CollectionOperation.Companion.getUpdates
import org.junit.Assert.assertEquals
import org.junit.Test

@SmallTest
class CollectionOperationsTest : BaseTest() {

    @Test
    fun testGetUpdates() {
        val ops = listOf("A", "B", "C").getUpdates(listOf("A", "E", "F"))

        assertEquals(4, ops.size)
        assertEquals(CollectionOperation(ACTION_DELETE, "B"), ops[0])
        assertEquals(CollectionOperation(ACTION_DELETE, "C"), ops[1])
        assertEquals(CollectionOperation(ACTION_ADD, "E"), ops[2])
        assertEquals(CollectionOperation(ACTION_ADD, "F"), ops[3])
    }

    @Test
    fun testApplyUpdates() {
        val list = mutableListOf("A", "B", "C")
        list.applyUpdates(
                ACTION_DELETE of "A",
                ACTION_DELETE of "C",
                ACTION_ADD of "E",
                ACTION_ADD of "F"
        )
        assertEquals(listOf("B", "E","F"), list)
    }

}