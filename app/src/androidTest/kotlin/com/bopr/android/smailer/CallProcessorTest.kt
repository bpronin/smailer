package com.bopr.android.smailer

import android.Manifest.permission.READ_CONTACTS
import android.Manifest.permission.WRITE_CONTACTS
import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.content.SharedPreferences
import androidx.test.rule.GrantPermissionRule
import androidx.test.rule.GrantPermissionRule.grant
import com.bopr.android.smailer.Notifications.Companion.TARGET_MAIN
import com.bopr.android.smailer.Notifications.Companion.TARGET_RECIPIENTS
import com.bopr.android.smailer.PhoneEvent.Companion.STATE_IGNORED
import com.bopr.android.smailer.PhoneEvent.Companion.STATE_PENDING
import com.bopr.android.smailer.PhoneEvent.Companion.STATE_PROCESSED
import com.bopr.android.smailer.PhoneEvent.Companion.STATUS_ACCEPTED
import com.bopr.android.smailer.PhoneEvent.Companion.STATUS_TRIGGER_OFF
import com.bopr.android.smailer.Settings.Companion.DEFAULT_CONTENT
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
class CallProcessorTest : BaseTest() {

    @Rule
    @JvmField
    val permissionRule: GrantPermissionRule = grant(READ_CONTACTS, WRITE_CONTACTS)

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

    @Before
    fun setUp() {
        preferences = mock {
            on { getString(eq(PREF_SENDER_ACCOUNT), anyOrNull()) }.doReturn("sender@mail.com")
            on { getString(eq(PREF_RECIPIENTS_ADDRESS), anyOrNull()) }.doReturn("recipient@mail.com")
            on { getString(eq(PREF_EMAIL_LOCALE), anyOrNull()) }.doReturn(VAL_PREF_DEFAULT)
            on { getStringSet(eq(PREF_EMAIL_TRIGGERS), anyOrNull()) }.doReturn(DEFAULT_TRIGGERS)
            on { getStringSet(eq(PREF_EMAIL_CONTENT), anyOrNull()) }.doReturn(DEFAULT_CONTENT)
        }

        accountManager = mock {
            on { getAccountsByType(eq("com.google")) }.doReturn(arrayOf(Account("sender@mail.com", "com.google")))
        }

        context = mock {
            on { contentResolver }.doReturn(targetContext.contentResolver)
            on { resources }.doReturn(targetContext.resources)
            on { getSharedPreferences(anyString(), anyInt()) }.doReturn(preferences)
            on { getSystemService(eq(Context.ACCOUNT_SERVICE)) }.doReturn(accountManager)
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

        verify(transport).startSession()
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

        val savedEvent = database.events.first()!!

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

        verify(transport, never()).startSession()
        verify(transport, never()).send(any())
        verify(notifications, never()).showMailError(any(), any())

        val savedEvent = database.events.first()!!

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
        verify(transport, never()).startSession()
        verify(transport, never()).send(any())
        verify(notifications).showMailError(eq(R.string.sender_account_not_found), eq(TARGET_MAIN))

        val savedEvent = database.events.first()!!

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

        verify(transport, never()).startSession()
        verify(transport, never()).send(any())
        verify(notifications).showMailError(eq(R.string.no_recipients_specified), eq(TARGET_RECIPIENTS))

        val savedEvent = database.events.first()!!

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
        verify(transport).startSession()
        verify(transport).send(any())
        verify(notifications, never()).showMailError(any(), any())

        val savedEvent = database.events.first()!!

        assertEquals(STATE_PENDING, savedEvent.state)
        assertEquals(STATUS_ACCEPTED, savedEvent.processStatus)
    }

    /**
     * When settings goes back to normal last error notification should be removed.
     */
    @Test
    fun testClearNotifications() {
        whenever(accountManager.getAccountsByType(eq("com.google"))).thenReturn(arrayOf())

        val event = testingEvent()
        processor.process(event)

        verify(notifications).showMailError(eq(R.string.sender_account_not_found), eq(TARGET_MAIN))
        verify(notifications, never()).cancelAllErrors()

        /* sending next message without errors hides all previous error notifications */
        whenever(accountManager.getAccountsByType(eq("com.google"))).thenReturn(arrayOf(Account("sender@mail.com", "com.google")))
        
        processor.process(testingEvent())

        verify(notifications).showMailError(eq(R.string.sender_account_not_found), eq(TARGET_MAIN))
        verify(notifications).cancelAllErrors()
    }

    /**
     * When [PREF_NOTIFY_SEND_SUCCESS] setting is set to true then success notification should be shown.
     */
    @Test
    fun testSuccessNotification() {
        /* the setting is OFF */
        whenever(preferences.getBoolean(eq(PREF_NOTIFY_SEND_SUCCESS), anyOrNull())).thenReturn(false)

        processor.process(testingEvent())

        verify(notifications, never()).showMessage(anyString(), anyInt())

        /* the setting is ON */
        whenever(preferences.getBoolean(eq(PREF_NOTIFY_SEND_SUCCESS), anyOrNull())).thenReturn(true)

        processor.process(testingEvent())

        verify(notifications).showMessage(eq(R.string.email_successfully_send), eq(TARGET_MAIN))
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

        assertEquals(3, database.events.count())
        assertEquals(3, database.pendingEvents.count())
        verify(notifications, never()).showError(anyString(), anyInt())

        /* try resend with disabled transport */
        processor.processPending()

        assertEquals(3, database.events.count())
        assertEquals(3, database.pendingEvents.count())
        verify(notifications, never()).showError(anyString(), anyInt())

        /* enable transport an try again */
        doNothing().whenever(transport).send(any())

        processor.processPending()

        assertEquals(3, database.events.count())
        assertEquals(0, database.pendingEvents.count())
        verify(notifications, never()).showError(anyString(), anyInt())
    }
}