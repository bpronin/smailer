@file:Suppress("DEPRECATION")

package com.bopr.android.smailer.messenger.mail

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
import com.bopr.android.smailer.NotificationsHelper.Companion.NTF_GOOGLE_ACCESS
import com.bopr.android.smailer.NotificationsHelper.Companion.NTF_GOOGLE_ACCOUNT
import com.bopr.android.smailer.NotificationsHelper.Companion.NTF_MAIL_RECIPIENTS
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.PREF_MAIL_MESSAGE_CONTENT
import com.bopr.android.smailer.Settings.Companion.PREF_MAIL_MESSENGER_RECIPIENTS
import com.bopr.android.smailer.Settings.Companion.PREF_MAIL_SENDER_ACCOUNT
import com.bopr.android.smailer.Settings.Companion.PREF_MAIL_TRIGGERS
import com.bopr.android.smailer.Settings.Companion.PREF_MESSAGE_LOCALE
import com.bopr.android.smailer.Settings.Companion.PREF_NOTIFY_SEND_SUCCESS
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_MESSAGE_CONTENT_BODY
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_MESSAGE_CONTENT_CALLER
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_MESSAGE_CONTENT_CONTROL_LINKS
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_MESSAGE_CONTENT_DEVICE_NAME
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_MESSAGE_CONTENT_DISPATCH_TIME
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_MESSAGE_CONTENT_CREATION_TIME
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_MESSAGE_CONTENT_HEADER
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_MESSAGE_CONTENT_LOCATION
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_TRIGGER_IN_SMS
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_TRIGGER_MISSED_CALLS
import com.bopr.android.smailer.data.Database
import com.bopr.android.smailer.messenger.MessageDispatcher
import com.bopr.android.smailer.messenger.ProcessState.Companion.STATE_IGNORED
import com.bopr.android.smailer.messenger.ProcessState.Companion.STATE_PENDING
import com.bopr.android.smailer.messenger.ProcessState.Companion.STATE_PROCESSED
import com.bopr.android.smailer.provider.telephony.PhoneCallInfo
import com.bopr.android.smailer.provider.telephony.PhoneCallProcessor
import com.bopr.android.smailer.ui.MailRecipientsActivity
import com.bopr.android.smailer.ui.MailSettingsActivity
import com.bopr.android.smailer.ui.MainActivity
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
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.*
import java.io.IOException

/**
 * [PhoneCallProcessor] tester.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
@SmallTest
class PhoneCallProcessorTest : BaseTest() {

    @get:Rule
    var permissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(Manifest.permission.READ_CONTACTS)

    private lateinit var database: Database
    private lateinit var context: Context
    private lateinit var messenger: MessageDispatcher
    private lateinit var notifications: NotificationsHelper
    private lateinit var preferences: SharedPreferences
    private lateinit var processor: PhoneCallProcessor
    private lateinit var accountManager: AccountManager

    private fun testingCall(): PhoneCallInfo {
        val time = System.currentTimeMillis()
        return PhoneCallInfo(
            phone = "+123",
            isIncoming = true,
            startTime = time,
            endTime = time + 1000,
            isMissed = false,
            acceptor = "device"
        )
    }

    @Suppress("DEPRECATION")
    @Before
    fun setUp() {
        preferences = mock {
            on {
                getString(
                    eq(PREF_MAIL_SENDER_ACCOUNT),
                    anyOrNull()
                )
            }.doReturn("sender@mail.com")
            on {
                getString(
                    eq(PREF_MAIL_MESSENGER_RECIPIENTS),
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
                    eq(PREF_MAIL_TRIGGERS),
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
                    eq(PREF_MAIL_MESSAGE_CONTENT),
                    anyOrNull()
                )
            }.doReturn(
                setOf(
                    VAL_PREF_MESSAGE_CONTENT_BODY,
                    VAL_PREF_MESSAGE_CONTENT_CALLER,
                    VAL_PREF_MESSAGE_CONTENT_DEVICE_NAME,
                    VAL_PREF_MESSAGE_CONTENT_HEADER,
                    VAL_PREF_MESSAGE_CONTENT_LOCATION,
                    VAL_PREF_MESSAGE_CONTENT_CREATION_TIME,
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
                    anyString(),
                    anyInt()
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

        processor = PhoneCallProcessor(context, database, notifications)
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
        val call = testingCall()

        processor.add(call)
        processor.process()

//        verify(messenger).sendMessagesFor(argThat {
//            id == null
//                    && subject == "[SMailer] Incoming SMS from \"+123\""
//                    && !body.isNullOrBlank()
//                    && attachment == null
//                    && recipients == "recipient@mail.com"
//                    && replyTo == null
//                    && from == "sender@mail.com"
//        })
        verifyNotifyAccountError()
        verifyNotifyRecipientsError()
        verifyNotifyGoogleAccessError()

        val savedRecord = database.phoneCalls.first()

        assertEquals(call.acceptor, savedRecord.acceptor)
        assertEquals(call.startTime, savedRecord.startTime)
        assertEquals(call.phone, savedRecord.phone)
        assertEquals(STATE_PROCESSED, savedRecord.processState)
        assertEquals(PhoneCallInfo.FLAG_BYPASS_NONE, savedRecord.bypassFlags)
        assertEquals(GeoLocation(60.0, 30.0), call.location)
    }

    /**
     * Tests successful processing - call ignored.
     */
    @Test
    fun testProcessIgnored() {
        /* make the call not an SMS then default filter will deny it */
        val call = testingCall()

        processor.add(call)
        processor.process()

        verify(messenger, never()).dispatch(any(), any(), any())
        verifyNotifyAccountError()
        verifyNotifyRecipientsError()
        verifyNotifyGoogleAccessError()

        val savedCall = database.phoneCalls.first()

        assertEquals(STATE_IGNORED, savedCall.processState)
        assertEquals(PhoneCallInfo.FLAG_BYPASS_TRIGGER_OFF, savedCall.bypassFlags)
    }

    /**
     * Test processing when sender account setting is unspecified.
     */
    @Test
    fun testProcessNoSender() {
        whenever(preferences.getString(eq(PREF_MAIL_SENDER_ACCOUNT), anyOrNull())).thenReturn(null)

        val call = testingCall()
        processor.add(call)
        processor.process()

//        verify(messenger, never()).login(any(), any())
        verify(messenger, never()).dispatch(any(), any(), any())
        verify(notifications).notifyError(
            eq(NTF_GOOGLE_ACCOUNT),
            eq(getString(R.string.sender_account_not_found)),
            eq(MailSettingsActivity::class)
        )

        val savedCall = database.phoneCalls.first()

        assertEquals(STATE_PENDING, savedCall.processState)
        assertEquals(PhoneCallInfo.FLAG_BYPASS_NONE, savedCall.bypassFlags)
    }

    /**
     * Test processing when recipients setting is unspecified.
     */
    @Test
    fun testProcessNoRecipients() {
        whenever(
            preferences.getString(
                eq(PREF_MAIL_MESSENGER_RECIPIENTS),
                anyOrNull()
            )
        ).thenReturn(null)

        val call = testingCall()
        processor.add(call)
        processor.process()

        verify(messenger, never()).dispatch(any(), any(), any())
        verify(notifications).notifyError(
            NTF_MAIL_RECIPIENTS,
            eq(getString(R.string.no_recipients_specified)),
            MailRecipientsActivity::class
        )

        val savedCall = database.phoneCalls.first()

        assertEquals(STATE_PENDING, savedCall.processState)
        assertEquals(PhoneCallInfo.FLAG_BYPASS_NONE, savedCall.bypassFlags)
    }

    /**
     * Transport exception during send does not produce any notifications.
     */
    @Test
    fun testProcessTransportSendFailed() {
        doThrow(IOException("Test error")).whenever(messenger).dispatch(any(), any(), any())

        val call = testingCall()
        processor.add(call)
        processor.process()

//        verify(messenger).login(any(), any())
        verify(messenger).dispatch(any(), any(), any())
        verifyNotifyAccountError()
        verifyNotifyRecipientsError()
        verifyNotifyGoogleAccessError()

        val savedCall = database.phoneCalls.first()

        assertEquals(STATE_PENDING, savedCall.processState)
        assertEquals(PhoneCallInfo.FLAG_BYPASS_NONE, savedCall.bypassFlags)
    }

    /**
     * When [PREF_NOTIFY_SEND_SUCCESS] setting is set to true then success notification should be shown.
     */
    @Test
    fun testSuccessNotification() {
        /* the setting is OFF */
        whenever(
            preferences.getBoolean(
                eq(PREF_NOTIFY_SEND_SUCCESS),
                anyOrNull()
            )
        ).thenReturn(false)

        processor.add(testingCall())
        processor.process()

        verifyNotifyMailSuccess()

        /* the setting is ON */
        whenever(
            preferences.getBoolean(
                eq(PREF_NOTIFY_SEND_SUCCESS),
                anyOrNull()
            )
        ).thenReturn(true)

        processor.add(testingCall())
        processor.process()

        verifyNotifyMailSuccess()
    }

    /**
     * Test resending pending messages.
     */
    @Test
    fun testProcessRecords() {
        /* disable transport */
        doThrow(IOException("Test error")).whenever(messenger).dispatch(any(), any(), any())

        processor.add(testingCall())
        processor.add(testingCall())
        processor.add(testingCall())
        processor.process()

        assertEquals(3, database.phoneCalls.size)
        assertEquals(3, database.phoneCalls.filterPending.size)
        verifyNotifyAccountError()
        verifyNotifyRecipientsError()
        verifyNotifyGoogleAccessError()

        /* try resend with disabled transport */
        processor.process()

        assertEquals(3, database.phoneCalls.size)
        assertEquals(3, database.phoneCalls.filterPending.size)
        verifyNotifyAccountError()
        verifyNotifyRecipientsError()
        verifyNotifyGoogleAccessError()

        /* enable transport an try again */
        doNothing().whenever(messenger).dispatch(any(), any(), any())

        processor.process()

        assertEquals(3, database.phoneCalls.size)
        assertEquals(0, database.phoneCalls.filterPending.size)
        verifyNotifyAccountError()
        verifyNotifyRecipientsError()
        verifyNotifyGoogleAccessError()
    }

    private fun verifyNotifyMailSuccess() {
        verify(notifications, never()).notifyInfo(
            eq(getString(R.string.email_successfully_send)),
            eq(null),
            eq(MainActivity::class)
        )
    }

    private fun verifyNotifyAccountError() {
        verify(notifications, never()).notifyError(
            eq(NTF_GOOGLE_ACCOUNT),
            eq(getString(R.string.sender_account_not_found)),
            eq(MailSettingsActivity::class)
        )
    }

    private fun verifyNotifyGoogleAccessError() {
        verify(notifications, never()).notifyError(
            eq(NTF_GOOGLE_ACCESS),
            eq(getString(R.string.no_access_to_google_account)),
            eq(MailSettingsActivity::class)
        )
    }

    private fun verifyNotifyRecipientsError() {
        verify(notifications, never()).notifyError(
            eq(NTF_GOOGLE_ACCESS),
            anyString(),
            eq(MailRecipientsActivity::class)
        )
    }
}