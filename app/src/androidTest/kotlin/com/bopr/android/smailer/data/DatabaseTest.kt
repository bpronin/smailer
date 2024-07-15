package com.bopr.android.smailer.data

import androidx.test.filters.SmallTest
import com.bopr.android.smailer.BaseTest
import com.bopr.android.smailer.provider.telephony.PhoneEventData
import com.bopr.android.smailer.util.GeoCoordinates
import org.junit.After
import org.junit.Assert
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
        Database.databaseName = "test.sqlite"
        targetContext.deleteDatabase(Database.databaseName)
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
            events.add(
                PhoneEventData(
                    "1",
                    true,
                    1000L,
                    0L,
                    true,
                    null,
                    null,
                    "Test 1",
                    PhoneEventData.STATE_PENDING,
                    "device",
                    PhoneEventData.STATUS_ACCEPTED,
                    isRead = false
                )
            )
            events.add(
                PhoneEventData(
                    "2",
                    false,
                    2000L,
                    0L,
                    false,
                    null,
                    null,
                    null,
                    PhoneEventData.STATE_PENDING,
                    "device",
                    PhoneEventData.STATUS_ACCEPTED,
                    isRead = false
                )
            )
            events.add(
                PhoneEventData(
                    "3",
                    true,
                    3000L,
                    0L,
                    false,
                    null,
                    null,
                    null,
                    PhoneEventData.STATE_PENDING,
                    "device",
                    PhoneEventData.STATUS_ACCEPTED,
                    isRead = false
                )
            )
            events.add(
                PhoneEventData(
                    "4",
                    false,
                    4000L,
                    0L,
                    false,
                    null,
                    null,
                    null,
                    PhoneEventData.STATE_PENDING,
                    "device",
                    PhoneEventData.STATUS_ACCEPTED,
                    isRead = false
                )
            )
            events.add(
                PhoneEventData(
                    "5",
                    true,
                    5000L,
                    0L,
                    true,
                    null,
                    null,
                    null,
                    PhoneEventData.STATE_PENDING,
                    "device",
                    PhoneEventData.STATUS_ACCEPTED,
                    isRead = false
                )
            )
            events.add(
                PhoneEventData(
                    "6",
                    true,
                    6000L,
                    7000L,
                    false,
                    null,
                    null,
                    "Test 1",
                    PhoneEventData.STATE_PENDING,
                    "device",
                    PhoneEventData.STATUS_ACCEPTED,
                    isRead = false
                )
            )
            events.add(
                PhoneEventData(
                    "7",
                    false,
                    7000L,
                    0L,
                    false,
                    null,
                    null,
                    "Test 2",
                    PhoneEventData.STATE_PENDING,
                    "device",
                    PhoneEventData.STATUS_ACCEPTED,
                    isRead = false
                )
            )
            events.add(
                PhoneEventData(
                    "8",
                    true,
                    8000L,
                    0L,
                    false,
                    null,
                    null,
                    "Test 3",
                    PhoneEventData.STATE_PENDING,
                    "device",
                    PhoneEventData.STATUS_ACCEPTED,
                    isRead = false
                )
            )
            events.add(
                PhoneEventData(
                    "9",
                    false,
                    9000L,
                    0L,
                    false,
                    null,
                    null,
                    "Test 4",
                    PhoneEventData.STATE_PENDING,
                    "device",
                    PhoneEventData.STATUS_ACCEPTED,
                    isRead = false
                )
            )
            events.add(
                PhoneEventData(
                    "10",
                    true,
                    10000L,
                    20000L,
                    false,
                    "SMS text",
                    GeoCoordinates(10.5, 20.5),
                    "Test 10",
                    PhoneEventData.STATE_PENDING,
                    "device",
                    PhoneEventData.STATUS_ACCEPTED,
                    isRead = false
                )
            )
        }

        Assert.assertEquals(10, events.size)

        val event = events.first() /* descending order so it should be the last */

        Assert.assertEquals(PhoneEventData.STATE_PENDING, event.state)
        Assert.assertEquals("10", event.phone)
        Assert.assertTrue(event.isIncoming)
        Assert.assertEquals(10000L, event.startTime)
        Assert.assertEquals(20000L, event.endTime!!)
        Assert.assertFalse(event.isMissed)
        Assert.assertTrue(event.isSms)
        Assert.assertEquals(10.5, event.location!!.latitude, 0.1)
        Assert.assertEquals(20.5, event.location!!.longitude, 0.1)
        Assert.assertEquals("SMS text", event.text)
        Assert.assertEquals("Test 10", event.details)
    }

    @Test
    fun testUpdate() {
        database.phoneEvents.add(
            PhoneEventData(
                phone = "1",
                startTime = 0,
                acceptor = "device",
                state = PhoneEventData.STATE_PENDING,
                isRead = false
            )
        )

        Assert.assertEquals(1, database.phoneEvents.size)
        database.phoneEvents.first().run {
            Assert.assertEquals("1", phone)
            Assert.assertEquals(0, startTime)
            Assert.assertEquals("device", acceptor)
            Assert.assertEquals(PhoneEventData.STATE_PENDING, state)
            Assert.assertEquals(false, isRead)
        }

        Assert.assertTrue(
            database.phoneEvents.add(
                PhoneEventData(
                    phone = "1",
                    startTime = 0,
                    acceptor = "device",
                    state = PhoneEventData.STATE_PROCESSED,
                    isRead = true
                )
            )
        )
        Assert.assertEquals(1, database.phoneEvents.size)
        database.phoneEvents.first().run {
            Assert.assertEquals("1", phone)
            Assert.assertEquals(0, startTime)
            Assert.assertEquals("device", acceptor)
            Assert.assertEquals(PhoneEventData.STATE_PROCESSED, state)
            Assert.assertEquals(true, isRead)
        }
    }

    @Test
    fun testUpdateGet() {
        var event = PhoneEventData(
            phone = "1",
            isIncoming = true,
            startTime = 1000L,
            endTime = 2000L,
            isMissed = false,
            text = "SMS text",
            location = GeoCoordinates(10.5, 20.5),
            details = "Test 1",
            state = PhoneEventData.STATE_PENDING,
            acceptor = "device",
            processStatus = PhoneEventData.STATUS_ACCEPTED,
            isRead = false
        )

        val events = database.phoneEvents

        events.add(event)

        Assert.assertEquals(1, events.size)

        event = events.first()

        Assert.assertEquals(PhoneEventData.STATE_PENDING, event.state)
        Assert.assertEquals("1", event.phone)
        Assert.assertTrue(event.isIncoming)
        Assert.assertEquals(1000L, event.startTime)
        Assert.assertEquals(2000L, event.endTime!!)
        Assert.assertFalse(event.isMissed)
        Assert.assertTrue(event.isSms)
        Assert.assertEquals(10.5, event.location!!.latitude, 0.1)
        Assert.assertEquals(20.5, event.location!!.longitude, 0.1)
        Assert.assertEquals("SMS text", event.text)
        Assert.assertEquals("Test 1", event.details)
        Assert.assertEquals(PhoneEventData.STATE_PENDING, event.state)

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

        Assert.assertEquals(1, events.size)

        event = events.first()

        Assert.assertEquals(PhoneEventData.STATE_PENDING, event.state)
        Assert.assertEquals("2", event.phone)
        Assert.assertFalse(event.isIncoming)
        Assert.assertEquals(3000L, event.endTime!!)
        Assert.assertTrue(event.isMissed)
        Assert.assertTrue(event.isSms)
        Assert.assertEquals(11.5, event.location!!.latitude, 0.1)
        Assert.assertEquals(21.5, event.location!!.longitude, 0.1)
        Assert.assertEquals("New text", event.text)
        Assert.assertEquals("New details", event.details)
    }

    @Test
    fun testClear() {
        val events = database.phoneEvents

        database.batch {
            events.add(
                PhoneEventData(
                    "1",
                    true,
                    1000L,
                    2000L,
                    false,
                    "SMS text",
                    GeoCoordinates(10.5, 20.5),
                    "Test 1",
                    PhoneEventData.STATE_PENDING,
                    "device",
                    PhoneEventData.STATUS_ACCEPTED,
                    isRead = false
                )
            )
            events.add(
                PhoneEventData(
                    "2",
                    false,
                    2000L,
                    0L,
                    false,
                    null,
                    null,
                    null,
                    PhoneEventData.STATE_PENDING,
                    "device",
                    PhoneEventData.STATUS_ACCEPTED,
                    isRead = false
                )
            )
            events.add(
                PhoneEventData(
                    "3",
                    true,
                    3000L,
                    0L,
                    false,
                    null,
                    null,
                    null,
                    PhoneEventData.STATE_PENDING,
                    "device",
                    PhoneEventData.STATUS_ACCEPTED,
                    isRead = false
                )
            )
            events.add(
                PhoneEventData(
                    "4",
                    false,
                    4000L,
                    0L,
                    false,
                    null,
                    null,
                    null,
                    PhoneEventData.STATE_PENDING,
                    "device",
                    PhoneEventData.STATUS_ACCEPTED,
                    isRead = false
                )
            )
            events.add(
                PhoneEventData(
                    "5",
                    true,
                    5000L,
                    0L,
                    true,
                    null,
                    null,
                    null,
                    PhoneEventData.STATE_PENDING,
                    "device",
                    PhoneEventData.STATUS_ACCEPTED,
                    isRead = false
                )
            )
            events.add(
                PhoneEventData(
                    "6",
                    true,
                    6000L,
                    7000L,
                    false,
                    null,
                    null,
                    "Test 1",
                    PhoneEventData.STATE_PENDING,
                    "device",
                    PhoneEventData.STATUS_ACCEPTED,
                    isRead = false
                )
            )
            events.add(
                PhoneEventData(
                    "7",
                    false,
                    7000L,
                    0L,
                    false,
                    null,
                    null,
                    "Test 2",
                    PhoneEventData.STATE_PENDING,
                    "device",
                    PhoneEventData.STATUS_ACCEPTED,
                    isRead = false
                )
            )
            events.add(
                PhoneEventData(
                    "8",
                    true,
                    8000L,
                    0L,
                    false,
                    null,
                    null,
                    "Test 3",
                    PhoneEventData.STATE_PENDING,
                    "device",
                    PhoneEventData.STATUS_ACCEPTED,
                    isRead = false
                )
            )
            events.add(
                PhoneEventData(
                    "9",
                    false,
                    9000L,
                    0L,
                    false,
                    null,
                    null,
                    "Test 4",
                    PhoneEventData.STATE_PENDING,
                    "device",
                    PhoneEventData.STATUS_ACCEPTED,
                    isRead = false
                )
            )
            events.add(
                PhoneEventData(
                    "10",
                    true,
                    10000L,
                    0L,
                    true,
                    null,
                    null,
                    "Test 5",
                    PhoneEventData.STATE_PENDING,
                    "device",
                    PhoneEventData.STATUS_ACCEPTED,
                    isRead = false
                )
            )
        }
        Assert.assertEquals(10, events.size)

        events.clear()

        Assert.assertEquals(0, events.size)
    }

    @Test
    fun testGetSetLocation() {
        database.lastLocation = GeoCoordinates(30.0, 60.0)
        val actual = database.lastLocation

        Assert.assertNotNull(actual)
        actual?.run {
            Assert.assertEquals(30.0, latitude, 0.1)
            Assert.assertEquals(60.0, longitude, 0.1)
        }
    }

    @Test
    fun testGetSetList() {
        database.phoneBlacklist.addAll(setOf("A", "B", "C"))

        Assert.assertEquals(setOf("A", "B", "C"), database.phoneBlacklist)
        Assert.assertFalse(database.phoneBlacklist.add("A"))
        Assert.assertEquals(setOf("A", "B", "C"), database.phoneBlacklist)
    }

    @Test
    fun testFilterPending() {
        val events = database.phoneEvents
        database.batch {
            events.add(
                PhoneEventData(
                    "1",
                    true,
                    1000L,
                    0L,
                    true,
                    null,
                    null,
                    "Test 1",
                    PhoneEventData.STATE_PROCESSED,
                    "device",
                    PhoneEventData.STATUS_ACCEPTED,
                    isRead = false
                )
            )
            events.add(
                PhoneEventData(
                    "2",
                    false,
                    2000L,
                    0L,
                    false,
                    null,
                    null,
                    null,
                    PhoneEventData.STATE_PROCESSED,
                    "device",
                    PhoneEventData.STATUS_ACCEPTED,
                    isRead = false
                )
            )
            events.add(
                PhoneEventData(
                    "3",
                    true,
                    3000L,
                    0L,
                    false,
                    null,
                    null,
                    null,
                    PhoneEventData.STATE_PROCESSED,
                    "device",
                    PhoneEventData.STATUS_ACCEPTED,
                    isRead = false
                )
            )
            events.add(
                PhoneEventData(
                    "4",
                    false,
                    4000L,
                    0L,
                    false,
                    null,
                    null,
                    null,
                    PhoneEventData.STATE_IGNORED,
                    "device",
                    PhoneEventData.STATUS_ACCEPTED,
                    isRead = false
                )
            )
            events.add(
                PhoneEventData(
                    "5",
                    true,
                    5000L,
                    0L,
                    true,
                    null,
                    null,
                    null,
                    PhoneEventData.STATE_IGNORED,
                    "device",
                    PhoneEventData.STATUS_ACCEPTED,
                    isRead = false
                )
            )
            events.add(
                PhoneEventData(
                    "6",
                    true,
                    6000L,
                    7000L,
                    false,
                    null,
                    null,
                    "Test 1",
                    PhoneEventData.STATE_PENDING,
                    "device",
                    PhoneEventData.STATUS_ACCEPTED,
                    isRead = false
                )
            )
            events.add(
                PhoneEventData(
                    "7",
                    false,
                    7000L,
                    0L,
                    false,
                    null,
                    null,
                    "Test 2",
                    PhoneEventData.STATE_PENDING,
                    "device",
                    PhoneEventData.STATUS_ACCEPTED,
                    isRead = false
                )
            )
            events.add(
                PhoneEventData(
                    "8",
                    true,
                    8000L,
                    0L,
                    false,
                    null,
                    null,
                    "Test 3",
                    PhoneEventData.STATE_PENDING,
                    "device",
                    PhoneEventData.STATUS_ACCEPTED,
                    isRead = false
                )
            )
            events.add(
                PhoneEventData(
                    "9",
                    false,
                    9000L,
                    0L,
                    false,
                    null,
                    null,
                    "Test 4",
                    PhoneEventData.STATE_PENDING,
                    "device",
                    PhoneEventData.STATUS_ACCEPTED,
                    isRead = false
                )
            )
            events.add(
                PhoneEventData(
                    "10",
                    true,
                    10000L,
                    20000L,
                    false,
                    null,
                    null,
                    "Test 10",
                    PhoneEventData.STATE_PENDING,
                    "device",
                    PhoneEventData.STATUS_ACCEPTED,
                    isRead = false
                )
            )
        }
        val pendingEvents = events.filterPending

        Assert.assertEquals(5, pendingEvents.size)

        val details = pendingEvents.first().details /* descending order so it should be the last */

        Assert.assertEquals("Test 10", details)
    }

    @Test
    fun testUnreadCount() {
        database.batch {
            phoneEvents.add(
                PhoneEventData(
                    phone = "1",
                    startTime = 0,
                    acceptor = "device",
                    isRead = true
                )
            )
            phoneEvents.add(
                PhoneEventData(
                    phone = "2",
                    startTime = 1,
                    acceptor = "device",
                    isRead = false
                )
            )
            phoneEvents.add(
                PhoneEventData(
                    phone = "3",
                    startTime = 2,
                    acceptor = "device",
                    isRead = false
                )
            )
        }

        Assert.assertEquals(2, database.phoneEvents.unreadCount)
    }

    @Test
    fun testGetTransform() {
        val events = database.phoneEvents
        database.batch {
            events.add(PhoneEventData(phone = "1", startTime = 0, acceptor = "device"))
            events.add(PhoneEventData(phone = "2", startTime = 1, acceptor = "device"))
            events.add(PhoneEventData(phone = "3", startTime = 2, acceptor = "device"))
            events.add(PhoneEventData(phone = "4", startTime = 3, acceptor = "device"))
            events.add(PhoneEventData(phone = "5", startTime = 4, acceptor = "device"))
        }

        val phones = events.map { it.phone }

        Assert.assertEquals(5, phones.size)

        Assert.assertEquals("5", phones[0])
        Assert.assertEquals("3", phones[2])
        Assert.assertEquals("1", phones[4])
    }

    @Test
    fun testIteratorRemove() {
        database.batch {
            phoneEvents.add(PhoneEventData(phone = "1", startTime = 0, acceptor = "device"))
            phoneEvents.add(PhoneEventData(phone = "2", startTime = 1, acceptor = "device"))
            phoneEvents.add(PhoneEventData(phone = "3", startTime = 2, acceptor = "device"))
        }

        val iterator = database.phoneEvents.iterator()
        while (iterator.hasNext()) {
            if (iterator.next().phone == "1") {
                iterator.remove()
            }
        }

        Assert.assertEquals(2, database.phoneEvents.size)
        Assert.assertEquals("3", database.phoneEvents.first().phone)
        Assert.assertEquals("2", database.phoneEvents.last().phone)
    }

    @Test
    fun testContains() {
        database.batch {
            phoneEvents.add(PhoneEventData(phone = "1", startTime = 0, acceptor = "device"))
            phoneEvents.add(PhoneEventData(phone = "2", startTime = 1, acceptor = "device"))
            phoneEvents.add(PhoneEventData(phone = "3", startTime = 2, acceptor = "device"))
        }

        Assert.assertTrue(
            database.phoneEvents.contains(
                PhoneEventData(
                    phone = "1",
                    startTime = 0,
                    acceptor = "device"
                )
            )
        )
        Assert.assertTrue(
            database.phoneEvents.containsAll(
                listOf(
                    PhoneEventData(phone = "1", startTime = 0, acceptor = "device"),
                    PhoneEventData(phone = "3", startTime = 2, acceptor = "device")
                )
            )
        )
    }

    @Test
    fun testAddAll() {
        database.phoneEvents.addAll(listOf(
            PhoneEventData(phone = "1", startTime = 0, acceptor = "device"),
            PhoneEventData(phone = "2", startTime = 1, acceptor = "device"),
            PhoneEventData(phone = "3", startTime = 2, acceptor = "device")
        ))

        Assert.assertEquals(3, database.phoneEvents.size)
        Assert.assertEquals("3", database.phoneEvents.first().phone)
        Assert.assertEquals("1", database.phoneEvents.last().phone)
    }

    @Test
    fun testRemoveAll() {
        database.phoneEvents.addAll(listOf(
            PhoneEventData(phone = "1", startTime = 0, acceptor = "device"),
            PhoneEventData(phone = "2", startTime = 1, acceptor = "device"),
            PhoneEventData(phone = "3", startTime = 2, acceptor = "device")
        ))

        database.phoneEvents.removeAll(listOf(
            PhoneEventData(phone = "1", startTime = 0, acceptor = "device"),
            PhoneEventData(phone = "3", startTime = 2, acceptor = "device"),
            PhoneEventData(phone = "33", startTime = 33, acceptor = "device")
        ))

        Assert.assertEquals(1, database.phoneEvents.size)
        Assert.assertEquals("2", database.phoneEvents.first().phone)
    }

    @Test
    fun testRetainAll() {
        database.phoneEvents.addAll(listOf(
            PhoneEventData(phone = "1", startTime = 0, acceptor = "device"),
            PhoneEventData(phone = "2", startTime = 1, acceptor = "device"),
            PhoneEventData(phone = "3", startTime = 2, acceptor = "device")
        ))

        database.phoneEvents.retainAll(listOf(
            PhoneEventData(phone = "1", startTime = 0, acceptor = "device"),
            PhoneEventData(phone = "3", startTime = 2, acceptor = "device"),
            PhoneEventData(phone = "33", startTime = 33, acceptor = "device")
        ))

        Assert.assertEquals(2, database.phoneEvents.size)
        Assert.assertEquals("3", database.phoneEvents.first().phone)
        Assert.assertEquals("1", database.phoneEvents.last().phone)
    }

    @Test
    fun testReplaceAll() {
        database.phoneEvents.addAll(listOf(
            PhoneEventData(phone = "1", startTime = 0, acceptor = "device"),
            PhoneEventData(phone = "2", startTime = 1, acceptor = "device"),
            PhoneEventData(phone = "3", startTime = 2, acceptor = "device")
        ))

        database.phoneEvents.replaceAll(listOf(
            PhoneEventData(phone = "11", startTime = 11, acceptor = "device"),
            PhoneEventData(phone = "22", startTime = 22, acceptor = "device")
        ))

        Assert.assertEquals(2, database.phoneEvents.size)
        Assert.assertEquals("22", database.phoneEvents.first().phone)
        Assert.assertEquals("11", database.phoneEvents.last().phone)
    }

}