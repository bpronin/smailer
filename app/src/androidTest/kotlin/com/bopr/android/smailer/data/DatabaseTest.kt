package com.bopr.android.smailer.data

import androidx.test.filters.SmallTest
import com.bopr.android.smailer.BaseTest
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
        val dataset = database.events

        database.batch {
            dataset.add(
                Event(
                    payload = PhoneCallInfo(
                        phone = "1",
                        isIncoming = true,
                        startTime = 1000L
                    )
                )
            )
            dataset.add(
                Event(
                    payload = PhoneCallInfo(
                        phone = "2",
                        isIncoming = false,
                        startTime = 2000L
                    )
                )
            )
            dataset.add(
                Event(
                    payload = PhoneCallInfo(
                        phone = "3",
                        isIncoming = true,
                        startTime = 3000L
                    )
                )
            )
            dataset.add(
                Event(
                    payload = PhoneCallInfo(
                        phone = "4",
                        isIncoming = false,
                        startTime = 4000L
                    )
                )
            )
            dataset.add(
                Event(
                    payload = PhoneCallInfo(
                        phone = "5",
                        isIncoming = true,
                        startTime = 5000L
                    )
                )
            )
            dataset.add(
                Event(
                    payload = PhoneCallInfo(
                        phone = "6",
                        isIncoming = true,
                        startTime = 6000L,
                        endTime = 7000L
                    )
                )
            )
            dataset.add(
                Event(
                    payload = PhoneCallInfo(
                        phone = "7",
                        isIncoming = false,
                        startTime = 7000L
                    )
                )
            )
            dataset.add(
                Event(
                    payload = PhoneCallInfo(
                        phone = "8",
                        isIncoming = true,
                        startTime = 8000L
                    )
                )
            )
            dataset.add(
                Event(
                    payload = PhoneCallInfo(
                        phone = "9",
                        isIncoming = false,
                        startTime = 9000L
                    )
                )
            )
            dataset.add(
                Event(
                    payload = PhoneCallInfo(
                        phone = "10",
                        isIncoming = true,
                        startTime = 10000L,
                        endTime = 20000L,
                        text = "SMS text"
                    ),
                    location = GeoLocation(10.5, 20.5)
                )
            )
        }

        assertEquals(10, dataset.size)

        val event = dataset.first() /* descending order so it should be the last */
        val info = event.payload as PhoneCallInfo

        assertEquals(STATE_PENDING, event.processState)
        assertEquals("10", info.phone)
        assertTrue(info.isIncoming)
        assertEquals(10000L, info.startTime)
        assertEquals(20000L, info.endTime!!)
        assertFalse(info.isMissed)
        assertTrue(info.isSms)
        assertEquals(10.5, event.location!!.latitude, 0.1)
        assertEquals(20.5, event.location!!.longitude, 0.1)
        assertEquals("SMS text", info.text)
    }

    @Test
    fun testUpdate() {
        database.events.add(
            Event(
                payload = PhoneCallInfo(
                    phone = "1", 
                    startTime = 0
                ), 
                target = "device"
            )
        )

        assertEquals(1, database.events.size)
        database.events.first().run {
            val info = payload as PhoneCallInfo

            assertEquals("1", info.phone)
            assertEquals(0, info.startTime)
            assertEquals("device", target)
            assertEquals(STATE_PENDING, processState)
            assertEquals(false, isRead)
        }

        assertTrue(
            database.events.add(
                Event(
                    payload = PhoneCallInfo(
                        phone = "1",
                        startTime = 0
                    ),
                    target = "device",
                    processState = STATE_PROCESSED, isRead = true
                )
            )
        )
        assertEquals(1, database.events.size)
        database.events.first().run {
            val info = payload as PhoneCallInfo
            assertEquals("1", info.phone)
            assertEquals(0, info.startTime)
            assertEquals("device", target)
            assertEquals(STATE_PROCESSED, processState)
            assertEquals(true, isRead)
        }
    }

    @Test
    fun testUpdateGet() {
        var event = Event(
            payload = PhoneCallInfo(
                phone = "1",
                isIncoming = true,
                startTime = 1000L,
                endTime = 2000L,
                
                text = "SMS text"
            ),
            location = GeoLocation(10.5, 20.5)
        )
        val dataset = database.events

        dataset.add(event)

        assertEquals(1, dataset.size)

        event = dataset.first()
        val payload = event.payload as PhoneCallInfo

        assertEquals(STATE_PENDING, event.processState)
        assertEquals("1", payload.phone)
        assertTrue(payload.isIncoming)
        assertEquals(1000L, payload.startTime)
        assertEquals(2000L, payload.endTime!!)
        assertFalse(payload.isMissed)
        assertTrue(payload.isSms)
        assertEquals(10.5, event.location!!.latitude, 0.1)
        assertEquals(20.5, event.location!!.longitude, 0.1)
        assertEquals("SMS text", payload.text)
        assertEquals(STATE_PENDING, event.processState)

        event = event.copy(
            location = GeoLocation(11.5, 21.5)
        )
        dataset.add(event)

        assertEquals(1, dataset.size)

        event = dataset.first()
        val info = event.payload as PhoneCallInfo

        assertEquals(STATE_PENDING, event.processState)
        assertEquals("2", info.phone)
        assertFalse(info.isIncoming)
        assertEquals(3000L, info.endTime!!)
        assertTrue(info.isMissed)
        assertTrue(info.isSms)
        assertEquals(11.5, event.location!!.latitude, 0.1)
        assertEquals(21.5, event.location!!.longitude, 0.1)
        assertEquals("New text", info.text)
    }

    @Test
    fun testClear() {
        val dataset = database.events

        database.batch {
            dataset.add(
                Event(
                    payload = PhoneCallInfo(
                        phone = "1",
                        isIncoming = true,
                        startTime = 1000L,
                        endTime = 2000L,
                        
                        text = "SMS text"
                    ), location = GeoLocation(10.5, 20.5)
                )
            )
            dataset.add(
                Event(
                    payload = PhoneCallInfo(
                        phone = "2",
                        isIncoming = false,
                        startTime = 2000L,
                        
                        
                        
                    )
                )
            )
            dataset.add(
                Event(
                    payload = PhoneCallInfo(
                        phone = "3",
                        isIncoming = true,
                        startTime = 3000L,
                        
                        
                        
                    )
                )
            )
            dataset.add(
                Event(
                    payload = PhoneCallInfo(
                        phone = "4",
                        isIncoming = false,
                        startTime = 4000L,
                        
                        
                        
                    )
                )
            )
            dataset.add(
                Event(
                    payload = PhoneCallInfo(
                        phone = "5",
                        isIncoming = true,
                        startTime = 5000L,
                        
                        isMissed = true
                    )
                )
            )
            dataset.add(
                Event(
                    payload = PhoneCallInfo(
                        phone = "6",
                        isIncoming = true,
                        startTime = 6000L,
                        endTime = 7000L,
                        
                        
                    )
                )
            )
            dataset.add(
                Event(
                    payload = PhoneCallInfo(
                        phone = "7",
                        isIncoming = false,
                        startTime = 7000L,
                        
                        isMissed = false
                    )
                )
            )
            dataset.add(
                Event(
                    payload = PhoneCallInfo(
                        phone = "8",
                        isIncoming = true,
                        startTime = 8000L,
                        
                        isMissed = false
                    )
                )
            )
            dataset.add(
                Event(
                    payload = PhoneCallInfo(
                        phone = "9",
                        isIncoming = false,
                        startTime = 9000L,
                        
                        isMissed = false
                    )
                )
            )
            dataset.add(
                Event(
                    payload = PhoneCallInfo(
                        phone = "10",
                        isIncoming = true,
                        startTime = 10000L,
                        
                        isMissed = true
                    )
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
        val dataset = database.events

        database.batch {
            dataset.add(
                Event(
                    payload = PhoneCallInfo(
                        phone = "1",
                        isIncoming = true,
                        startTime = 1000L,
                        
                        isMissed = true
                    ),
                    processState = STATE_PROCESSED
                )
            )
            dataset.add(
                Event(
                    payload = PhoneCallInfo(
                        phone = "2",
                        isIncoming = false,
                        startTime = 2000L,
                        
                        isMissed = false
                    ),
                    processState = STATE_PROCESSED
                )
            )
            dataset.add(
                Event(
                    payload = PhoneCallInfo(
                        phone = "3",
                        isIncoming = true,
                        startTime = 3000L,
                        
                        isMissed = false
                    ),
                    processState = STATE_PROCESSED
                )
            )
            dataset.add(
                Event(
                    payload = PhoneCallInfo(
                        phone = "4",
                        isIncoming = false,
                        startTime = 4000L,
                        
                        isMissed = false
                    ),
                    processState = STATE_IGNORED
                )
            )
            dataset.add(
                Event(
                    payload = PhoneCallInfo(
                        phone = "5",
                        isIncoming = true,
                        startTime = 5000L,
                        
                        isMissed = true
                    ),
                    processState = STATE_IGNORED
                )
            )
            dataset.add(
                Event(
                    payload = PhoneCallInfo(
                        phone = "6",
                        isIncoming = true,
                        startTime = 6000L,
                        endTime = 7000L,
                        isMissed = false
                    )
                )
            )
            dataset.add(
                Event(
                    payload = PhoneCallInfo(
                        phone = "7",
                        isIncoming = false,
                        startTime = 7000L,
                        
                        isMissed = false
                    )
                )
            )
            dataset.add(
                Event(
                    payload = PhoneCallInfo(
                        phone = "8",
                        isIncoming = true,
                        startTime = 8000L,
                        
                        isMissed = false
                    )
                )
            )
            dataset.add(
                Event(
                    payload = PhoneCallInfo(
                        phone = "9",
                        isIncoming = false,
                        startTime = 9000L,
                        
                        isMissed = false
                    )
                )
            )
            dataset.add(
                Event(
                    payload = PhoneCallInfo(
                        phone = "10",
                        isIncoming = true,
                        startTime = 10000L,
                        endTime = 20000L,
                        isMissed = false
                    )
                )
            )
        }
        val pendingRecords = dataset.pending

        assertEquals(5, pendingRecords.size)
    }

    @Test
    fun testUnreadCount() {
        database.batch {
            events.add(
                Event(payload = PhoneCallInfo(phone = "1", startTime = 0), isRead = true)
            )
            events.add(
                Event(payload = PhoneCallInfo(phone = "2", startTime = 1))
            )
            events.add(
                Event(payload = PhoneCallInfo(phone = "3", startTime = 2))
            )
        }

        assertEquals(2, database.events.unreadCount)
    }

    @Test
    fun testGetTransform() {
        val dataset = database.events

        database.batch {
            dataset.add(
                Event(payload = PhoneCallInfo(phone = "1", startTime = 0))
            )
            dataset.add(
                Event(payload = PhoneCallInfo(phone = "2", startTime = 1))
            )
            dataset.add(
                Event(payload = PhoneCallInfo(phone = "3", startTime = 2))
            )
            dataset.add(
                Event(payload = PhoneCallInfo(phone = "4", startTime = 3))
            )
            dataset.add(
                Event(payload = PhoneCallInfo(phone = "5", startTime = 4))
            )
        }

        val phones = dataset.map {
            (it.payload as PhoneCallInfo).phone
        }

        assertEquals(5, phones.size)

        assertEquals("5", phones[0])
        assertEquals("3", phones[2])
        assertEquals("1", phones[4])
    }

    @Test
    fun testIteratorRemove() {
        database.batch {
            events.add(
                Event(payload = PhoneCallInfo(phone = "1", startTime = 0))
            )
            events.add(
                Event(payload = PhoneCallInfo(phone = "2", startTime = 1))
            )
            events.add(
                Event(payload = PhoneCallInfo(phone = "3", startTime = 2))
            )
        }

        val iterator = database.events.iterator()
        while (iterator.hasNext()) {
            if ((iterator.next().payload as PhoneCallInfo).phone == "1") {
                iterator.remove()
            }
        }

        assertEquals(2, database.events.size)
        assertEquals("3", (database.events.first().payload as PhoneCallInfo).phone)
        assertEquals("2", (database.events.last().payload as PhoneCallInfo).phone)
    }

    @Test
    fun testContains() {
        database.batch {
            events.add(
                Event(payload = PhoneCallInfo(phone = "1", startTime = 0))
            )
            events.add(
                Event(payload = PhoneCallInfo(phone = "2", startTime = 1))
            )
            events.add(
                Event(payload = PhoneCallInfo(phone = "3", startTime = 2))
            )
        }

        assertTrue(
            database.events.contains(
                Event(payload = PhoneCallInfo(phone = "1", startTime = 0))
            )
        )
        assertTrue(
            database.events.containsAll(
                listOf(
                    Event(payload = PhoneCallInfo(phone = "1", startTime = 0)),
                    Event(payload = PhoneCallInfo(phone = "3", startTime = 2))
                )
            )
        )
    }

    @Test
    fun testAddAll() {
        database.events.addAll(
            listOf(
                Event(payload = PhoneCallInfo(phone = "1", startTime = 0)),
                Event(payload = PhoneCallInfo(phone = "2", startTime = 1)),
                Event(payload = PhoneCallInfo(phone = "3", startTime = 2))
            )
        )

        assertEquals(3, database.events.size)
        assertEquals("3", (database.events.first().payload as PhoneCallInfo).phone)
        assertEquals("1", (database.events.last().payload as PhoneCallInfo).phone)
    }

    @Test
    fun testRemoveAll() {
        with(database) {
            events.addAll(
                listOf(
                    Event(payload = PhoneCallInfo(phone = "1", startTime = 0)),
                    Event(payload = PhoneCallInfo(phone = "2", startTime = 1)),
                    Event(payload = PhoneCallInfo(phone = "3", startTime = 2))
                )
            )

            events.removeAll(
                listOf(
                    Event(payload = PhoneCallInfo(phone = "1", startTime = 0)),
                    Event(payload = PhoneCallInfo(phone = "3", startTime = 2)),
                    Event(payload = PhoneCallInfo(phone = "33", startTime = 33))
                )
            )
        }

        assertEquals(1, database.events.size)
        assertEquals("2", (database.events.first().payload as PhoneCallInfo).phone)
    }

    @Test
    fun testRetainAll() {
        database.events.addAll(
            listOf(
                Event(payload = PhoneCallInfo(phone = "1", startTime = 0)),
                Event(payload = PhoneCallInfo(phone = "2", startTime = 1)),
                Event(payload = PhoneCallInfo(phone = "3", startTime = 2))
            )
        )

        database.events.retainAll(
            listOf(
                Event(payload = PhoneCallInfo(phone = "1", startTime = 0)),
                Event(payload = PhoneCallInfo(phone = "3", startTime = 2)),
                Event(payload = PhoneCallInfo(phone = "33", startTime = 33))
            )
        )

        assertEquals(2, database.events.size)
        assertEquals("3", (database.events.first().payload as PhoneCallInfo).phone)
        assertEquals("1", (database.events.last().payload as PhoneCallInfo).phone)
    }

    @Test
    fun testReplaceAll() {
        database.events.addAll(
            listOf(
                Event(payload = PhoneCallInfo(phone = "1", startTime = 0)),
                Event(payload = PhoneCallInfo(phone = "2", startTime = 1)),
                Event(payload = PhoneCallInfo(phone = "3", startTime = 2))
            )
        )

        database.events.replaceAll(
            listOf(
                Event(payload = PhoneCallInfo(phone = "11", startTime = 11)),
                Event(payload = PhoneCallInfo(phone = "22", startTime = 22))
            )
        )

        assertEquals(2, database.events.size)
        assertEquals("22", (database.events.first().payload as PhoneCallInfo).phone)
        assertEquals("11", (database.events.last().payload as PhoneCallInfo).phone)
    }

}