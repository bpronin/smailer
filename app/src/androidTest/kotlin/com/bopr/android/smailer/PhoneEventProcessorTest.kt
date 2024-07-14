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
import com.bopr.android.smailer.provider.telephony.PhoneEventInfo.Companion.STATE_IGNORED
import com.bopr.android.smailer.provider.telephony.PhoneEventInfo.Companion.STATE_PENDING
import com.bopr.android.smailer.provider.telephony.PhoneEventInfo.Companion.STATE_PROCESSED
import com.bopr.android.smailer.provider.telephony.PhoneEventInfo.Companion.STATUS_ACCEPTED
import com.bopr.android.smailer.provider.telephony.PhoneEventInfo.Companion.STATUS_TRIGGER_OFF
import com.bopr.android.smailer.Settings.Companion.DEFAULT_EMAIL_CONTENT
import com.bopr.android.smailer.Settings.Companion.DEFAULT_TRIGGERS
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_CONTENT
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_LOCALE
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_TRIGGERS
import com.bopr.android.smailer.Settings.Companion.PREF_NOTIFY_SEND_SUCCESS
import com.bopr.android.smailer.Settings.Companion.PREF_RECIPIENTS_ADDRESS
import com.bopr.android.smailer.Settings.Companion.PREF_SENDER_ACCOUNT
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_DEFAULT
import com.bopr.android.smailer.provider.telephony.PhoneEventProcessor
import com.bopr.android.smailer.consumer.EventMessenger
import com.bopr.android.smailer.data.Database
import com.bopr.android.smailer.provider.telephony.PhoneEventInfo
import com.bopr.android.smailer.util.GeoCoordinates
import com.bopr.android.smailer.util.GeoLocator
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
 * [PhoneEventProcessor] tester.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
@SmallTest
class PhoneEventProcessorTest : BaseTest() {

    @get:Rule
    var permissionRule: GrantPermissionRule = GrantPermissionRule.grant(READ_CONTACTS)

    private lateinit var database: Database
    private lateinit var context: Context
    private lateinit var messenger: EventMessenger
    private lateinit var notifications: NotificationsHelper
    private lateinit var preferences: SharedPreferences
    private lateinit var geoLocator: GeoLocator
    private lateinit var processor: PhoneEventProcessor
    private lateinit var accountManager: AccountManager

    private fun testingEvent(text: String? = "Message"): PhoneEventInfo {
        val time = currentTimeMillis()
        return PhoneEventInfo(
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

        messenger = mock()
        notifications = mock()

        Database.databaseName = "test.sqlite"
        targetContext.deleteDatabase(Database.databaseName)
        database = Database(targetContext) /* not a mock context here! */

        processor = PhoneEventProcessor(context, database, messenger, notifications, geoLocator)
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
        processor.processEvent(event)

//        verify(messenger).sendMessages(argThat {
//            id == null
//                    && subject == "[SMailer] Incoming SMS from \"+123\""
//                    && !body.isNullOrBlank()
//                    && attachment == null
//                    && recipients == "recipient@mail.com"
//                    && replyTo == null
//                    && from == "sender@mail.com"
//        })
        verify(notifications, never()).showSenderAccountError()
        verify(notifications, never()).showRecipientsError(anyInt())
        verify(notifications, never()).showGoogleAccessError()

        val savedEvent = database.phoneEvents.first()

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

        processor.processEvent(event)

        verify(messenger, never()).sendMessages(any())
        verify(notifications, never()).showSenderAccountError()
        verify(notifications, never()).showRecipientsError(anyInt())
        verify(notifications, never()).showGoogleAccessError()

        val savedEvent = database.phoneEvents.first()

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
        processor.processEvent(event)

//        verify(messenger, never()).login(any(), any())
        verify(messenger, never()).sendMessages(any())
        verify(notifications).showSenderAccountError()

        val savedEvent = database.phoneEvents.first()

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
        processor.processEvent(event)

        verify(messenger, never()).sendMessages(any())
        verify(notifications).showRecipientsError(eq(R.string.no_recipients_specified))

        val savedEvent = database.phoneEvents.first()

        assertEquals(STATE_PENDING, savedEvent.state)
        assertEquals(STATUS_ACCEPTED, savedEvent.processStatus)
    }

    /**
     * Transport exception during send does not produce any notifications.
     */
    @Test
    fun testProcessTransportSendFailed() {
        doThrow(IOException("Test error")).whenever(messenger).sendMessages(any())

        val event = testingEvent()
        processor.processEvent(event)

//        verify(messenger).login(any(), any())
        verify(messenger).sendMessages(any())
        verify(notifications, never()).showSenderAccountError()
        verify(notifications, never()).showRecipientsError(anyInt())
        verify(notifications, never()).showGoogleAccessError()

        val savedEvent = database.phoneEvents.first()

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

        processor.processEvent(testingEvent())

        verify(notifications, never()).showMailSendSuccess()

        /* the setting is ON */
        whenever(preferences.getBoolean(eq(PREF_NOTIFY_SEND_SUCCESS), anyOrNull())).thenReturn(true)

        processor.processEvent(testingEvent())

        verify(notifications).showMailSendSuccess()
    }

    /**
     * Test resending pending messages.
     */
    @Test
    fun testProcessPending() {
        /* disable transport */
        doThrow(IOException("Test error")).whenever(messenger).sendMessages(any())

        processor.processEvent(testingEvent())
        processor.processEvent(testingEvent())
        processor.processEvent(testingEvent())

        assertEquals(3, database.phoneEvents.size)
        assertEquals(3, database.phoneEvents.filterPending.size)
        verify(notifications, never()).showSenderAccountError()
        verify(notifications, never()).showRecipientsError(anyInt())
        verify(notifications, never()).showGoogleAccessError()

        /* try resend with disabled transport */
        processor.processPending()

        assertEquals(3, database.phoneEvents.size)
        assertEquals(3, database.phoneEvents.filterPending.size)
        verify(notifications, never()).showSenderAccountError()
        verify(notifications, never()).showRecipientsError(anyInt())
        verify(notifications, never()).showGoogleAccessError()

        /* enable transport an try again */
        doNothing().whenever(messenger).sendMessages(any())

        processor.processPending()

        assertEquals(3, database.phoneEvents.size)
        assertEquals(0, database.phoneEvents.filterPending.size)
        verify(notifications, never()).showSenderAccountError()
        verify(notifications, never()).showRecipientsError(anyInt())
        verify(notifications, never()).showGoogleAccessError()
    }
}