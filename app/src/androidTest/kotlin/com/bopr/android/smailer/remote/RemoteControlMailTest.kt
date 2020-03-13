package com.bopr.android.smailer.remote

import android.accounts.AccountsException
import androidx.test.filters.LargeTest
import com.bopr.android.smailer.*
import com.bopr.android.smailer.Database.Companion.TABLE_PHONE_BLACKLIST
import com.bopr.android.smailer.Database.Companion.TABLE_PHONE_WHITELIST
import com.bopr.android.smailer.Database.Companion.TABLE_TEXT_BLACKLIST
import com.bopr.android.smailer.Database.Companion.TABLE_TEXT_WHITELIST
import com.bopr.android.smailer.Notifications.Companion.TARGET_PHONE_BLACKLIST
import com.bopr.android.smailer.Notifications.Companion.TARGET_PHONE_WHITELIST
import com.bopr.android.smailer.Notifications.Companion.TARGET_TEXT_BLACKLIST
import com.bopr.android.smailer.Notifications.Companion.TARGET_TEXT_WHITELIST
import com.bopr.android.smailer.Settings.Companion.PREF_RECIPIENTS_ADDRESS
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_ACCOUNT
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_FILTER_RECIPIENTS
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_NOTIFICATIONS
import com.bopr.android.smailer.util.deviceName
import com.bopr.android.smailer.util.primaryAccount
import com.google.api.services.gmail.GmailScopes.MAIL_GOOGLE_COM
import com.nhaarman.mockitokotlin2.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import java.lang.Thread.sleep

@LargeTest
class RemoteControlMailTest : BaseTest() {

    private val sender = "TEST"
    private val settings = Settings(targetContext, "test.preferences")
    private val account = targetContext.primaryAccount()!!
    private val transport = GoogleMail(targetContext)
    private val notifications: Notifications = mock()
    private lateinit var database: Database
    private lateinit var processor: RemoteControlProcessor

    @Before
    fun setUp() {
        settings.update { clear() }

        database = Database(targetContext, "test.sqlite")
        database.destroy()

        transport.login(account, MAIL_GOOGLE_COM)
        for (message in getMail()) {
            transport.trash(message)
        }

        processor = RemoteControlProcessor(
                context = targetContext,
                database = database,
                settings = settings,
                notifications = notifications
        )
    }

    fun tearDown() {
        database.close()
    }

    private fun getMail(): List<MailMessage> {
        return transport.list("subject:(Re:[SMailer] AND $sender) label:inbox")
    }

    private fun awaitMail(): List<MailMessage> {
        var list = emptyList<MailMessage>()
        for (i in 0..20) {
            list = getMail()
            if (list.isNotEmpty()) break
            sleep(1000)
        }
        return list
    }

    @Test
    fun testHandleServiceMailNoAccount() {
        settings.update {
            putString(PREF_REMOTE_CONTROL_ACCOUNT, null)
        }

        assertThrows(AccountsException::class.java) { processor.checkMailbox() }
    }

    @Test
    fun testHandleServiceMailNoServiceMail() {
        settings.update {
            putString(PREF_REMOTE_CONTROL_ACCOUNT, account.name)
            putBoolean(PREF_REMOTE_CONTROL_FILTER_RECIPIENTS, true)
            putStringList(PREF_RECIPIENTS_ADDRESS, setOf(account.name))
            putBoolean(PREF_REMOTE_CONTROL_NOTIFICATIONS, true)
        }

        assertTrue(getMail().isEmpty())

        processor.checkMailbox()

        assertTrue(getMail().isEmpty())
        assertTrue(database.getFilterList(TABLE_PHONE_BLACKLIST).isEmpty())
        assertTrue(database.getFilterList(TABLE_PHONE_WHITELIST).isEmpty())
        assertTrue(database.getFilterList(TABLE_TEXT_BLACKLIST).isEmpty())
        assertTrue(database.getFilterList(TABLE_TEXT_WHITELIST).isEmpty())
        verify(notifications, never()).showRemoteAction(any(), anyInt())
    }

    @Test
    fun testHandleServiceMailNoMyMail() {
        settings.update {
            putString(PREF_REMOTE_CONTROL_ACCOUNT, account.name)
            putBoolean(PREF_REMOTE_CONTROL_FILTER_RECIPIENTS, true)
            putStringList(PREF_RECIPIENTS_ADDRESS, setOf(account.name))
            putBoolean(PREF_REMOTE_CONTROL_NOTIFICATIONS, true)
        }

        transport.run {
            send(MailMessage(
                    subject = "Re: [SMailer] Incoming SMS from \"$sender\"",
                    body = "To device \"ANOTHER DEVICE\" add phone \"1234567890\" to blacklist",
                    recipients = account.name,
                    from = account.name
            ))
        }

        assertEquals(1, awaitMail().size)

        processor.checkMailbox()

        assertEquals(1, getMail().size)
        assertTrue(database.getFilterList(TABLE_PHONE_BLACKLIST).isEmpty())
        assertTrue(database.getFilterList(TABLE_PHONE_WHITELIST).isEmpty())
        assertTrue(database.getFilterList(TABLE_TEXT_BLACKLIST).isEmpty())
        assertTrue(database.getFilterList(TABLE_TEXT_WHITELIST).isEmpty())
        verify(notifications, never()).showRemoteAction(any(), anyInt())
    }

    @Test
    fun testHandleServiceMailDefault() {
        settings.update {
            putString(PREF_REMOTE_CONTROL_ACCOUNT, account.name)
            putBoolean(PREF_REMOTE_CONTROL_FILTER_RECIPIENTS, true)
            putStringList(PREF_RECIPIENTS_ADDRESS, setOf(account.name))
            putBoolean(PREF_REMOTE_CONTROL_NOTIFICATIONS, true)
        }

        transport.run {
            send(MailMessage(
                    subject = "Re: [SMailer] Incoming SMS from \"$sender\"",
                    body = "To device \"${deviceName()}\" add phone \"1234567890\" to blacklist",
                    recipients = account.name,
                    from = account.name
            ))
            send(MailMessage(
                    subject = "Re: [SMailer] Incoming SMS from \"$sender\"",
                    body = "To device \"${deviceName()}\" add phone \"0987654321\" to whitelist",
                    recipients = account.name,
                    from = account.name
            ))
            send(MailMessage(
                    subject = "Re: [SMailer] Incoming SMS from \"$sender\"",
                    body = "To device \"${deviceName()}\" add text \"SPAM\" to blacklist",
                    recipients = account.name,
                    from = account.name
            ))
            send(MailMessage(
                    subject = "Re: [SMailer] Incoming SMS from \"$sender\"",
                    body = "To device \"${deviceName()}\" add text \"NON SPAM\" to whitelist",
                    recipients = account.name,
                    from = account.name
            ))
        }

        assertEquals(4, awaitMail().size)

        processor.checkMailbox()

        assertTrue(getMail().isEmpty())
        assertTrue(database.getFilterList(TABLE_PHONE_BLACKLIST).contains("1234567890"))
        assertTrue(database.getFilterList(TABLE_PHONE_WHITELIST).contains("0987654321"))
        assertTrue(database.getFilterList(TABLE_TEXT_BLACKLIST).contains("SPAM"))
        assertTrue(database.getFilterList(TABLE_TEXT_WHITELIST).contains("NON SPAM"))
        verify(notifications).showRemoteAction(eq(targetContext.getString(
                R.string.phone_remotely_added_to_blacklist, "1234567890")), eq(TARGET_PHONE_BLACKLIST))
        verify(notifications).showRemoteAction(eq(targetContext.getString(
                R.string.phone_remotely_added_to_whitelist, "0987654321")), eq(TARGET_PHONE_WHITELIST))
        verify(notifications).showRemoteAction(eq(targetContext.getString(
                R.string.text_remotely_added_to_blacklist, "SPAM")), eq(TARGET_TEXT_BLACKLIST))
        verify(notifications).showRemoteAction(eq(targetContext.getString(
                R.string.text_remotely_added_to_whitelist, "NON SPAM")), eq(TARGET_TEXT_WHITELIST))
    }

    @Test
    fun testHandleServiceMailNoRecipientsFilter() {
        settings.update {
            putString(PREF_REMOTE_CONTROL_ACCOUNT, account.name)
            putBoolean(PREF_REMOTE_CONTROL_FILTER_RECIPIENTS, false)
        }

        transport.run {
            send(MailMessage(
                    subject = "Re: [SMailer] Incoming SMS from \"$sender\"",
                    body = "To device \"${deviceName()}\" add phone \"1234567890\" to blacklist",
                    recipients = account.name,
                    from = account.name
            ))
            send(MailMessage(
                    subject = "Re: [SMailer] Incoming SMS from \"$sender\"",
                    body = "To device \"${deviceName()}\" add phone \"0987654321\" to whitelist",
                    recipients = account.name,
                    from = account.name
            ))
            send(MailMessage(
                    subject = "Re: [SMailer] Incoming SMS from \"$sender\"",
                    body = "To device \"${deviceName()}\" add text \"SPAM\" to blacklist",
                    recipients = account.name,
                    from = account.name
            ))
            send(MailMessage(
                    subject = "Re: [SMailer] Incoming SMS from \"$sender\"",
                    body = "To device \"${deviceName()}\" add text \"NON SPAM\" to whitelist",
                    recipients = account.name,
                    from = account.name
            ))
        }

        assertEquals(4, awaitMail().size)

        processor.checkMailbox()

        assertTrue(getMail().isEmpty())
        assertTrue(database.getFilterList(TABLE_PHONE_BLACKLIST).contains("1234567890"))
        assertTrue(database.getFilterList(TABLE_PHONE_WHITELIST).contains("0987654321"))
        assertTrue(database.getFilterList(TABLE_TEXT_BLACKLIST).contains("SPAM"))
        assertTrue(database.getFilterList(TABLE_TEXT_WHITELIST).contains("NON SPAM"))
    }

    @Test
    fun testHandleServiceMailNotificationsOff() {
        settings.update {
            putString(PREF_REMOTE_CONTROL_ACCOUNT, account.name)
            putBoolean(PREF_REMOTE_CONTROL_FILTER_RECIPIENTS, true)
            putStringList(PREF_RECIPIENTS_ADDRESS, setOf(account.name))
            putBoolean(PREF_REMOTE_CONTROL_NOTIFICATIONS, false)
        }

        transport.run {
            send(MailMessage(
                    subject = "Re: [SMailer] Incoming SMS from \"$sender\"",
                    body = "To device \"${deviceName()}\" add phone \"1234567890\" to blacklist",
                    recipients = account.name,
                    from = account.name
            ))
            send(MailMessage(
                    subject = "Re: [SMailer] Incoming SMS from \"$sender\"",
                    body = "To device \"${deviceName()}\" add phone \"0987654321\" to whitelist",
                    recipients = account.name,
                    from = account.name
            ))
            send(MailMessage(
                    subject = "Re: [SMailer] Incoming SMS from \"$sender\"",
                    body = "To device \"${deviceName()}\" add text \"SPAM\" to blacklist",
                    recipients = account.name,
                    from = account.name
            ))
            send(MailMessage(
                    subject = "Re: [SMailer] Incoming SMS from \"$sender\"",
                    body = "To device \"${deviceName()}\" add text \"NON SPAM\" to whitelist",
                    recipients = account.name,
                    from = account.name
            ))
        }

        assertEquals(4, awaitMail().size)

        processor.checkMailbox()

        assertTrue(getMail().isEmpty())
        assertTrue(database.getFilterList(TABLE_PHONE_BLACKLIST).contains("1234567890"))
        assertTrue(database.getFilterList(TABLE_PHONE_WHITELIST).contains("0987654321"))
        assertTrue(database.getFilterList(TABLE_TEXT_BLACKLIST).contains("SPAM"))
        assertTrue(database.getFilterList(TABLE_TEXT_WHITELIST).contains("NON SPAM"))
        verify(notifications, never()).showRemoteAction(any(), anyInt())
    }

}