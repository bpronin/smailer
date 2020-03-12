package com.bopr.android.smailer

import androidx.test.filters.SmallTest
import com.bopr.android.smailer.PhoneEvent.Companion.STATE_IGNORED
import com.bopr.android.smailer.PhoneEvent.Companion.STATE_PENDING
import com.bopr.android.smailer.PhoneEvent.Companion.STATE_PROCESSED
import com.bopr.android.smailer.PhoneEvent.Companion.STATUS_ACCEPTED
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
        database = Database(targetContext, "test.sqlite")
        database.destroy()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testPutGet() {
        database.putEvent(PhoneEvent("1", true, 1000L, 0L, true, null, null, "Test 1", STATE_PENDING, "device", STATUS_ACCEPTED, isRead = false))
        database.putEvent(PhoneEvent("2", false, 2000L, 0L, false, null, null, null, STATE_PENDING, "device", STATUS_ACCEPTED, isRead = false))
        database.putEvent(PhoneEvent("3", true, 3000L, 0L, false, null, null, null, STATE_PENDING, "device", STATUS_ACCEPTED, isRead = false))
        database.putEvent(PhoneEvent("4", false, 4000L, 0L, false, null, null, null, STATE_PENDING, "device", STATUS_ACCEPTED, isRead = false))
        database.putEvent(PhoneEvent("5", true, 5000L, 0L, true, null, null, null, STATE_PENDING, "device", STATUS_ACCEPTED, isRead = false))
        database.putEvent(PhoneEvent("6", true, 6000L, 7000L, false, null, null, "Test 1", STATE_PENDING, "device", STATUS_ACCEPTED, isRead = false))
        database.putEvent(PhoneEvent("7", false, 7000L, 0L, false, null, null, "Test 2", STATE_PENDING, "device", STATUS_ACCEPTED, isRead = false))
        database.putEvent(PhoneEvent("8", true, 8000L, 0L, false, null, null, "Test 3", STATE_PENDING, "device", STATUS_ACCEPTED, isRead = false))
        database.putEvent(PhoneEvent("9", false, 9000L, 0L, false, null, null, "Test 4", STATE_PENDING, "device", STATUS_ACCEPTED, isRead = false))
        database.putEvent(PhoneEvent("10", true, 10000L, 20000L, false, "SMS text", GeoCoordinates(10.5, 20.5), "Test 10", STATE_PENDING, "device", STATUS_ACCEPTED, isRead = false))

        val items = database.events.list()

        assertEquals(10, items.size)

        val message = items[0] /* descending order so it should be the last */

        assertEquals(STATE_PENDING, message.state)
        assertEquals("10", message.phone)
        assertTrue(message.isIncoming)
        assertEquals(10000L, message.startTime)
        assertEquals(20000L, message.endTime!!)
        assertFalse(message.isMissed)
        assertTrue(message.isSms)
        assertEquals(10.5, message.location!!.latitude, 0.1)
        assertEquals(20.5, message.location!!.longitude, 0.1)
        assertEquals("SMS text", message.text)
        assertEquals("Test 10", message.details)
    }

    @Test
    fun testUpdateGet() {
        var event = PhoneEvent(
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
        database.putEvent(event)
        var items = database.events.list()

        assertEquals(1, items.size)

        event = items[0]

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
        database.putEvent(event)

        items = database.events.list()

        assertEquals(1, items.size)

        event = items[0]

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

    /**
     * Check [Database.clearEvents]  method.
     */
    @Test
    fun testClear() {
        database.putEvent(PhoneEvent("1", true, 1000L, 2000L, false, "SMS text", GeoCoordinates(10.5, 20.5), "Test 1", STATE_PENDING, "device", STATUS_ACCEPTED, isRead = false))
        database.putEvent(PhoneEvent("2", false, 2000L, 0L, false, null, null, null, STATE_PENDING, "device", STATUS_ACCEPTED, isRead = false))
        database.putEvent(PhoneEvent("3", true, 3000L, 0L, false, null, null, null, STATE_PENDING, "device", STATUS_ACCEPTED, isRead = false))
        database.putEvent(PhoneEvent("4", false, 4000L, 0L, false, null, null, null, STATE_PENDING, "device", STATUS_ACCEPTED, isRead = false))
        database.putEvent(PhoneEvent("5", true, 5000L, 0L, true, null, null, null, STATE_PENDING, "device", STATUS_ACCEPTED, isRead = false))
        database.putEvent(PhoneEvent("6", true, 6000L, 7000L, false, null, null, "Test 1", STATE_PENDING, "device", STATUS_ACCEPTED, isRead = false))
        database.putEvent(PhoneEvent("7", false, 7000L, 0L, false, null, null, "Test 2", STATE_PENDING, "device", STATUS_ACCEPTED, isRead = false))
        database.putEvent(PhoneEvent("8", true, 8000L, 0L, false, null, null, "Test 3", STATE_PENDING, "device", STATUS_ACCEPTED, isRead = false))
        database.putEvent(PhoneEvent("9", false, 9000L, 0L, false, null, null, "Test 4", STATE_PENDING, "device", STATUS_ACCEPTED, isRead = false))
        database.putEvent(PhoneEvent("10", true, 10000L, 0L, true, null, null, "Test 5", STATE_PENDING, "device", STATUS_ACCEPTED, isRead = false))

        assertEquals(10, database.events.count())

        database.clearEvents()

        assertEquals(0, database.events.count())
    }

//    /**
//     * Check [Database.purge]  method..
//     */
//    @Test
//    @Throws(InterruptedException::class)
//    fun testPurge() {
//        database.putEvent(PhoneEvent("1", true, 1000L, 2000L, false, "SMS text", GeoCoordinates(10.5, 20.5), "Test 1", STATE_PENDING, "device", REASON_ACCEPTED, false))
//        database.putEvent(PhoneEvent("2", false, 2000L, 0L, false, null, null, null, STATE_PENDING, "device", REASON_ACCEPTED, false))
//        database.putEvent(PhoneEvent("3", true, 3000L, 0L, false, null, null, null, STATE_PENDING, "device", REASON_ACCEPTED, false))
//        database.putEvent(PhoneEvent("4", false, 4000L, 0L, false, null, null, null, STATE_PENDING, "device", REASON_ACCEPTED, false))
//        database.putEvent(PhoneEvent("5", true, 5000L, 0L, true, null, null, null, STATE_PENDING, "device", REASON_ACCEPTED, false))
//        database.putEvent(PhoneEvent("6", true, 6000L, 7000L, false, null, null, "Test 1", STATE_PENDING, "device", REASON_ACCEPTED, false))
//        database.putEvent(PhoneEvent("7", false, 7000L, 0L, false, null, null, "Test 2", STATE_PENDING, "device", REASON_ACCEPTED, false))
//        database.putEvent(PhoneEvent("8", true, 8000L, 0L, false, null, null, "Test 3", STATE_PENDING, "device", REASON_ACCEPTED, false))
//        database.putEvent(PhoneEvent("9", false, 9000L, 0L, false, null, null, "Test 4", STATE_PENDING, "device", REASON_ACCEPTED, false))
//        database.putEvent(PhoneEvent("10", true, 10000L, 0L, true, null, null, "Test 5", STATE_PENDING, "device", REASON_ACCEPTED, false))
//        /* first we have 9 records */assertEquals(10, database.events.getCount())
//        /* change default capacity and period to small values */database.capacity = 5
//        database.purgePeriod = 500
//        /* sleep a time that is less than purge period */Thread.sleep(100)
//        database.purge()
//        /* nothing happens cause elapsed time is less than purge period */assertEquals(10, database.events.getCount())
//        /* sleep again */Thread.sleep(500)
//        database.purge()
//        /* last addition should trigger purge process cause total elapsed time exceeds purge period */assertEquals(5, database.events.getCount())
//    }

    @Test
    fun testSaveLoadLocation() {
        database.lastLocation = GeoCoordinates(30.0, 60.0)
        val actual = database.lastLocation!!

        assertEquals(30.0, actual.latitude, 0.1)
        assertEquals(60.0, actual.longitude, 0.1)
    }

    @Test
    fun testGetUnsentMessages() {
        database.putEvent(PhoneEvent("1", true, 1000L, 0L, true, null, null, "Test 1", STATE_PROCESSED, "device", STATUS_ACCEPTED, isRead = false))
        database.putEvent(PhoneEvent("2", false, 2000L, 0L, false, null, null, null, STATE_PROCESSED, "device", STATUS_ACCEPTED, isRead = false))
        database.putEvent(PhoneEvent("3", true, 3000L, 0L, false, null, null, null, STATE_PROCESSED, "device", STATUS_ACCEPTED, isRead = false))
        database.putEvent(PhoneEvent("4", false, 4000L, 0L, false, null, null, null, STATE_IGNORED, "device", STATUS_ACCEPTED, isRead = false))
        database.putEvent(PhoneEvent("5", true, 5000L, 0L, true, null, null, null, STATE_IGNORED, "device", STATUS_ACCEPTED, isRead = false))
        database.putEvent(PhoneEvent("6", true, 6000L, 7000L, false, null, null, "Test 1", STATE_PENDING, "device", STATUS_ACCEPTED, isRead = false))
        database.putEvent(PhoneEvent("7", false, 7000L, 0L, false, null, null, "Test 2", STATE_PENDING, "device", STATUS_ACCEPTED, isRead = false))
        database.putEvent(PhoneEvent("8", true, 8000L, 0L, false, null, null, "Test 3", STATE_PENDING, "device", STATUS_ACCEPTED, isRead = false))
        database.putEvent(PhoneEvent("9", false, 9000L, 0L, false, null, null, "Test 4", STATE_PENDING, "device", STATUS_ACCEPTED, isRead = false))
        database.putEvent(PhoneEvent("10", true, 10000L, 20000L, false, null, null, "Test 10", STATE_PENDING, "device", STATUS_ACCEPTED, isRead = false))

        val items = database.pendingEvents.list()

        assertEquals(5, items.size)

        val details = items[0].details /* descending order so it should be the last */

        assertEquals("Test 10", details)
    }

    @Test
    fun testGetTransform() {
        database.putEvent(PhoneEvent(phone = "1", startTime = 0, acceptor = "device"))
        database.putEvent(PhoneEvent(phone = "2", startTime = 1, acceptor = "device"))
        database.putEvent(PhoneEvent(phone = "3", startTime = 2, acceptor = "device"))
        database.putEvent(PhoneEvent(phone = "4", startTime = 3, acceptor = "device"))
        database.putEvent(PhoneEvent(phone = "5", startTime = 4, acceptor = "device"))

        val phones = database.events.map { it.phone }

        assertEquals(5, phones.size)

        assertEquals("5", phones[0])
        assertEquals("3", phones[2])
        assertEquals("1", phones[4])
    }

}