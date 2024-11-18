package com.bopr.android.smailer.data

import androidx.test.filters.SmallTest
import com.bopr.android.smailer.BaseTest
import com.bopr.android.smailer.messenger.ProcessingState.Companion.STATE_IGNORED
import com.bopr.android.smailer.messenger.ProcessingState.Companion.STATE_PENDING
import com.bopr.android.smailer.messenger.ProcessingState.Companion.STATE_PROCESSED
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
        val dataset = database.phoneCalls

        database.batch {
            dataset.add(
                PhoneCallInfo(
                    "1",
                    true,
                    1000L,
                    0L,
                    true,
                    null,
                    null,
                    "Test 1",
                    STATE_PENDING,
                    "device",
                    PhoneCallInfo.FLAG_BYPASS_NONE,
                    isRead = false
                )
            )
            dataset.add(
                PhoneCallInfo(
                    "2",
                    false,
                    2000L,
                    0L,
                    false,
                    null,
                    null,
                    null,
                    STATE_PENDING,
                    "device",
                    PhoneCallInfo.FLAG_BYPASS_NONE,
                    isRead = false
                )
            )
            dataset.add(
                PhoneCallInfo(
                    "3",
                    true,
                    3000L,
                    0L,
                    false,
                    null,
                    null,
                    null,
                    STATE_PENDING,
                    "device",
                    PhoneCallInfo.FLAG_BYPASS_NONE,
                    isRead = false
                )
            )
            dataset.add(
                PhoneCallInfo(
                    "4",
                    false,
                    4000L,
                    0L,
                    false,
                    null,
                    null,
                    null,
                    STATE_PENDING,
                    "device",
                    PhoneCallInfo.FLAG_BYPASS_NONE,
                    isRead = false
                )
            )
            dataset.add(
                PhoneCallInfo(
                    "5",
                    true,
                    5000L,
                    0L,
                    true,
                    null,
                    null,
                    null,
                    STATE_PENDING,
                    "device",
                    PhoneCallInfo.FLAG_BYPASS_NONE,
                    isRead = false
                )
            )
            dataset.add(
                PhoneCallInfo(
                    "6",
                    true,
                    6000L,
                    7000L,
                    false,
                    null,
                    null,
                    "Test 1",
                    STATE_PENDING,
                    "device",
                    PhoneCallInfo.FLAG_BYPASS_NONE,
                    isRead = false
                )
            )
            dataset.add(
                PhoneCallInfo(
                    "7",
                    false,
                    7000L,
                    0L,
                    false,
                    null,
                    null,
                    "Test 2",
                    STATE_PENDING,
                    "device",
                    PhoneCallInfo.FLAG_BYPASS_NONE,
                    isRead = false
                )
            )
            dataset.add(
                PhoneCallInfo(
                    "8",
                    true,
                    8000L,
                    0L,
                    false,
                    null,
                    null,
                    "Test 3",
                    STATE_PENDING,
                    "device",
                    PhoneCallInfo.FLAG_BYPASS_NONE,
                    isRead = false
                )
            )
            dataset.add(
                PhoneCallInfo(
                    "9",
                    false,
                    9000L,
                    0L,
                    false,
                    null,
                    null,
                    "Test 4",
                    STATE_PENDING,
                    "device",
                    PhoneCallInfo.FLAG_BYPASS_NONE,
                    isRead = false
                )
            )
            dataset.add(
                PhoneCallInfo(
                    "10",
                    true,
                    10000L,
                    20000L,
                    false,
                    "SMS text",
                    GeoLocation(10.5, 20.5),
                    "Test 10",
                    STATE_PENDING,
                    "device",
                    PhoneCallInfo.FLAG_BYPASS_NONE,
                    isRead = false
                )
            )
        }

        assertEquals(10, dataset.size)

        val record = dataset.first() /* descending order so it should be the last */

        assertEquals(STATE_PENDING, record.processState)
        assertEquals("10", record.phone)
        assertTrue(record.isIncoming)
        assertEquals(10000L, record.startTime)
        assertEquals(20000L, record.endTime!!)
        assertFalse(record.isMissed)
        assertTrue(record.isSms)
        assertEquals(10.5, record.location!!.latitude, 0.1)
        assertEquals(20.5, record.location!!.longitude, 0.1)
        assertEquals("SMS text", record.text)
        assertEquals("Test 10", record.details)
    }

    @Test
    fun testUpdate() {
        database.phoneCalls.add(
            PhoneCallInfo(
                phone = "1",
                startTime = 0,
                acceptor = "device",
                processState = STATE_PENDING,
                isRead = false
            )
        )

        assertEquals(1, database.phoneCalls.size)
        database.phoneCalls.first().run {
            assertEquals("1", phone)
            assertEquals(0, startTime)
            assertEquals("device", acceptor)
            assertEquals(STATE_PENDING, processState)
            assertEquals(false, isRead)
        }

        assertTrue(
            database.phoneCalls.add(
                PhoneCallInfo(
                    phone = "1",
                    startTime = 0,
                    acceptor = "device",
                    processState = STATE_PROCESSED,
                    isRead = true
                )
            )
        )
        assertEquals(1, database.phoneCalls.size)
        database.phoneCalls.first().run {
            assertEquals("1", phone)
            assertEquals(0, startTime)
            assertEquals("device", acceptor)
            assertEquals(STATE_PROCESSED, processState)
            assertEquals(true, isRead)
        }
    }

    @Test
    fun testUpdateGet() {
        var info = PhoneCallInfo(
            phone = "1",
            isIncoming = true,
            startTime = 1000L,
            endTime = 2000L,
            isMissed = false,
            text = "SMS text",
            location = GeoLocation(10.5, 20.5),
            details = "Test 1",
            processState = STATE_PENDING,
            acceptor = "device",
            bypassFlags = PhoneCallInfo.FLAG_BYPASS_NONE,
            isRead = false
        )

        val dataset = database.phoneCalls

        dataset.add(info)

        assertEquals(1, dataset.size)

        info = dataset.first()

        assertEquals(STATE_PENDING, info.processState)
        assertEquals("1", info.phone)
        assertTrue(info.isIncoming)
        assertEquals(1000L, info.startTime)
        assertEquals(2000L, info.endTime!!)
        assertFalse(info.isMissed)
        assertTrue(info.isSms)
        assertEquals(10.5, info.location!!.latitude, 0.1)
        assertEquals(20.5, info.location!!.longitude, 0.1)
        assertEquals("SMS text", info.text)
        assertEquals("Test 1", info.details)
        assertEquals(STATE_PENDING, info.processState)

        info = info.copy(
            phone = "2",
            isIncoming = false,
            endTime = 3000L,
            isMissed = true,
            location = GeoLocation(11.5, 21.5),
            text = "New text",
            details = "New details"
        )
        dataset.add(info)

        assertEquals(1, dataset.size)

        info = dataset.first()

        assertEquals(STATE_PENDING, info.processState)
        assertEquals("2", info.phone)
        assertFalse(info.isIncoming)
        assertEquals(3000L, info.endTime!!)
        assertTrue(info.isMissed)
        assertTrue(info.isSms)
        assertEquals(11.5, info.location!!.latitude, 0.1)
        assertEquals(21.5, info.location!!.longitude, 0.1)
        assertEquals("New text", info.text)
        assertEquals("New details", info.details)
    }

    @Test
    fun testClear() {
        val dataset = database.phoneCalls

        database.batch {
            dataset.add(
                PhoneCallInfo(
                    "1",
                    true,
                    1000L,
                    2000L,
                    false,
                    "SMS text",
                    GeoLocation(10.5, 20.5),
                    "Test 1",
                    STATE_PENDING,
                    "device",
                    PhoneCallInfo.FLAG_BYPASS_NONE,
                    isRead = false
                )
            )
            dataset.add(
                PhoneCallInfo(
                    "2",
                    false,
                    2000L,
                    0L,
                    false,
                    null,
                    null,
                    null,
                    STATE_PENDING,
                    "device",
                    PhoneCallInfo.FLAG_BYPASS_NONE,
                    isRead = false
                )
            )
            dataset.add(
                PhoneCallInfo(
                    "3",
                    true,
                    3000L,
                    0L,
                    false,
                    null,
                    null,
                    null,
                    STATE_PENDING,
                    "device",
                    PhoneCallInfo.FLAG_BYPASS_NONE,
                    isRead = false
                )
            )
            dataset.add(
                PhoneCallInfo(
                    "4",
                    false,
                    4000L,
                    0L,
                    false,
                    null,
                    null,
                    null,
                    STATE_PENDING,
                    "device",
                    PhoneCallInfo.FLAG_BYPASS_NONE,
                    isRead = false
                )
            )
            dataset.add(
                PhoneCallInfo(
                    "5",
                    true,
                    5000L,
                    0L,
                    true,
                    null,
                    null,
                    null,
                    STATE_PENDING,
                    "device",
                    PhoneCallInfo.FLAG_BYPASS_NONE,
                    isRead = false
                )
            )
            dataset.add(
                PhoneCallInfo(
                    "6",
                    true,
                    6000L,
                    7000L,
                    false,
                    null,
                    null,
                    "Test 1",
                    STATE_PENDING,
                    "device",
                    PhoneCallInfo.FLAG_BYPASS_NONE,
                    isRead = false
                )
            )
            dataset.add(
                PhoneCallInfo(
                    "7",
                    false,
                    7000L,
                    0L,
                    false,
                    null,
                    null,
                    "Test 2",
                    STATE_PENDING,
                    "device",
                    PhoneCallInfo.FLAG_BYPASS_NONE,
                    isRead = false
                )
            )
            dataset.add(
                PhoneCallInfo(
                    "8",
                    true,
                    8000L,
                    0L,
                    false,
                    null,
                    null,
                    "Test 3",
                    STATE_PENDING,
                    "device",
                    PhoneCallInfo.FLAG_BYPASS_NONE,
                    isRead = false
                )
            )
            dataset.add(
                PhoneCallInfo(
                    "9",
                    false,
                    9000L,
                    0L,
                    false,
                    null,
                    null,
                    "Test 4",
                    STATE_PENDING,
                    "device",
                    PhoneCallInfo.FLAG_BYPASS_NONE,
                    isRead = false
                )
            )
            dataset.add(
                PhoneCallInfo(
                    "10",
                    true,
                    10000L,
                    0L,
                    true,
                    null,
                    null,
                    "Test 5",
                    STATE_PENDING,
                    "device",
                    PhoneCallInfo.FLAG_BYPASS_NONE,
                    isRead = false
                )
            )
        }
        assertEquals(10, dataset.size)

        dataset.clear()

        assertEquals(0, dataset.size)
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
        val dataset = database.phoneCalls

        database.batch {
            dataset.add(
                PhoneCallInfo(
                    "1",
                    true,
                    1000L,
                    0L,
                    true,
                    null,
                    null,
                    "Test 1",
                    STATE_PROCESSED,
                    "device",
                    PhoneCallInfo.FLAG_BYPASS_NONE,
                    isRead = false
                )
            )
            dataset.add(
                PhoneCallInfo(
                    "2",
                    false,
                    2000L,
                    0L,
                    false,
                    null,
                    null,
                    null,
                    STATE_PROCESSED,
                    "device",
                    PhoneCallInfo.FLAG_BYPASS_NONE,
                    isRead = false
                )
            )
            dataset.add(
                PhoneCallInfo(
                    "3",
                    true,
                    3000L,
                    0L,
                    false,
                    null,
                    null,
                    null,
                    STATE_PROCESSED,
                    "device",
                    PhoneCallInfo.FLAG_BYPASS_NONE,
                    isRead = false
                )
            )
            dataset.add(
                PhoneCallInfo(
                    "4",
                    false,
                    4000L,
                    0L,
                    false,
                    null,
                    null,
                    null,
                    STATE_IGNORED,
                    "device",
                    PhoneCallInfo.FLAG_BYPASS_NONE,
                    isRead = false
                )
            )
            dataset.add(
                PhoneCallInfo(
                    "5",
                    true,
                    5000L,
                    0L,
                    true,
                    null,
                    null,
                    null,
                    STATE_IGNORED,
                    "device",
                    PhoneCallInfo.FLAG_BYPASS_NONE,
                    isRead = false
                )
            )
            dataset.add(
                PhoneCallInfo(
                    "6",
                    true,
                    6000L,
                    7000L,
                    false,
                    null,
                    null,
                    "Test 1",
                    STATE_PENDING,
                    "device",
                    PhoneCallInfo.FLAG_BYPASS_NONE,
                    isRead = false
                )
            )
            dataset.add(
                PhoneCallInfo(
                    "7",
                    false,
                    7000L,
                    0L,
                    false,
                    null,
                    null,
                    "Test 2",
                    STATE_PENDING,
                    "device",
                    PhoneCallInfo.FLAG_BYPASS_NONE,
                    isRead = false
                )
            )
            dataset.add(
                PhoneCallInfo(
                    "8",
                    true,
                    8000L,
                    0L,
                    false,
                    null,
                    null,
                    "Test 3",
                    STATE_PENDING,
                    "device",
                    PhoneCallInfo.FLAG_BYPASS_NONE,
                    isRead = false
                )
            )
            dataset.add(
                PhoneCallInfo(
                    "9",
                    false,
                    9000L,
                    0L,
                    false,
                    null,
                    null,
                    "Test 4",
                    STATE_PENDING,
                    "device",
                    PhoneCallInfo.FLAG_BYPASS_NONE,
                    isRead = false
                )
            )
            dataset.add(
                PhoneCallInfo(
                    "10",
                    true,
                    10000L,
                    20000L,
                    false,
                    null,
                    null,
                    "Test 10",
                    STATE_PENDING,
                    "device",
                    PhoneCallInfo.FLAG_BYPASS_NONE,
                    isRead = false
                )
            )
        }
        val pendingRecords = dataset.filterPending

        assertEquals(5, pendingRecords.size)

        val details = pendingRecords.first().details /* descending order so it should be the last */

        assertEquals("Test 10", details)
    }

    @Test
    fun testUnreadCount() {
        database.batch {
            phoneCalls.add(
                PhoneCallInfo(
                    phone = "1",
                    startTime = 0,
                    acceptor = "device",
                    isRead = true
                )
            )
            phoneCalls.add(
                PhoneCallInfo(
                    phone = "2",
                    startTime = 1,
                    acceptor = "device",
                    isRead = false
                )
            )
            phoneCalls.add(
                PhoneCallInfo(
                    phone = "3",
                    startTime = 2,
                    acceptor = "device",
                    isRead = false
                )
            )
        }

        assertEquals(2, database.phoneCalls.unreadCount)
    }

    @Test
    fun testGetTransform() {
        val dataset = database.phoneCalls

        database.batch {
            dataset.add(PhoneCallInfo(phone = "1", startTime = 0, acceptor = "device"))
            dataset.add(PhoneCallInfo(phone = "2", startTime = 1, acceptor = "device"))
            dataset.add(PhoneCallInfo(phone = "3", startTime = 2, acceptor = "device"))
            dataset.add(PhoneCallInfo(phone = "4", startTime = 3, acceptor = "device"))
            dataset.add(PhoneCallInfo(phone = "5", startTime = 4, acceptor = "device"))
        }

        val phones = dataset.map { it.phone }

        assertEquals(5, phones.size)

        assertEquals("5", phones[0])
        assertEquals("3", phones[2])
        assertEquals("1", phones[4])
    }

    @Test
    fun testIteratorRemove() {
        database.batch {
            phoneCalls.add(PhoneCallInfo(phone = "1", startTime = 0, acceptor = "device"))
            phoneCalls.add(PhoneCallInfo(phone = "2", startTime = 1, acceptor = "device"))
            phoneCalls.add(PhoneCallInfo(phone = "3", startTime = 2, acceptor = "device"))
        }

        val iterator = database.phoneCalls.iterator()
        while (iterator.hasNext()) {
            if (iterator.next().phone == "1") {
                iterator.remove()
            }
        }

        assertEquals(2, database.phoneCalls.size)
        assertEquals("3", database.phoneCalls.first().phone)
        assertEquals("2", database.phoneCalls.last().phone)
    }

    @Test
    fun testContains() {
        database.batch {
            phoneCalls.add(PhoneCallInfo(phone = "1", startTime = 0, acceptor = "device"))
            phoneCalls.add(PhoneCallInfo(phone = "2", startTime = 1, acceptor = "device"))
            phoneCalls.add(PhoneCallInfo(phone = "3", startTime = 2, acceptor = "device"))
        }

        assertTrue(
            database.phoneCalls.contains(
                PhoneCallInfo(
                    phone = "1",
                    startTime = 0,
                    acceptor = "device"
                )
            )
        )
        assertTrue(
            database.phoneCalls.containsAll(
                listOf(
                    PhoneCallInfo(phone = "1", startTime = 0, acceptor = "device"),
                    PhoneCallInfo(phone = "3", startTime = 2, acceptor = "device")
                )
            )
        )
    }

    @Test
    fun testAddAll() {
        database.phoneCalls.addAll(
            listOf(
                PhoneCallInfo(phone = "1", startTime = 0, acceptor = "device"),
                PhoneCallInfo(phone = "2", startTime = 1, acceptor = "device"),
                PhoneCallInfo(phone = "3", startTime = 2, acceptor = "device")
            )
        )

        assertEquals(3, database.phoneCalls.size)
        assertEquals("3", database.phoneCalls.first().phone)
        assertEquals("1", database.phoneCalls.last().phone)
    }

    @Test
    fun testRemoveAll() {
        database.phoneCalls.addAll(
            listOf(
                PhoneCallInfo(phone = "1", startTime = 0, acceptor = "device"),
                PhoneCallInfo(phone = "2", startTime = 1, acceptor = "device"),
                PhoneCallInfo(phone = "3", startTime = 2, acceptor = "device")
            )
        )

        database.phoneCalls.removeAll(
            listOf(
                PhoneCallInfo(phone = "1", startTime = 0, acceptor = "device"),
                PhoneCallInfo(phone = "3", startTime = 2, acceptor = "device"),
                PhoneCallInfo(phone = "33", startTime = 33, acceptor = "device")
            )
        )

        assertEquals(1, database.phoneCalls.size)
        assertEquals("2", database.phoneCalls.first().phone)
    }

    @Test
    fun testRetainAll() {
        database.phoneCalls.addAll(
            listOf(
                PhoneCallInfo(phone = "1", startTime = 0, acceptor = "device"),
                PhoneCallInfo(phone = "2", startTime = 1, acceptor = "device"),
                PhoneCallInfo(phone = "3", startTime = 2, acceptor = "device")
            )
        )

        database.phoneCalls.retainAll(
            listOf(
                PhoneCallInfo(phone = "1", startTime = 0, acceptor = "device"),
                PhoneCallInfo(phone = "3", startTime = 2, acceptor = "device"),
                PhoneCallInfo(phone = "33", startTime = 33, acceptor = "device")
            )
        )

        assertEquals(2, database.phoneCalls.size)
        assertEquals("3", database.phoneCalls.first().phone)
        assertEquals("1", database.phoneCalls.last().phone)
    }

    @Test
    fun testReplaceAll() {
        database.phoneCalls.addAll(
            listOf(
                PhoneCallInfo(phone = "1", startTime = 0, acceptor = "device"),
                PhoneCallInfo(phone = "2", startTime = 1, acceptor = "device"),
                PhoneCallInfo(phone = "3", startTime = 2, acceptor = "device")
            )
        )

        database.phoneCalls.replaceAll(
            listOf(
                PhoneCallInfo(phone = "11", startTime = 11, acceptor = "device"),
                PhoneCallInfo(phone = "22", startTime = 22, acceptor = "device")
            )
        )

        assertEquals(2, database.phoneCalls.size)
        assertEquals("22", database.phoneCalls.first().phone)
        assertEquals("11", database.phoneCalls.last().phone)
    }

}