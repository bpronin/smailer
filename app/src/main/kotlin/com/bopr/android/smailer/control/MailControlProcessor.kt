package com.bopr.android.smailer.control

import android.content.Context
import com.bopr.android.smailer.AccountHelper
import com.bopr.android.smailer.NotificationsHelper
import com.bopr.android.smailer.NotificationsHelper.Companion.NTF_SERVICE_ACCOUNT
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_ACCOUNT
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_FILTER_RECIPIENTS
import com.bopr.android.smailer.processor.mail.GoogleMailSession
import com.bopr.android.smailer.processor.mail.MailMessage
import com.bopr.android.smailer.ui.RemoteControlActivity
import com.bopr.android.smailer.util.commaSplit
import com.bopr.android.smailer.util.containsEmail
import com.bopr.android.smailer.util.extractEmail
import com.google.api.services.gmail.GmailScopes.MAIL_GOOGLE_COM
import org.slf4j.LoggerFactory

/**
 * Checks service mailbox for messages containing control commands and performs it.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal class MailControlProcessor(
    private val context: Context,
    private val settings: Settings = Settings(context),
    private val notifications: NotificationsHelper = NotificationsHelper(context),
) {

    private val parser = MailControlCommandInterpreter()
    private val query = "subject:Re:[${context.getString(R.string.app_name)}] label:inbox"
    private val accountHelper = AccountHelper(context)
    private val commandExecutor = ControlCommandExecutor(context)

    fun checkMailbox(onSuccess: (Int) -> Unit = {}, onError: (Throwable) -> Unit) {
        val accountName = settings.getString(PREF_REMOTE_CONTROL_ACCOUNT)
        val account = accountHelper.getGoogleAccount(accountName)?:run{
            log.warn("Service account [$accountName] not found")

            notifyNoAccount()
            onError(Exception("Service account [$accountName] not found"))
            return
        }

        val session = GoogleMailSession(context, account, MAIL_GOOGLE_COM)
        session.list(
            query,
            onSuccess = { messages ->
                readMessages(messages, session)
                onSuccess(messages.size)
            },
            onError = onError
        )
    }

    private fun readMessages(messages: List<MailMessage>, session: GoogleMailSession): Int {
        if (messages.isEmpty()) {
            log.debug("No service mail")
        } else {
            for (message in messages) {
                if (acceptMessage(message)) {
                    message.body?.let {
                        val command = parser.interpret(it)
                        when {
                            command == null ->
                                log.debug("Not a service mail")

                            command.target != settings.getDeviceName() ->
                                log.debug("Not my mail")

                            else -> {
                                session.markAsRead(message) {
                                    commandExecutor.execute(command)
                                    session.trash(message) {}
                                }
                            }
                        }
                    }
                }
            }
        }

        return messages.size
    }

    private fun acceptMessage(message: MailMessage): Boolean {
        if (settings.getBoolean(PREF_REMOTE_CONTROL_FILTER_RECIPIENTS)) {
            val address = extractEmail(message.from)!!
            val recipients = commaSplit(settings.getEmailRecipients())
            if (!recipients.containsEmail(address)) {
                log.debug("Address $address rejected")

                return false
            }
        }
        return true
    }

    private fun notifyNoAccount() {
        notifications.notifyError(
            NTF_SERVICE_ACCOUNT,
            context.getString(R.string.service_account_not_found),
            RemoteControlActivity::class
        )
    }

    companion object {

        private val log = LoggerFactory.getLogger("MailControlProcessor")
    }
}