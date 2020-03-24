package com.bopr.android.smailer

import android.Manifest.permission.READ_CONTACTS
import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.content.Context.ACCOUNT_SERVICE
import android.content.Context.CONNECTIVITY_SERVICE
import android.content.SharedPreferences
import android.net.ConnectivityManager
import androidx.test.filters.SmallTest
import androidx.test.rule.GrantPermissionRule
import com.bopr.android.smailer.PhoneEvent.Companion.STATE_IGNORED
import com.bopr.android.smailer.PhoneEvent.Companion.STATE_PENDING
import com.bopr.android.smailer.PhoneEvent.Companion.STATE_PROCESSED
import com.bopr.android.smailer.PhoneEvent.Companion.STATUS_ACCEPTED
import com.bopr.android.smailer.PhoneEvent.Companion.STATUS_TRIGGER_OFF
import com.bopr.android.smailer.Settings.Companion.DEFAULT_EMAIL_CONTENT
import com.bopr.android.smailer.Settings.Companion.DEFAULT_TRIGGERS
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_CONTENT
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_LOCALE
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_TRIGGERS
import com.bopr.android.smailer.Settings.Companion.PREF_NOTIFY_SEND_SUCCESS
import com.bopr.android.smailer.Settings.Companion.PREF_RECIPIENTS_ADDRESS
import com.bopr.android.smailer.Settings.Companion.PREF_SENDER_ACCOUNT
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_DEFAULT
import com.nhaarman.mockitokotlin2.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import java.io.IOException
import java.lang.System.currentTimeMillis

/**
 * [CallProcessor] tester.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
@SmallTest
class CallProcessorTest : BaseTest() {

    @get:Rule
    var permissionRule: GrantPermissionRule = GrantPermissionRule.grant(READ_CONTACTS)

    private lateinit var database: Database
    private lateinit var context: Context
    private lateinit var transport: GoogleMail
    private lateinit var notifications: Notifications
    private lateinit var preferences: SharedPreferences
    private lateinit var geoLocator: GeoLocator
    private lateinit var processor: CallProcessor
    private lateinit var accountManager: AccountManager

    private fun testingEvent(text: String? = "Message"): PhoneEvent {
        val time = currentTimeMillis()
        return PhoneEvent(
                phone = "+123",
                isIncoming = true,
                startTime = time,
                endTime = time + 1000,
                isMissed = false,
                text = text,
                acceptor = "device"
        )
    }

    @Suppress("DEPRECATION")
    @Before
    fun setUp() {
        preferences = mock {
            on { getString(eq(PREF_SENDER_ACCOUNT), anyOrNull()) }.doReturn("sender@mail.com")
            on { getString(eq(PREF_RECIPIENTS_ADDRESS), anyOrNull()) }.doReturn("recipient@mail.com")
            on { getString(eq(PREF_EMAIL_LOCALE), anyOrNull()) }.doReturn(VAL_PREF_DEFAULT)
            on { getStringSet(eq(PREF_EMAIL_TRIGGERS), anyOrNull()) }.doReturn(DEFAULT_TRIGGERS)
            on { getStringSet(eq(PREF_EMAIL_CONTENT), anyOrNull()) }.doReturn(DEFAULT_EMAIL_CONTENT)
        }

        accountManager = mock {
            on { getAccountsByType(eq("com.google")) }.doReturn(arrayOf(Account("sender@mail.com", "com.google")))
        }

        val connectivityManager = mock<ConnectivityManager> {
            val networkInfo = mock<android.net.NetworkInfo> { /* do not inline. do not optimize imports */
                on { isConnectedOrConnecting }.doReturn(true)
            }
            on { activeNetworkInfo }.doReturn(networkInfo)
        }

        context = mock {
            on { contentResolver }.doReturn(targetContext.contentResolver)
            on { resources }.doReturn(targetContext.resources)
            on { getSharedPreferences(anyString(), anyInt()) }.doReturn(preferences)
            on { getSystemService(eq(ACCOUNT_SERVICE)) }.doReturn(accountManager)
            on { getSystemService(eq(CONNECTIVITY_SERVICE)) }.doReturn(connectivityManager)
        }

        geoLocator = mock {
            on { getLocation() }.doReturn(GeoCoordinates(60.0, 30.0))
        }

        transport = mock()
        notifications = mock()

        Database.databaseName = "test.sqlite"
        targetContext.deleteDatabase(Database.databaseName)
        database = Database(targetContext) /* not a mock context here! */

        processor = CallProcessor(context, database, transport, notifications, geoLocator)
    }

    @After
    fun tearDown() {
        database.close()
    }

    /**
     * Tests successful processing - mail sent.
     */
    @Test
    fun testProcessMailSent() {
        val event = testingEvent()
        processor.process(event)

        verify(transport).send(argThat {
            id == null
                    && subject == "[SMailer] Incoming SMS from \"+123\""
                    && !body.isNullOrBlank()
                    && attachment == null
                    && recipients == "recipient@mail.com"
                    && replyTo == null
                    && from == "sender@mail.com"
        })
        verify(notifications, never()).showSenderAccountError()
        verify(notifications, never()).showRecipientsError(anyInt())
        verify(notifications, never()).showGoogleAccessError()

        val savedEvent = database.events.first()

        assertEquals(event.acceptor, savedEvent.acceptor)
        assertEquals(event.startTime, savedEvent.startTime)
        assertEquals(event.phone, savedEvent.phone)
        assertEquals(STATE_PROCESSED, savedEvent.state)
        assertEquals(STATUS_ACCEPTED, savedEvent.processStatus)
        assertEquals(GeoCoordinates(60.0, 30.0), event.location)
    }

    /**
     * Tests successful processing - event ignored.
     */
    @Test
    fun testProcessIgnored() {
        /* make the event not an SMS then default filter will deny it */
        val event = testingEvent(null)

        processor.process(event)

        verify(transport, never()).send(any())
        verify(notifications, never()).showSenderAccountError()
        verify(notifications, never()).showRecipientsError(anyInt())
        verify(notifications, never()).showGoogleAccessError()

        val savedEvent = database.events.first()

        assertEquals(STATE_IGNORED, savedEvent.state)
        assertEquals(STATUS_TRIGGER_OFF, savedEvent.processStatus)
    }

    /**
     * Test processing when sender account setting is not specified.
     */
    @Test
    fun testProcessNoSender() {
        whenever(preferences.getString(eq(PREF_SENDER_ACCOUNT), anyOrNull())).thenReturn(null)

        val event = testingEvent()
        processor.process(event)

        verify(transport, never()).login(any(), any())
        verify(transport, never()).send(any())
        verify(notifications).showSenderAccountError()

        val savedEvent = database.events.first()

        assertEquals(STATE_PENDING, savedEvent.state)
        assertEquals(STATUS_ACCEPTED, savedEvent.processStatus)
    }

    /**
     * Test processing when recipients setting is not specified.
     */
    @Test
    fun testProcessNoRecipients() {
        whenever(preferences.getString(eq(PREF_RECIPIENTS_ADDRESS), anyOrNull())).thenReturn(null)

        val event = testingEvent()
        processor.process(event)

        verify(transport, never()).send(any())
        verify(notifications).showRecipientsError(eq(R.string.no_recipients_specified))

        val savedEvent = database.events.first()

        assertEquals(STATE_PENDING, savedEvent.state)
        assertEquals(STATUS_ACCEPTED, savedEvent.processStatus)
    }

    /**
     * Transport exception during send does not produce any notifications.
     */
    @Test
    fun testProcessTransportSendFailed() {
        doThrow(IOException("Test error")).whenever(transport).send(any())

        val event = testingEvent()
        processor.process(event)

        verify(transport).login(any(), any())
        verify(transport).send(any())
        verify(notifications, never()).showSenderAccountError()
        verify(notifications, never()).showRecipientsError(anyInt())
        verify(notifications, never()).showGoogleAccessError()

        val savedEvent = database.events.first()

        assertEquals(STATE_PENDING, savedEvent.state)
        assertEquals(STATUS_ACCEPTED, savedEvent.processStatus)
    }

    /**
     * When [PREF_NOTIFY_SEND_SUCCESS] setting is set to true then success notification should be shown.
     */
    @Test
    fun testSuccessNotification() {
        /* the setting is OFF */
        whenever(preferences.getBoolean(eq(PREF_NOTIFY_SEND_SUCCESS), anyOrNull())).thenReturn(false)

        processor.process(testingEvent())

        verify(notifications, never()).showMailSendSuccess()

        /* the setting is ON */
        whenever(preferences.getBoolean(eq(PREF_NOTIFY_SEND_SUCCESS), anyOrNull())).thenReturn(true)

        processor.process(testingEvent())

        verify(notifications).showMailSendSuccess()
    }

    /**
     * Test resending pending messages.
     */
    @Test
    fun testProcessPending() {
        /* disable transport */
        doThrow(IOException("Test error")).whenever(transport).send(any())

        processor.process(testingEvent())
        processor.process(testingEvent())
        processor.process(testingEvent())

        assertEquals(3, database.events.size)
        assertEquals(3, database.events.filterPending.size)
        verify(notifications, never()).showSenderAccountError()
        verify(notifications, never()).showRecipientsError(anyInt())
        verify(notifications, never()).showGoogleAccessError()

        /* try resend with disabled transport */
        processor.processPending()

        assertEquals(3, database.events.size)
        assertEquals(3, database.events.filterPending.size)
        verify(notifications, never()).showSenderAccountError()
        verify(notifications, never()).showRecipientsError(anyInt())
        verify(notifications, never()).showGoogleAccessError()

        /* enable transport an try again */
        doNothing().whenever(transport).send(any())

        processor.processPending()

        assertEquals(3, database.events.size)
        assertEquals(0, database.events.filterPending.size)
        verify(notifications, never()).showSenderAccountError()
        verify(notifications, never()).showRecipientsError(anyInt())
        verify(notifications, never()).showGoogleAccessError()
    }
}