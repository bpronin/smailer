package com.bopr.android.smailer.data

import android.content.BroadcastReceiver
import androidx.test.filters.SmallTest
import com.bopr.android.smailer.BaseTest
import com.bopr.android.smailer.data.Database.Companion.TABLE_EVENTS
import com.bopr.android.smailer.data.Database.Companion.TABLE_PHONE_CALLS
import com.bopr.android.smailer.data.Database.Companion.database
import com.bopr.android.smailer.messenger.Event
import com.bopr.android.smailer.provider.telephony.PhoneCallInfo
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * [Database] class tester.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
@SmallTest
class DatabaseListenerTest : BaseTest() {

    private lateinit var database: Database
    private lateinit var listener: BroadcastReceiver

    @Before
    fun setUp() {
        Database.databaseName = "test.sqlite"
        targetContext.deleteDatabase(Database.databaseName)
        database = targetContext.database
    }

    @After
    fun tearDown() {
        database.unregisterListener(listener)
        database.close()
    }

    @Test
    fun testListener() {
        val latch = CountDownLatch(1)
        var modifications: Set<String>? = null

        listener = database.registerListener {
            modifications = it
            latch.countDown()
        }

        database.commit {
            events.apply {
                add(Event(timestamp = 1, payload = PhoneCallInfo(startTime = 0, phone = "1")))
                add(Event(timestamp = 2, payload = PhoneCallInfo(startTime = 0, phone = "2")))
                add(Event(timestamp = 3, payload = PhoneCallInfo(startTime = 0, phone = "3")))
            }
        }

        assertTrue(latch.await(5, TimeUnit.SECONDS))
        assertNotNull(modifications)
        modifications?.run {
            assertTrue(containsAll(setOf(TABLE_EVENTS, TABLE_PHONE_CALLS)))
        }
    }

}