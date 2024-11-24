package com.bopr.android.smailer.data

import androidx.test.filters.SmallTest
import com.bopr.android.smailer.BaseTest
import com.bopr.android.smailer.data.Database.Companion.database
import com.bopr.android.smailer.data.Database.Companion.databaseName
import com.bopr.android.smailer.messenger.Event
import com.bopr.android.smailer.messenger.ProcessState.Companion.STATE_IGNORED
import com.bopr.android.smailer.messenger.ProcessState.Companion.STATE_PENDING
import com.bopr.android.smailer.messenger.ProcessState.Companion.STATE_PROCESSED
import com.bopr.android.smailer.provider.telephony.PhoneCallInfo
import com.bopr.android.smailer.util.GeoLocation
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * [Database] class tester.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
@SmallTest
class DatabaseTest : BaseTest() {

    private lateinit var database: Database
    private val defaultPayload = PhoneCallInfo(startTime = 0, phone = "1")

    @Before
    fun setUp() {
        databaseName = "test.sqlite"
        targetContext.deleteDatabase(databaseName)
        database = targetContext.database
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testAdd() {
        database.batch {
            events.run {
                add(Event(timestamp = 1, payload = defaultPayload))
                add(Event(timestamp = 2, payload = defaultPayload))
                add(Event(timestamp = 3, payload = defaultPayload))
            }
        }

        assertEquals(3, database.events.size)

        /* descending order so "3" should be the last */
        database.events.first().run {
            assertEquals(3, timestamp)
            assertEquals(STATE_PENDING, processState)
        }
    }

    @Test
    fun testUpdate() {
        database.events.add(
            Event(
                timestamp = 0,
                target = "1",
                payload = defaultPayload
            )
        )

        assertEquals(1, database.events.size)

        database.events.first().run {
            assertEquals(STATE_PENDING, processState)
            assertFalse(isRead)
            assertEquals(defaultPayload, payload)
        }

        assertTrue(
            database.events.add(
                Event(
                    timestamp = 0,
                    target = "1",
                    processState = STATE_PROCESSED,
                    isRead = true,
                    payload = defaultPayload.copy(
                        startTime = 2,
                        phone = "2"
                    )
                )
            )
        )

        assertEquals(1, database.events.size)

        database.events.first().run {
            assertEquals(STATE_PROCESSED, processState)
            assertTrue(isRead)
            (payload as PhoneCallInfo).run {
                assertEquals(2, startTime)
                assertEquals("2", phone)
            }
        }
    }

    @Test
    fun testUpdateGet() {
        database.events.add(
            Event(
                timestamp = 0,
                location = GeoLocation(10.5, 20.5),
                payload = PhoneCallInfo(
                    startTime = 1000L,
                    phone = "1",
                    isIncoming = true,
                    endTime = 2000L,
                    text = "SMS text"
                )
            )
        )

        assertEquals(1, database.events.size)

        var event = database.events.first().apply {
            assertEquals(0, timestamp)
            assertEquals(10.5, location!!.latitude, 0.1)
            assertEquals(20.5, location!!.longitude, 0.1)
            assertEquals(STATE_PENDING, processState)

            (payload as PhoneCallInfo).run {
                assertEquals("1", phone)
                assertTrue(isIncoming)
                assertEquals(1000L, startTime)
                assertEquals(2000L, endTime!!)
                assertFalse(isMissed)
                assertTrue(isSms)
                assertEquals("SMS text", text)
            }
        }

        database.events.add(
            event.copy(
                location = GeoLocation(11.5, 21.5),
                processState = STATE_IGNORED
            )
        )

        assertEquals(1, database.events.size)

        database.events.first(). run {
            assertEquals(0, timestamp)
            assertEquals(11.5, location!!.latitude, 0.1)
            assertEquals(21.5, location!!.longitude, 0.1)
            assertEquals(STATE_IGNORED, processState)

            (payload as PhoneCallInfo).run {
                assertEquals("1", phone)
                assertTrue(isIncoming)
                assertEquals(1000L, startTime)
                assertEquals(2000L, endTime!!)
                assertFalse(isMissed)
                assertTrue(isSms)
                assertEquals("SMS text", text)
            }
        }
    }

    @Test
    fun testClear() {
        database.batch {
            events.apply {
                add(Event(timestamp = 1, payload = defaultPayload))
                add(Event(timestamp = 2, payload = defaultPayload))
                add(Event(timestamp = 3, payload = defaultPayload))
            }
        }

        assertEquals(3, database.events.size)

        database.events.clear()

        assertEquals(0, database.events.size)
    }

    @Test
    fun testGetSetList() {
        database.phoneBlacklist.addAll(setOf("A", "B", "C"))

        assertEquals(setOf("A", "B", "C"), database.phoneBlacklist)
        assertTrue(database.phoneBlacklist.add("A"))
        assertEquals(setOf("A", "B", "C"), database.phoneBlacklist)
    }

    @Test
    fun testFilterPending() {
        database.batch {
            events.apply {
                add(Event(payload = defaultPayload))
                add(Event(payload = defaultPayload, processState = STATE_PROCESSED))
                add(Event(payload = defaultPayload, processState = STATE_IGNORED))
            }
        }

        assertEquals(1, database.events.pending.size)
    }

    @Test
    fun testUnreadCount() {
        database.batch {
            events.apply {
                add(Event(payload = defaultPayload))
                add(Event(payload = defaultPayload))
                add(Event(payload = defaultPayload, isRead = true))
            }
        }

        assertEquals(2, database.events.unreadCount)
    }

    @Test
    fun testGetTransform() {
        database.batch {
            events.apply {
                add(Event(timestamp = 1, payload = defaultPayload))
                add(Event(timestamp = 2, payload = defaultPayload))
                add(Event(timestamp = 3, payload = defaultPayload))
            }
        }

        val timestamps = database.events.map { it.timestamp }

        assertEquals(3, timestamps.size)
        assertEquals(3, timestamps[0])
        assertEquals(2, timestamps[1])
        assertEquals(1, timestamps[2])
    }

    @Test
    fun testIteratorRemove() {
        database.batch {
            events.apply {
                add(Event(timestamp = 1, payload = defaultPayload))
                add(Event(timestamp = 2, payload = defaultPayload))
                add(Event(timestamp = 3, payload = defaultPayload))
            }
        }

        val iterator = database.events.iterator()
        while (iterator.hasNext()) {
            if (iterator.next().timestamp == 2L) {
                iterator.remove()
            }
        }

        assertEquals(2, database.events.size)
        assertEquals(3, database.events.first().timestamp)
        assertEquals(1, database.events.last().timestamp)
    }

    @Test
    fun testContains() {
        database.batch {
            events.apply {
                add(Event(timestamp = 1, payload = defaultPayload))
                add(Event(timestamp = 2, payload = defaultPayload))
                add(Event(timestamp = 3, payload = defaultPayload))
            }
        }

        assertTrue(
            database.events.contains(Event(timestamp = 2, payload = defaultPayload))
        )
        assertTrue(
            database.events.containsAll(
                listOf(
                    Event(timestamp = 1, payload = defaultPayload),
                    Event(timestamp = 2, payload = defaultPayload),
                    Event(timestamp = 3, payload = defaultPayload)
                )
            )
        )
    }

    @Test
    fun testAddAll() {
        database.events.addAll(
            listOf(
                Event(timestamp = 1, payload = defaultPayload),
                Event(timestamp = 2, payload = defaultPayload),
                Event(timestamp = 3, payload = defaultPayload)
            )
        )

        assertEquals(3, database.events.size)
        assertEquals(3, database.events.first().timestamp)
        assertEquals(1, database.events.last().timestamp)
    }

    @Test
    fun testRemoveAll() {
        with(database) {
            events.addAll(
                listOf(
                    Event(timestamp = 1, payload = defaultPayload),
                    Event(timestamp = 2, payload = defaultPayload),
                    Event(timestamp = 3, payload = defaultPayload)
                )
            )

            events.removeAll(
                listOf(
                    Event(timestamp = 1, payload = defaultPayload),
                    Event(timestamp = 2, payload = defaultPayload),
                    Event(timestamp = 4, payload = defaultPayload)
                )
            )
        }

        assertEquals(1, database.events.size)
        assertEquals(3, database.events.first().timestamp)
    }

    @Test
    fun testRetainAll() {
        database.events.addAll(
            listOf(
                Event(timestamp = 1, payload = defaultPayload),
                Event(timestamp = 2, payload = defaultPayload),
                Event(timestamp = 3, payload = defaultPayload)
            )
        )

        database.events.retainAll(
            listOf(
                Event(timestamp = 1, payload = defaultPayload),
                Event(timestamp = 2, payload = defaultPayload),
                Event(timestamp = 4, payload = defaultPayload)
            )
        )

        assertEquals(2, database.events.size)
        assertEquals(2, database.events.first().timestamp)
        assertEquals(1, database.events.last().timestamp)
    }

    @Test
    fun testReplaceAll() {
        database.events.addAll(
            listOf(
                Event(timestamp = 1, payload = defaultPayload),
                Event(timestamp = 2, payload = defaultPayload),
                Event(timestamp = 3, payload = defaultPayload)
            )
        )

        assertEquals(3, database.events.size)
        assertEquals(3, database.events.first().timestamp)
        assertEquals(1, database.events.last().timestamp)

        database.events.replaceAll(
            listOf(
                Event(timestamp = 3, payload = defaultPayload),
                Event(timestamp = 4, payload = defaultPayload)
            )
        )

        assertEquals(2, database.events.size)
        assertEquals(4, database.events.first().timestamp)
        assertEquals(3, database.events.last().timestamp)
    }

}