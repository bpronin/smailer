package com.bopr.android.smailer

import androidx.test.filters.SmallTest
import com.bopr.android.smailer.data.Database
import com.bopr.android.smailer.data.Database.Companion.databaseName
import com.bopr.android.smailer.provider.telephony.PhoneEventInfo
import com.bopr.android.smailer.provider.telephony.PhoneEventInfo.Companion.STATE_IGNORED
import com.bopr.android.smailer.provider.telephony.PhoneEventInfo.Companion.STATE_PENDING
import com.bopr.android.smailer.provider.telephony.PhoneEventInfo.Companion.STATE_PROCESSED
import com.bopr.android.smailer.provider.telephony.PhoneEventInfo.Companion.STATUS_ACCEPTED
import com.bopr.android.smailer.util.GeoCoordinates
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

    @Before
    fun setUp() {
        databaseName = "test.sqlite"
        targetContext.deleteDatabase(databaseName)
        database = Database(targetContext)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testAdd() {
        val events = database.phoneEvents

        database.batch {
            events.add(PhoneEventInfo("1", true, 1000L, 0L, true, null, null, "Test 1", STATE_PENDING, "device", STATUS_ACCEPTED, isRead = false))
            events.add(PhoneEventInfo("2", false, 2000L, 0L, false, null, null, null, STATE_PENDING, "device", STATUS_ACCEPTED, isRead = false))
            events.add(PhoneEventInfo("3", true, 3000L, 0L, false, null, null, null, STATE_PENDING, "device", STATUS_ACCEPTED, isRead = false))
            events.add(PhoneEventInfo("4", false, 4000L, 0L, false, null, null, null, STATE_PENDING, "device", STATUS_ACCEPTED, isRead = false))
            events.add(PhoneEventInfo("5", true, 5000L, 0L, true, null, null, null, STATE_PENDING, "device", STATUS_ACCEPTED, isRead = false))
            events.add(PhoneEventInfo("6", true, 6000L, 7000L, false, null, null, "Test 1", STATE_PENDING, "device", STATUS_ACCEPTED, isRead = false))
            events.add(PhoneEventInfo("7", false, 7000L, 0L, false, null, null, "Test 2", STATE_PENDING, "device", STATUS_ACCEPTED, isRead = false))
            events.add(PhoneEventInfo("8", true, 8000L, 0L, false, null, null, "Test 3", STATE_PENDING, "device", STATUS_ACCEPTED, isRead = false))
            events.add(PhoneEventInfo("9", false, 9000L, 0L, false, null, null, "Test 4", STATE_PENDING, "device", STATUS_ACCEPTED, isRead = false))
            events.add(PhoneEventInfo("10", true, 10000L, 20000L, false, "SMS text", GeoCoordinates(10.5, 20.5), "Test 10", STATE_PENDING, "device", STATUS_ACCEPTED, isRead = false))
        }

        assertEquals(10, events.size)

        val event = events.first() /* descending order so it should be the last */

        assertEquals(STATE_PENDING, event.state)
        assertEquals("10", event.phone)
        assertTrue(event.isIncoming)
        assertEquals(10000L, event.startTime)
        assertEquals(20000L, event.endTime!!)
        assertFalse(event.isMissed)
        assertTrue(event.isSms)
        assertEquals(10.5, event.location!!.latitude, 0.1)
        assertEquals(20.5, event.location!!.longitude, 0.1)
        assertEquals("SMS text", event.text)
        assertEquals("Test 10", event.details)
    }

    @Test
    fun testUpdate() {
        database.phoneEvents.add(
            PhoneEventInfo(
                phone = "1",
                startTime = 0,
                acceptor = "device",
                state = STATE_PENDING,
                isRead = false
        )
        )

        assertEquals(1, database.phoneEvents.size)
        database.phoneEvents.first().run {
            assertEquals("1", phone)
            assertEquals(0, startTime)
            assertEquals("device", acceptor)
            assertEquals(STATE_PENDING, state)
            assertEquals(false, isRead)
        }

        assertTrue(database.phoneEvents.add(
            PhoneEventInfo(
                phone = "1",
                startTime = 0,
                acceptor = "device",
                state = STATE_PROCESSED,
                isRead = true
        )
        ))
        assertEquals(1, database.phoneEvents.size)
        database.phoneEvents.first().run {
            assertEquals("1", phone)
            assertEquals(0, startTime)
            assertEquals("device", acceptor)
            assertEquals(STATE_PROCESSED, state)
            assertEquals(true, isRead)
        }
    }

    @Test
    fun testUpdateGet() {
        var event = PhoneEventInfo(
                phone = "1",
                isIncoming = true,
                startTime = 1000L,
                endTime = 2000L,
                isMissed = false,
                text = "SMS text",
                location = GeoCoordinates(10.5, 20.5),
                details = "Test 1",
                state = STATE_PENDING,
                acceptor = "device",
                processStatus = STATUS_ACCEPTED,
                isRead = false)

        val events = database.phoneEvents

        events.add(event)

        assertEquals(1, events.size)

        event = events.first()

        assertEquals(STATE_PENDING, event.state)
        assertEquals("1", event.phone)
        assertTrue(event.isIncoming)
        assertEquals(1000L, event.startTime)
        assertEquals(2000L, event.endTime!!)
        assertFalse(event.isMissed)
        assertTrue(event.isSms)
        assertEquals(10.5, event.location!!.latitude, 0.1)
        assertEquals(20.5, event.location!!.longitude, 0.1)
        assertEquals("SMS text", event.text)
        assertEquals("Test 1", event.details)
        assertEquals(STATE_PENDING, event.state)

        event = event.copy(
                phone = "2",
                isIncoming = false,
                endTime = 3000L,
                isMissed = true,
                location = GeoCoordinates(11.5, 21.5),
                text = "New text",
                details = "New details"
        )
        events.add(event)

        assertEquals(1, events.size)

        event = events.first()

        assertEquals(STATE_PENDING, event.state)
        assertEquals("2", event.phone)
        assertFalse(event.isIncoming)
        assertEquals(3000L, event.endTime!!)
        assertTrue(event.isMissed)
        assertTrue(event.isSms)
        assertEquals(11.5, event.location!!.latitude, 0.1)
        assertEquals(21.5, event.location!!.longitude, 0.1)
        assertEquals("New text", event.text)
        assertEquals("New details", event.details)
    }

    @Test
    fun testClear() {
        val events = database.phoneEvents

        database.batch {
            events.add(PhoneEventInfo("1", true, 1000L, 2000L, false, "SMS text", GeoCoordinates(10.5, 20.5), "Test 1", STATE_PENDING, "device", STATUS_ACCEPTED, isRead = false))
            events.add(PhoneEventInfo("2", false, 2000L, 0L, false, null, null, null, STATE_PENDING, "device", STATUS_ACCEPTED, isRead = false))
            events.add(PhoneEventInfo("3", true, 3000L, 0L, false, null, null, null, STATE_PENDING, "device", STATUS_ACCEPTED, isRead = false))
            events.add(PhoneEventInfo("4", false, 4000L, 0L, false, null, null, null, STATE_PENDING, "device", STATUS_ACCEPTED, isRead = false))
            events.add(PhoneEventInfo("5", true, 5000L, 0L, true, null, null, null, STATE_PENDING, "device", STATUS_ACCEPTED, isRead = false))
            events.add(PhoneEventInfo("6", true, 6000L, 7000L, false, null, null, "Test 1", STATE_PENDING, "device", STATUS_ACCEPTED, isRead = false))
            events.add(PhoneEventInfo("7", false, 7000L, 0L, false, null, null, "Test 2", STATE_PENDING, "device", STATUS_ACCEPTED, isRead = false))
            events.add(PhoneEventInfo("8", true, 8000L, 0L, false, null, null, "Test 3", STATE_PENDING, "device", STATUS_ACCEPTED, isRead = false))
            events.add(PhoneEventInfo("9", false, 9000L, 0L, false, null, null, "Test 4", STATE_PENDING, "device", STATUS_ACCEPTED, isRead = false))
            events.add(PhoneEventInfo("10", true, 10000L, 0L, true, null, null, "Test 5", STATE_PENDING, "device", STATUS_ACCEPTED, isRead = false))
        }
        assertEquals(10, events.size)

        events.clear()

        assertEquals(0, events.size)
    }

    @Test
    fun testGetSetLocation() {
        database.lastLocation = GeoCoordinates(30.0, 60.0)
        val actual = database.lastLocation

        assertNotNull(actual)
        actual?.run {
            assertEquals(30.0, latitude, 0.1)
            assertEquals(60.0, longitude, 0.1)
        }
    }

    @Test
    fun testGetSetList() {
        database.phoneBlacklist.addAll(setOf("A", "B", "C"))

        assertEquals(setOf("A", "B", "C"), database.phoneBlacklist)
        assertFalse(database.phoneBlacklist.add("A"))
        assertEquals(setOf("A", "B", "C"), database.phoneBlacklist)
    }

    @Test
    fun testFilterPending() {
        val events = database.phoneEvents
        database.batch {
            events.add(PhoneEventInfo("1", true, 1000L, 0L, true, null, null, "Test 1", STATE_PROCESSED, "device", STATUS_ACCEPTED, isRead = false))
            events.add(PhoneEventInfo("2", false, 2000L, 0L, false, null, null, null, STATE_PROCESSED, "device", STATUS_ACCEPTED, isRead = false))
            events.add(PhoneEventInfo("3", true, 3000L, 0L, false, null, null, null, STATE_PROCESSED, "device", STATUS_ACCEPTED, isRead = false))
            events.add(PhoneEventInfo("4", false, 4000L, 0L, false, null, null, null, STATE_IGNORED, "device", STATUS_ACCEPTED, isRead = false))
            events.add(PhoneEventInfo("5", true, 5000L, 0L, true, null, null, null, STATE_IGNORED, "device", STATUS_ACCEPTED, isRead = false))
            events.add(PhoneEventInfo("6", true, 6000L, 7000L, false, null, null, "Test 1", STATE_PENDING, "device", STATUS_ACCEPTED, isRead = false))
            events.add(PhoneEventInfo("7", false, 7000L, 0L, false, null, null, "Test 2", STATE_PENDING, "device", STATUS_ACCEPTED, isRead = false))
            events.add(PhoneEventInfo("8", true, 8000L, 0L, false, null, null, "Test 3", STATE_PENDING, "device", STATUS_ACCEPTED, isRead = false))
            events.add(PhoneEventInfo("9", false, 9000L, 0L, false, null, null, "Test 4", STATE_PENDING, "device", STATUS_ACCEPTED, isRead = false))
            events.add(PhoneEventInfo("10", true, 10000L, 20000L, false, null, null, "Test 10", STATE_PENDING, "device", STATUS_ACCEPTED, isRead = false))
        }
        val pendingEvents = events.filterPending

        assertEquals(5, pendingEvents.size)

        val details = pendingEvents.first().details /* descending order so it should be the last */

        assertEquals("Test 10", details)
    }

    @Test
    fun testUnreadCount() {
        database.batch {
            phoneEvents.add(PhoneEventInfo(phone = "1", startTime = 0, acceptor = "device", isRead = true))
            phoneEvents.add(PhoneEventInfo(phone = "2", startTime = 1, acceptor = "device", isRead = false))
            phoneEvents.add(PhoneEventInfo(phone = "3", startTime = 2, acceptor = "device", isRead = false))
        }

        assertEquals(2, database.phoneEvents.unreadCount)
    }

    @Test
    fun testGetTransform() {
        val events = database.phoneEvents
        database.batch {
            events.add(PhoneEventInfo(phone = "1", startTime = 0, acceptor = "device"))
            events.add(PhoneEventInfo(phone = "2", startTime = 1, acceptor = "device"))
            events.add(PhoneEventInfo(phone = "3", startTime = 2, acceptor = "device"))
            events.add(PhoneEventInfo(phone = "4", startTime = 3, acceptor = "device"))
            events.add(PhoneEventInfo(phone = "5", startTime = 4, acceptor = "device"))
        }

        val phones = events.map { it.phone }

        assertEquals(5, phones.size)

        assertEquals("5", phones[0])
        assertEquals("3", phones[2])
        assertEquals("1", phones[4])
    }

    @Test
    fun testIteratorRemove() {
        database.batch {
            phoneEvents.add(PhoneEventInfo(phone = "1", startTime = 0, acceptor = "device"))
            phoneEvents.add(PhoneEventInfo(phone = "2", startTime = 1, acceptor = "device"))
            phoneEvents.add(PhoneEventInfo(phone = "3", startTime = 2, acceptor = "device"))
        }

        val iterator = database.phoneEvents.iterator()
        while (iterator.hasNext()) {
            if (iterator.next().phone == "1") {
                iterator.remove()
            }
        }

        assertEquals(2, database.phoneEvents.size)
        assertEquals("3", database.phoneEvents.first().phone)
        assertEquals("2", database.phoneEvents.last().phone)
    }

    @Test
    fun testContains() {
        database.batch {
            phoneEvents.add(PhoneEventInfo(phone = "1", startTime = 0, acceptor = "device"))
            phoneEvents.add(PhoneEventInfo(phone = "2", startTime = 1, acceptor = "device"))
            phoneEvents.add(PhoneEventInfo(phone = "3", startTime = 2, acceptor = "device"))
        }

        assertTrue(database.phoneEvents.contains(PhoneEventInfo(phone = "1", startTime = 0, acceptor = "device")))
        assertTrue(database.phoneEvents.containsAll(listOf(
                PhoneEventInfo(phone = "1", startTime = 0, acceptor = "device"),
                PhoneEventInfo(phone = "3", startTime = 2, acceptor = "device")
        )))
    }

    @Test
    fun testAddAll() {
        database.phoneEvents.addAll(listOf(
                PhoneEventInfo(phone = "1", startTime = 0, acceptor = "device"),
                PhoneEventInfo(phone = "2", startTime = 1, acceptor = "device"),
                PhoneEventInfo(phone = "3", startTime = 2, acceptor = "device")
        ))

        assertEquals(3, database.phoneEvents.size)
        assertEquals("3", database.phoneEvents.first().phone)
        assertEquals("1", database.phoneEvents.last().phone)
    }

    @Test
    fun testRemoveAll() {
        database.phoneEvents.addAll(listOf(
                PhoneEventInfo(phone = "1", startTime = 0, acceptor = "device"),
                PhoneEventInfo(phone = "2", startTime = 1, acceptor = "device"),
                PhoneEventInfo(phone = "3", startTime = 2, acceptor = "device")
        ))

        database.phoneEvents.removeAll(listOf(
                PhoneEventInfo(phone = "1", startTime = 0, acceptor = "device"),
                PhoneEventInfo(phone = "3", startTime = 2, acceptor = "device"),
                PhoneEventInfo(phone = "33", startTime = 33, acceptor = "device")
        ))

        assertEquals(1, database.phoneEvents.size)
        assertEquals("2", database.phoneEvents.first().phone)
    }

    @Test
    fun testRetainAll() {
        database.phoneEvents.addAll(listOf(
                PhoneEventInfo(phone = "1", startTime = 0, acceptor = "device"),
                PhoneEventInfo(phone = "2", startTime = 1, acceptor = "device"),
                PhoneEventInfo(phone = "3", startTime = 2, acceptor = "device")
        ))

        database.phoneEvents.retainAll(listOf(
                PhoneEventInfo(phone = "1", startTime = 0, acceptor = "device"),
                PhoneEventInfo(phone = "3", startTime = 2, acceptor = "device"),
                PhoneEventInfo(phone = "33", startTime = 33, acceptor = "device")
        ))

        assertEquals(2, database.phoneEvents.size)
        assertEquals("3", database.phoneEvents.first().phone)
        assertEquals("1", database.phoneEvents.last().phone)
    }

    @Test
    fun testReplaceAll() {
        database.phoneEvents.addAll(listOf(
                PhoneEventInfo(phone = "1", startTime = 0, acceptor = "device"),
                PhoneEventInfo(phone = "2", startTime = 1, acceptor = "device"),
                PhoneEventInfo(phone = "3", startTime = 2, acceptor = "device")
        ))

        database.phoneEvents.replaceAll(listOf(
                PhoneEventInfo(phone = "11", startTime = 11, acceptor = "device"),
                PhoneEventInfo(phone = "22", startTime = 22, acceptor = "device")
        ))

        assertEquals(2, database.phoneEvents.size)
        assertEquals("22", database.phoneEvents.first().phone)
        assertEquals("11", database.phoneEvents.last().phone)
    }

}