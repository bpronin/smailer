package com.bopr.android.smailer

import android.Manifest.permission.READ_CONTACTS
import android.Manifest.permission.WRITE_CONTACTS
import android.accounts.AccountsException
import android.content.Context
import android.content.SharedPreferences
import androidx.test.rule.GrantPermissionRule
import com.bopr.android.smailer.Notifications.Companion.ACTION_SHOW_MAIN
import com.bopr.android.smailer.PhoneEvent.Companion.REASON_ACCEPTED
import com.bopr.android.smailer.PhoneEvent.Companion.REASON_TRIGGER_OFF
import com.bopr.android.smailer.PhoneEvent.Companion.STATE_IGNORED
import com.bopr.android.smailer.PhoneEvent.Companion.STATE_PENDING
import com.bopr.android.smailer.PhoneEvent.Companion.STATE_PROCESSED
import com.bopr.android.smailer.Settings.Companion.DEFAULT_CONTENT
import com.bopr.android.smailer.Settings.Companion.DEFAULT_TRIGGERS
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_CONTENT
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_LOCALE
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_TRIGGERS
import com.bopr.android.smailer.Settings.Companion.PREF_NOTIFY_SEND_SUCCESS
import com.bopr.android.smailer.Settings.Companion.PREF_RECIPIENTS_ADDRESS
import com.bopr.android.smailer.Settings.Companion.PREF_SENDER_ACCOUNT
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_DEFAULT
import com.google.api.services.gmail.GmailScopes.GMAIL_SEND
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
class CallProcessorTest : BaseTest() {

    @Rule
    @JvmField
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(READ_CONTACTS, WRITE_CONTACTS)

    private lateinit var database: Database
    private lateinit var context: Context
    private lateinit var transport: GoogleMail
    private lateinit var notifications: Notifications
    private lateinit var preferences: SharedPreferences
    private lateinit var geoLocator: GeoLocator
    private lateinit var processor: CallProcessor

    private fun testingEvent(text: String? = "Message"): PhoneEvent {
        val time = currentTimeMillis()
        return PhoneEvent(
                phone = "+123",
                isIncoming = true,
                isMissed = false,
                startTime = time,
                endTime = time + 1000,
                text = text,
                acceptor = "device"
        )
    }

    @Before
    fun setUp() {
        preferences = mock {
            on { getString(eq(PREF_SENDER_ACCOUNT), anyOrNull()) }.doReturn("sender@mail.com")
            on { getString(eq(PREF_RECIPIENTS_ADDRESS), anyOrNull()) }.doReturn("recipient@mail.com")
            on { getString(eq(PREF_EMAIL_LOCALE), anyOrNull()) }.doReturn(VAL_PREF_DEFAULT)
            on { getStringSet(eq(PREF_EMAIL_TRIGGERS), anyOrNull()) }.doReturn(DEFAULT_TRIGGERS)
            on { getStringSet(eq(PREF_EMAIL_CONTENT), anyOrNull()) }.doReturn(DEFAULT_CONTENT)
        }

        context = mock {
            on { contentResolver }.doReturn(targetContext.contentResolver)
            on { resources }.doReturn(targetContext.resources)
            on { getSharedPreferences(anyString(), anyInt()) }.doReturn(preferences)
        }

        geoLocator = mock {
            on { getLocation() }.doReturn(GeoCoordinates(60.0, 30.0))
        }

        transport = mock()
        notifications = mock()

        database = Database(targetContext, "test.sqlite") /* not a mock context here! */
        database.destroy()

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

        verify(transport).startSession(eq("sender@mail.com"), eq(GMAIL_SEND))
        verify(transport).send(argThat {
            id == null
                    && subject == "[SMailer] Incoming SMS from \"+123\""
                    && !body.isNullOrBlank()
                    && attachment == null
                    && recipients == "recipient@mail.com"
                    && replyTo == null
                    && from == "sender@mail.com"
        })
        verify(notifications, never()).showMailError(any(), any())

        val savedEvent = database.events.findFirst()!!

        assertEquals(event.acceptor, savedEvent.acceptor)
        assertEquals(event.startTime, savedEvent.startTime)
        assertEquals(event.phone, savedEvent.phone)
        assertEquals(STATE_PROCESSED, savedEvent.state)
        assertEquals(REASON_ACCEPTED, savedEvent.stateReason)
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

        verify(transport, never()).startSession(any(), any())
        verify(transport, never()).send(any())
        verify(notifications, never()).showMailError(any(), any())

        val savedEvent = database.events.findFirst()!!

        assertEquals(STATE_IGNORED, savedEvent.state)
        assertEquals(REASON_TRIGGER_OFF, savedEvent.stateReason)
    }

    /**
     * Test processing when sender account is not specified.
     */
    @Test
    fun testProcessNoSender() {
        whenever(preferences.getString(eq(PREF_SENDER_ACCOUNT), anyOrNull())).thenReturn(null)

        val event = testingEvent()
        processor.process(event)

        verify(transport, never()).startSession(any(), any())
        verify(transport, never()).send(any())
        verify(notifications).showMailError(eq(R.string.no_account_specified), eq(ACTION_SHOW_MAIN))

        val savedEvent = database.events.findFirst()!!

        assertEquals(STATE_PENDING, savedEvent.state)
        assertEquals(REASON_ACCEPTED, savedEvent.stateReason)
    }

    /**
     * Test processing when no recipients specified.
     */
    @Test
    fun testProcessNoRecipients() {
        whenever(preferences.getString(eq(PREF_RECIPIENTS_ADDRESS), anyOrNull())).thenReturn(null)

        val event = testingEvent()
        processor.process(event)

        verify(transport, never()).startSession(any(), any())
        verify(transport, never()).send(any())
        verify(notifications).showMailError(eq(R.string.no_recipients_specified), eq(ACTION_SHOW_MAIN))

        val savedEvent = database.events.findFirst()!!

        assertEquals(STATE_PENDING, savedEvent.state)
        assertEquals(REASON_ACCEPTED, savedEvent.stateReason)
    }

    /**
     * Tests processing when mail transport produces init error.
     */
    @Test
    fun testProcessTransportInitFailed() {
        doThrow(AccountsException("Test error")).whenever(transport).startSession(any(), any())

        val event = testingEvent()
        processor.process(event)

        verify(transport).startSession(any(), any())
        verify(transport, never()).send(any())
        verify(notifications).showMailError(eq(R.string.account_not_registered), eq(ACTION_SHOW_MAIN))

        val savedEvent = database.events.findFirst()!!

        assertEquals(STATE_PENDING, savedEvent.state)
        assertEquals(REASON_ACCEPTED, savedEvent.stateReason)
    }

    /**
     * Transport exception during send does not produce any notifications.
     */
    @Test
    fun testProcessTransportSendFailed() {
        doThrow(IOException("Test error")).whenever(transport).send(any())

        val event = testingEvent()
        processor.process(event)

        verify(transport).startSession(eq("sender@mail.com"), eq(GMAIL_SEND))
        verify(transport).send(any())
        verify(notifications, never()).showMailError(any(), any())

        val savedEvent = database.events.findFirst()!!

        assertEquals(STATE_PENDING, savedEvent.state)
        assertEquals(REASON_ACCEPTED, savedEvent.stateReason)
    }

    /**
     * When settings goes back to normal last error notification should be removed.
     */
    @Test
    fun testClearNotifications() {
        doThrow(AccountsException("Test error")).whenever(transport).startSession(any(), any())

        val event = testingEvent()
        processor.process(event)

        verify(notifications).showMailError(eq(R.string.account_not_registered), eq(ACTION_SHOW_MAIN))
        verify(notifications, never()).hideAllErrors()

        /* sending without errors hides all previous error notifications */
        doNothing().whenever(transport).startSession(any(), any())

        processor.process(testingEvent())

        verify(notifications).showMailError(eq(R.string.account_not_registered), eq(ACTION_SHOW_MAIN))
        verify(notifications).hideAllErrors()
    }

    /**
     * When [PREF_NOTIFY_SEND_SUCCESS] setting is set to true then success notification should be shown.
     */
    @Test
    fun testSuccessNotification() {
        /* the setting is OFF */
        whenever(preferences.getBoolean(eq(PREF_NOTIFY_SEND_SUCCESS), anyOrNull())).thenReturn(false)

        processor.process(testingEvent())

        verify(notifications, never()).showMessage(any(), any())

        /* the setting is ON */
        whenever(preferences.getBoolean(eq(PREF_NOTIFY_SEND_SUCCESS), anyOrNull())).thenReturn(true)

        processor.process(testingEvent())

        verify(notifications).showMessage(eq(R.string.email_send), eq(ACTION_SHOW_MAIN))
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

        assertEquals(3, database.events.getCount())
        assertEquals(3, database.pendingEvents.getCount())
        verify(notifications, never()).showError(any(), any())

        /* try resend with disabled transport */
        processor.processPending()

        assertEquals(3, database.events.getCount())
        assertEquals(3, database.pendingEvents.getCount())
        verify(notifications, never()).showError(any(), any())

        /* enable transport an try again */
        doNothing().whenever(transport).send(any())

        processor.processPending()

        assertEquals(3, database.events.getCount())
        assertEquals(0, database.pendingEvents.getCount())
        verify(notifications, never()).showError(any(), any())
    }
}