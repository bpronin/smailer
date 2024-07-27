package com.bopr.android.smailer.processor.mail

import android.Manifest
import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.test.filters.SmallTest
import androidx.test.rule.GrantPermissionRule
import com.bopr.android.smailer.BaseTest
import com.bopr.android.smailer.NotificationsHelper
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_MESSAGE_CONTENT
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_SENDER_ACCOUNT
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_TRIGGERS
import com.bopr.android.smailer.Settings.Companion.PREF_MESSAGE_LOCALE
import com.bopr.android.smailer.Settings.Companion.PREF_RECIPIENTS_ADDRESS
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_MESSAGE_CONTENT_BODY
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_MESSAGE_CONTENT_CALLER
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_MESSAGE_CONTENT_CONTROL_LINKS
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_MESSAGE_CONTENT_DEVICE_NAME
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_MESSAGE_CONTENT_DISPATCH_TIME
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_MESSAGE_CONTENT_EVENT_TIME
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_MESSAGE_CONTENT_HEADER
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_MESSAGE_CONTENT_LOCATION
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_TRIGGER_IN_SMS
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_TRIGGER_MISSED_CALLS
import com.bopr.android.smailer.processor.EventDispatcher
import com.bopr.android.smailer.data.Database
import com.bopr.android.smailer.provider.EventState.Companion.STATE_IGNORED
import com.bopr.android.smailer.provider.EventState.Companion.STATE_PENDING
import com.bopr.android.smailer.provider.EventState.Companion.STATE_PROCESSED
import com.bopr.android.smailer.provider.telephony.PhoneEventData
import com.bopr.android.smailer.provider.telephony.PhoneEventProcessor
import com.bopr.android.smailer.util.GeoLocation
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers
import java.io.IOException

/**
 * [PhoneEventProcessor] tester.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
@SmallTest
class PhoneEventProcessorTest : BaseTest() {

    @get:Rule
    var permissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(Manifest.permission.READ_CONTACTS)

    private lateinit var database: Database
    private lateinit var context: Context
    private lateinit var messenger: EventDispatcher
    private lateinit var notifications: NotificationsHelper
    private lateinit var preferences: SharedPreferences
    private lateinit var processor: PhoneEventProcessor
    private lateinit var accountManager: AccountManager

    private fun testingEvent(text: String? = "Message"): PhoneEventData {
        val time = System.currentTimeMillis()
        return PhoneEventData(
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
            on {
                getString(
                    eq(PREF_EMAIL_SENDER_ACCOUNT),
                    anyOrNull()
                )
            }.doReturn("sender@mail.com")
            on {
                getString(
                    eq(PREF_RECIPIENTS_ADDRESS),
                    anyOrNull()
                )
            }.doReturn("recipient@mail.com")
            on {
                getString(
                    eq(PREF_MESSAGE_LOCALE),
                    anyOrNull()
                )
            }.doReturn(Settings.VAL_PREF_DEFAULT)
            on {
                getStringSet(
                    eq(PREF_EMAIL_TRIGGERS),
                    anyOrNull()
                )
            }.doReturn(
                setOf(
                    VAL_PREF_TRIGGER_IN_SMS,
                    VAL_PREF_TRIGGER_MISSED_CALLS
                )
            )
            on {
                getStringSet(
                    eq(PREF_EMAIL_MESSAGE_CONTENT),
                    anyOrNull()
                )
            }.doReturn(
                setOf(
                    VAL_PREF_MESSAGE_CONTENT_BODY,
                    VAL_PREF_MESSAGE_CONTENT_CALLER,
                    VAL_PREF_MESSAGE_CONTENT_DEVICE_NAME,
                    VAL_PREF_MESSAGE_CONTENT_HEADER,
                    VAL_PREF_MESSAGE_CONTENT_LOCATION,
                    VAL_PREF_MESSAGE_CONTENT_EVENT_TIME,
                    VAL_PREF_MESSAGE_CONTENT_DISPATCH_TIME,
                    VAL_PREF_MESSAGE_CONTENT_CONTROL_LINKS
                )
            )
        }

        accountManager = mock {
            on { getAccountsByType(eq("com.google")) }.doReturn(
                arrayOf(
                    Account(
                        "sender@mail.com",
                        "com.google"
                    )
                )
            )
        }

        val connectivityManager = mock<ConnectivityManager> {
            val networkInfo = mock<NetworkInfo> { /* do not inline. do not optimize imports */
                on { isConnectedOrConnecting }.doReturn(true)
            }
            on { activeNetworkInfo }.doReturn(networkInfo)
        }

        context = mock {
            on { contentResolver }.doReturn(targetContext.contentResolver)
            on { resources }.doReturn(targetContext.resources)
            on {
                getSharedPreferences(
                    ArgumentMatchers.anyString(),
                    ArgumentMatchers.anyInt()
                )
            }.doReturn(preferences)
            on { getSystemService(eq(Context.ACCOUNT_SERVICE)) }.doReturn(accountManager)
            on { getSystemService(eq(Context.CONNECTIVITY_SERVICE)) }.doReturn(connectivityManager)
        }

        messenger = mock()
        notifications = mock()

        Database.databaseName = "test.sqlite"
        targetContext.deleteDatabase(Database.databaseName)
        database = Database(targetContext) /* not a mock context here! */

        processor = PhoneEventProcessor(context, database, messenger, notifications)
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

//        verify(messenger).sendMessagesFor(argThat {
//            id == null
//                    && subject == "[SMailer] Incoming SMS from \"+123\""
//                    && !body.isNullOrBlank()
//                    && attachment == null
//                    && recipients == "recipient@mail.com"
//                    && replyTo == null
//                    && from == "sender@mail.com"
//        })
        verify(notifications, never()).showAccountError()
        verify(notifications, never()).showRecipientsError(ArgumentMatchers.anyInt())
        verify(notifications, never()).showGoogleAccessError()

        val savedEvent = database.phoneEvents.first()

        Assert.assertEquals(event.acceptor, savedEvent.acceptor)
        Assert.assertEquals(event.startTime, savedEvent.startTime)
        Assert.assertEquals(event.phone, savedEvent.phone)
        Assert.assertEquals(STATE_PROCESSED, savedEvent.state)
        Assert.assertEquals(PhoneEventData.STATUS_ACCEPTED, savedEvent.processStatus)
        Assert.assertEquals(GeoLocation(60.0, 30.0), event.location)
    }

    /**
     * Tests successful processing - event ignored.
     */
    @Test
    fun testProcessIgnored() {
        /* make the event not an SMS then default filter will deny it */
        val event = testingEvent(null)

        processor.process(event)

        verify(messenger, never()).dispatch(any())
        verify(notifications, never()).showAccountError()
        verify(notifications, never()).showRecipientsError(ArgumentMatchers.anyInt())
        verify(notifications, never()).showGoogleAccessError()

        val savedEvent = database.phoneEvents.first()

        Assert.assertEquals(STATE_IGNORED, savedEvent.state)
        Assert.assertEquals(PhoneEventData.STATUS_TRIGGER_OFF, savedEvent.processStatus)
    }

    /**
     * Test processing when sender account setting is unspecified.
     */
    @Test
    fun testProcessNoSender() {
        whenever(preferences.getString(eq(PREF_EMAIL_SENDER_ACCOUNT), anyOrNull())).thenReturn(null)

        val event = testingEvent()
        processor.process(event)

//        verify(messenger, never()).login(any(), any())
        verify(messenger, never()).dispatch(any())
        verify(notifications).showAccountError()

        val savedEvent = database.phoneEvents.first()

        Assert.assertEquals(STATE_PENDING, savedEvent.state)
        Assert.assertEquals(PhoneEventData.STATUS_ACCEPTED, savedEvent.processStatus)
    }

    /**
     * Test processing when recipients setting is unspecified.
     */
    @Test
    fun testProcessNoRecipients() {
        whenever(preferences.getString(eq(PREF_RECIPIENTS_ADDRESS), anyOrNull())).thenReturn(null)

        val event = testingEvent()
        processor.process(event)

        verify(messenger, never()).dispatch(any())
        verify(notifications).showRecipientsError(eq(R.string.no_recipients_specified))

        val savedEvent = database.phoneEvents.first()

        Assert.assertEquals(STATE_PENDING, savedEvent.state)
        Assert.assertEquals(PhoneEventData.STATUS_ACCEPTED, savedEvent.processStatus)
    }

    /**
     * Transport exception during send does not produce any notifications.
     */
    @Test
    fun testProcessTransportSendFailed() {
        doThrow(IOException("Test error")).whenever(messenger).dispatch(any())

        val event = testingEvent()
        processor.process(event)

//        verify(messenger).login(any(), any())
        verify(messenger).dispatch(any())
        verify(notifications, never()).showAccountError()
        verify(notifications, never()).showRecipientsError(ArgumentMatchers.anyInt())
        verify(notifications, never()).showGoogleAccessError()

        val savedEvent = database.phoneEvents.first()

        Assert.assertEquals(STATE_PENDING, savedEvent.state)
        Assert.assertEquals(PhoneEventData.STATUS_ACCEPTED, savedEvent.processStatus)
    }

    /**
     * When [PREF_NOTIFY_SEND_SUCCESS] setting is set to true then success notification should be shown.
     */
    @Test
    fun testSuccessNotification() {
        /* the setting is OFF */
        whenever(preferences.getBoolean(eq(Settings.PREF_NOTIFY_SEND_SUCCESS), anyOrNull())).thenReturn(false)

        processor.process(testingEvent())

        verify(notifications, never()).showMailSendSuccess()

        /* the setting is ON */
        whenever(preferences.getBoolean(eq(Settings.PREF_NOTIFY_SEND_SUCCESS), anyOrNull())).thenReturn(true)

        processor.process(testingEvent())

        verify(notifications).showMailSendSuccess()
    }

    /**
     * Test resending pending messages.
     */
    @Test
    fun testProcessPending() {
        /* disable transport */
        doThrow(IOException("Test error")).whenever(messenger).dispatch(any())

        processor.process(testingEvent())
        processor.process(testingEvent())
        processor.process(testingEvent())

        Assert.assertEquals(3, database.phoneEvents.size)
        Assert.assertEquals(3, database.phoneEvents.filterPending.size)
        verify(notifications, never()).showAccountError()
        verify(notifications, never()).showRecipientsError(ArgumentMatchers.anyInt())
        verify(notifications, never()).showGoogleAccessError()

        /* try resend with disabled transport */
        processor.processPending()

        Assert.assertEquals(3, database.phoneEvents.size)
        Assert.assertEquals(3, database.phoneEvents.filterPending.size)
        verify(notifications, never()).showAccountError()
        verify(notifications, never()).showRecipientsError(ArgumentMatchers.anyInt())
        verify(notifications, never()).showGoogleAccessError()

        /* enable transport an try again */
        doNothing().whenever(messenger).dispatch(any())

        processor.processPending()

        Assert.assertEquals(3, database.phoneEvents.size)
        Assert.assertEquals(0, database.phoneEvents.filterPending.size)
        verify(notifications, never()).showAccountError()
        verify(notifications, never()).showRecipientsError(ArgumentMatchers.anyInt())
        verify(notifications, never()).showGoogleAccessError()
    }
}