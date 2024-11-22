package com.bopr.android.smailer.data

import android.content.BroadcastReceiver
import androidx.test.filters.SmallTest
import com.bopr.android.smailer.BaseTest
import com.bopr.android.smailer.data.Database.Companion.database
import com.bopr.android.smailer.messenger.Event
import com.bopr.android.smailer.provider.telephony.PhoneCallInfo
import org.junit.After
import org.junit.Assert
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
        listener = database.registerListener {
            latch.countDown()
        }

        database.commit {
            batch {
                events.add(Event(payload = PhoneCallInfo(phone = "1", startTime = 0)))
                events.add(Event(payload = PhoneCallInfo(phone = "2", startTime = 0)))
                events.add(Event(payload = PhoneCallInfo(phone = "3", startTime = 0)))
            }
        }

        Assert.assertTrue(latch.await(5, TimeUnit.SECONDS))
    }

}