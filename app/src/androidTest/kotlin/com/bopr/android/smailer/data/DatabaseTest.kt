package com.bopr.android.smailer.data

import androidx.test.filters.SmallTest
import com.bopr.android.smailer.BaseTest
import com.bopr.android.smailer.data.Database.Companion.database
import com.bopr.android.smailer.messenger.Event
import com.bopr.android.smailer.messenger.ProcessState.Companion.STATE_IGNORED
import com.bopr.android.smailer.messenger.ProcessState.Companion.STATE_PENDING
import com.bopr.android.smailer.messenger.ProcessState.Companion.STATE_PROCESSED
import com.bopr.android.smailer.provider.telephony.PhoneCallData
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * [Database] class tester.
 *
 * @author Boris Pronin ([boris280471@gmail.com](mailto:boris280471@gmail.com))
 */
@SmallTest
class DatabaseTest : BaseTest() {

    private val database = targetContext.database
    private val defaultPayload = PhoneCallData(startTime = 0, phone = "1")

    @Before
    fun setUp() {
        database.destroy()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testInsert() {
        with(database.events) {
            assertTrue(insert(Event(time = 1, payload = defaultPayload)))
            assertTrue(insert(Event(time = 2, payload = defaultPayload)))
            assertTrue(insert(Event(time = 3, payload = defaultPayload)))
            drain().apply {
                assertEquals(3, size)
                assertEquals(3, first().time)
                assertEquals(STATE_PENDING, first().processState)
            }
        }
    }

    @Test
    fun testReinsert() {
        with(database.events) {
            insert(Event(time = 0, target = "1", payload = defaultPayload))
            assertFalse(
                insert(Event(time = 0, target = "1", payload = defaultPayload))
            )
            assertEquals(1, size)
        }
    }

    @Test
    fun testUpdate() {
        with(database.events) {
            insert(Event(time = 0, target = "1", isRead = false, payload = defaultPayload))
            assertTrue(
                updateRead(Event(time = 0, target = "1", isRead = true, payload = defaultPayload))
            )
            assertEquals(1, size)
            assertTrue(drain().first().isRead)
        }
    }

    @Test
    fun testInsertSet() {
        val elements = setOf("A", "B", "C")
        assertTrue(database.phoneBlacklist.insert(elements))
        assertEquals(elements, database.phoneBlacklist.drain())
    }

    @Test
    fun testReplaceAll() {
        with(database.events) {
            insert(
                setOf(
                    Event(time = 1, payload = defaultPayload),
                    Event(time = 2, payload = defaultPayload),
                    Event(time = 3, payload = defaultPayload)
                )
            )

            replaceAll(
                setOf(
                    Event(time = 3, payload = defaultPayload),
                    Event(time = 4, payload = defaultPayload)
                )
            )

            drain().apply {
                assertEquals(2, size)
                assertEquals(4, first().time)
                assertEquals(3, last().time)
            }
        }
    }

    @Test
    fun testClear() {
        with(database.events) {
            insert(Event(time = 0, target = "1", payload = defaultPayload))
            insert(Event(time = 1, target = "1", payload = defaultPayload))
            insert(Event(time = 2, target = "1", payload = defaultPayload))

            clear()
     
            assertEquals(0, size)
        }
    }

    @Test
    fun testDeleteSet() {
        with(database.events) {
            insert(
                setOf(
                    Event(time = 1, payload = defaultPayload),
                    Event(time = 2, payload = defaultPayload),
                    Event(time = 3, payload = defaultPayload)
                )
            )

            delete(
                setOf(
                    Event(time = 1, payload = defaultPayload),
                    Event(time = 2, payload = defaultPayload),
                    Event(time = 4, payload = defaultPayload)
                )
            )

            drain().apply {
                assertEquals(1, size)
                assertEquals(3, first().time)
            }
        }
    }

    @Test
    fun testFilterPending() {
        database.batchUpdate {
            events.apply {
                insert(Event(payload = defaultPayload))
                insert(Event(payload = defaultPayload, processState = STATE_PROCESSED))
                insert(Event(payload = defaultPayload, processState = STATE_IGNORED))
            }
        }

        assertEquals(1, database.events.drainPending().size)
    }

    @Test
    fun testUnreadCount() {
        database.batchUpdate {
            events.apply {
                insert(Event(payload = defaultPayload))
                insert(Event(payload = defaultPayload))
                insert(Event(payload = defaultPayload, isRead = true))
            }
        }

        assertEquals(2, database.events.getUnreadCount())
    }
    

}