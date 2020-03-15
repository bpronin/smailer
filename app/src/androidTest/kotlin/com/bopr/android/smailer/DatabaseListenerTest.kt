package com.bopr.android.smailer

import android.content.BroadcastReceiver
import androidx.test.filters.SmallTest
import com.bopr.android.smailer.Database.Companion.registerDatabaseListener
import com.bopr.android.smailer.Database.Companion.unregisterDatabaseListener
import org.junit.After
import org.junit.Assert.assertTrue
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
        database = Database(targetContext, "test.sqlite").apply { clean() }
    }

    @After
    fun tearDown() {
        targetContext.unregisterDatabaseListener(listener)
        database.close()
    }

    @Test
    fun testListener() {
        val latch = CountDownLatch(1)
        listener = targetContext.registerDatabaseListener {
            latch.countDown()
        }

        database.putEvent(PhoneEvent(phone = "1", startTime = 0, acceptor = "device"))
        database.putEvent(PhoneEvent(phone = "2", startTime = 1, acceptor = "device"))
        database.putEvent(PhoneEvent(phone = "3", startTime = 2, acceptor = "device"))
        database.notifyChanged()

        assertTrue(latch.await(5, TimeUnit.SECONDS))
    }

}