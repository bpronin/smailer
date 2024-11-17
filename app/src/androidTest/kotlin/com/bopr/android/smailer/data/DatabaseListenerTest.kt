package com.bopr.android.smailer.data

import android.content.BroadcastReceiver
import androidx.test.filters.SmallTest
import com.bopr.android.smailer.BaseTest
import com.bopr.android.smailer.data.Database.Companion.registerDatabaseListener
import com.bopr.android.smailer.data.Database.Companion.unregisterDatabaseListener
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
        database = Database(targetContext)
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

        database.commit {
            batch {
                phoneCalls.add(PhoneCallInfo(phone = "1", startTime = 0, acceptor = "device"))
                phoneCalls.add(PhoneCallInfo(phone = "2", startTime = 1, acceptor = "device"))
                phoneCalls.add(PhoneCallInfo(phone = "3", startTime = 2, acceptor = "device"))
            }
        }

        Assert.assertTrue(latch.await(5, TimeUnit.SECONDS))
    }

}