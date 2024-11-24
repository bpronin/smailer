package com.bopr.android.smailer.control.mail

import android.accounts.Account
import androidx.test.filters.LargeTest
import com.bopr.android.smailer.BaseTest
import com.bopr.android.smailer.NotificationsHelper
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.data.Database
import com.bopr.android.smailer.messenger.mail.GoogleMailSession
import com.nhaarman.mockitokotlin2.mock

@LargeTest
class RemoteControlMailTest : BaseTest() {

    private val sender = "TEST"
    private val notifications: NotificationsHelper = mock()
    private lateinit var database: Database
    private lateinit var processor: MailControlProcessor
    private lateinit var account: Account
    private lateinit var transport: GoogleMailSession
    private lateinit var settings: Settings

//    @Before
//    fun setUp() {
//        sharedPreferencesName = "test.preferences"
//        databaseName = "test.sqlite"
//
//        targetContext.deleteDatabase(databaseName)
//        database = Database(targetContext)
//
//        settings = Settings(targetContext)
//        account = AccountHelper(targetContext).requirePrimaryGoogleAccount()
//        transport = GoogleMailSession(targetContext, account)
//
//        for (message in loadMail()) {
//            transport.trash(message){}
//        }
//
//        processor = MailControlProcessor(
//            context = targetContext,
//            settings = settings,
//            notifications = notifications
//        )
//    }
//
//    fun tearDown() {
//        database.close()
//    }
//
//    private fun loadMail(): List<MailMessage> {
//        return transport.list("subject:(Re:[SMailer] AND $sender) label:inbox")
//    }
//
//    private fun awaitMail(): List<MailMessage> {
//        var list = emptyList<MailMessage>()
//        for (i in 0..20) {
//            list = loadMail()
//            if (list.isNotEmpty()) break
//            sleep(1000)
//        }
//        return list
//    }
//
//    @Test
//    fun testHandleServiceMailNoAccount() {
//        settings.update {
//            putString(PREF_REMOTE_CONTROL_ACCOUNT, null)
//        }
//
//        processor.checkMailbox()
//
//        verify(notifications).showRemoteAccountError()
//    }
//
//    @Test
//    fun testHandleServiceMailNoMail() {
//        settings.update {
//            putString(PREF_REMOTE_CONTROL_ACCOUNT, account.name)
//            putBoolean(PREF_REMOTE_CONTROL_FILTER_RECIPIENTS, true)
//            putStringList(PREF_RECIPIENTS_ADDRESS, setOf(account.name))
//            putBoolean(PREF_REMOTE_CONTROL_NOTIFICATIONS, true)
//        }
//
//        assertTrue(loadMail().isEmpty())
//
//        processor.checkMailbox()
//
//        assertTrue(loadMail().isEmpty())
//        assertTrue(database.phoneBlacklist.isEmpty())
//        assertTrue(database.phoneWhitelist.isEmpty())
//        assertTrue(database.smsTextBlacklist.isEmpty())
//        assertTrue(database.smsTextWhitelist.isEmpty())
//        verify(notifications, never()).showRemoteAction(any(), any())
//    }
//
//    @Test
//    fun testHandleServiceMailNoMyMail() {
//        settings.update {
//            putString(PREF_REMOTE_CONTROL_ACCOUNT, account.name)
//            putBoolean(PREF_REMOTE_CONTROL_FILTER_RECIPIENTS, true)
//            putStringList(PREF_RECIPIENTS_ADDRESS, setOf(account.name))
//            putBoolean(PREF_REMOTE_CONTROL_NOTIFICATIONS, true)
//        }
//
//        transport.run {
//            send(
//                MailMessage(
//                    subject = "Re: [SMailer] Incoming SMS from \"$sender\"",
//                    body = "To device \"ANOTHER DEVICE\" add phone \"1234567890\" to blacklist",
//                    recipients = account.name,
//                    from = account.name
//                )
//            )
//        }
//
//        assertEquals(1, awaitMail().size)
//
//        processor.checkMailbox()
//
//        assertEquals(1, loadMail().size)
//        assertTrue(database.phoneBlacklist.isEmpty())
//        assertTrue(database.phoneWhitelist.isEmpty())
//        assertTrue(database.smsTextBlacklist.isEmpty())
//        assertTrue(database.smsTextWhitelist.isEmpty())
//        verify(notifications, never()).showRemoteAction(any(), any())
//    }
//
//    @Test
//    fun testHandleServiceMailDefault() {
//        settings.update {
//            putString(PREF_REMOTE_CONTROL_ACCOUNT, account.name)
//            putBoolean(PREF_REMOTE_CONTROL_FILTER_RECIPIENTS, true)
//            putStringList(PREF_RECIPIENTS_ADDRESS, setOf(account.name))
//            putBoolean(PREF_REMOTE_CONTROL_NOTIFICATIONS, true)
//        }
//
//        transport.run {
//            send(
//                MailMessage(
//                    subject = "Re: [SMailer] Incoming SMS from \"$sender\"",
//                    body = "To device \"${DEVICE_NAME}\" add phone \"1234567890\" to blacklist",
//                    recipients = account.name,
//                    from = account.name
//                )
//            )
//            send(
//                MailMessage(
//                    subject = "Re: [SMailer] Incoming SMS from \"$sender\"",
//                    body = "To device \"${DEVICE_NAME}\" add phone \"0987654321\" to whitelist",
//                    recipients = account.name,
//                    from = account.name
//                )
//            )
//            send(
//                MailMessage(
//                    subject = "Re: [SMailer] Incoming SMS from \"$sender\"",
//                    body = "To device \"${DEVICE_NAME}\" add text \"SPAM\" to blacklist",
//                    recipients = account.name,
//                    from = account.name
//                )
//            )
//            send(
//                MailMessage(
//                    subject = "Re: [SMailer] Incoming SMS from \"$sender\"",
//                    body = "To device \"${DEVICE_NAME}\" add text \"NON SPAM\" to whitelist",
//                    recipients = account.name,
//                    from = account.name
//                )
//            )
//        }
//
//        assertEquals(4, awaitMail().size)
//
//        processor.checkMailbox()
//
//        assertTrue(loadMail().isEmpty())
//        assertTrue(database.phoneBlacklist.contains("1234567890"))
//        assertTrue(database.phoneWhitelist.contains("0987654321"))
//        assertTrue(database.smsTextBlacklist.contains("SPAM"))
//        assertTrue(database.smsTextWhitelist.contains("NON SPAM"))
//        verify(notifications).showRemoteAction(
//            eq(
//                targetContext.getString(
//                    R.string.phone_remotely_added_to_blacklist, "1234567890"
//                )
//            ), eq(EventFilterPhoneBlacklistActivity::class)
//        )
//        verify(notifications).showRemoteAction(
//            eq(
//                targetContext.getString(
//                    R.string.phone_remotely_added_to_whitelist, "0987654321"
//                )
//            ), eq(EventFilterPhoneWhitelistActivity::class)
//        )
//        verify(notifications).showRemoteAction(
//            eq(
//                targetContext.getString(
//                    R.string.text_remotely_added_to_blacklist, "SPAM"
//                )
//            ), eq(EventFilterTextBlacklistActivity::class)
//        )
//        verify(notifications).showRemoteAction(
//            eq(
//                targetContext.getString(
//                    R.string.text_remotely_added_to_whitelist, "NON SPAM"
//                )
//            ), eq(EventFilterTextWhitelistActivity::class)
//        )
//    }
//
//    @Test
//    fun testHandleServiceMailNoRecipientsFilter() {
//        settings.update {
//            putString(PREF_REMOTE_CONTROL_ACCOUNT, account.name)
//            putBoolean(PREF_REMOTE_CONTROL_FILTER_RECIPIENTS, false)
//        }
//
//        transport.run {
//            send(
//                MailMessage(
//                    subject = "Re: [SMailer] Incoming SMS from \"$sender\"",
//                    body = "To device \"${DEVICE_NAME}\" add phone \"1234567890\" to blacklist",
//                    recipients = account.name,
//                    from = account.name
//                )
//            )
//            send(
//                MailMessage(
//                    subject = "Re: [SMailer] Incoming SMS from \"$sender\"",
//                    body = "To device \"${DEVICE_NAME}\" add phone \"0987654321\" to whitelist",
//                    recipients = account.name,
//                    from = account.name
//                )
//            )
//            send(
//                MailMessage(
//                    subject = "Re: [SMailer] Incoming SMS from \"$sender\"",
//                    body = "To device \"${DEVICE_NAME}\" add text \"SPAM\" to blacklist",
//                    recipients = account.name,
//                    from = account.name
//                )
//            )
//            send(
//                MailMessage(
//                    subject = "Re: [SMailer] Incoming SMS from \"$sender\"",
//                    body = "To device \"${DEVICE_NAME}\" add text \"NON SPAM\" to whitelist",
//                    recipients = account.name,
//                    from = account.name
//                )
//            )
//        }
//
//        assertEquals(4, awaitMail().size)
//
//        processor.checkMailbox()
//
//        assertTrue(loadMail().isEmpty())
//        assertTrue(database.phoneBlacklist.contains("1234567890"))
//        assertTrue(database.phoneWhitelist.contains("0987654321"))
//        assertTrue(database.smsTextBlacklist.contains("SPAM"))
//        assertTrue(database.smsTextWhitelist.contains("NON SPAM"))
//    }
//
//    @Test
//    fun testHandleServiceMailNotificationsOff() {
//        settings.update {
//            putString(PREF_REMOTE_CONTROL_ACCOUNT, account.name)
//            putBoolean(PREF_REMOTE_CONTROL_FILTER_RECIPIENTS, true)
//            putStringList(PREF_RECIPIENTS_ADDRESS, setOf(account.name))
//            putBoolean(PREF_REMOTE_CONTROL_NOTIFICATIONS, false)
//        }
//
//        transport.run {
//            send(
//                MailMessage(
//                    subject = "Re: [SMailer] Incoming SMS from \"$sender\"",
//                    body = "To device \"${DEVICE_NAME}\" add phone \"1234567890\" to blacklist",
//                    recipients = account.name,
//                    from = account.name
//                )
//            )
//            send(
//                MailMessage(
//                    subject = "Re: [SMailer] Incoming SMS from \"$sender\"",
//                    body = "To device \"${DEVICE_NAME}\" add phone \"0987654321\" to whitelist",
//                    recipients = account.name,
//                    from = account.name
//                )
//            )
//            send(
//                MailMessage(
//                    subject = "Re: [SMailer] Incoming SMS from \"$sender\"",
//                    body = "To device \"${DEVICE_NAME}\" add text \"SPAM\" to blacklist",
//                    recipients = account.name,
//                    from = account.name
//                )
//            )
//            send(
//                MailMessage(
//                    subject = "Re: [SMailer] Incoming SMS from \"$sender\"",
//                    body = "To device \"${DEVICE_NAME}\" add text \"NON SPAM\" to whitelist",
//                    recipients = account.name,
//                    from = account.name
//                )
//            )
//        }
//
//        assertEquals(4, awaitMail().size)
//
//        processor.checkMailbox()
//
//        assertTrue(loadMail().isEmpty())
//        assertTrue(database.phoneBlacklist.contains("1234567890"))
//        assertTrue(database.phoneWhitelist.contains("0987654321"))
//        assertTrue(database.smsTextBlacklist.contains("SPAM"))
//        assertTrue(database.smsTextWhitelist.contains("NON SPAM"))
//        verify(notifications, never()).showRemoteAction(any(), any())
//    }

}