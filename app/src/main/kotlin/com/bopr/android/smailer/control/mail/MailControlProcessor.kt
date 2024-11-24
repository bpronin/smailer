package com.bopr.android.smailer.control.mail

import android.content.Context
import com.bopr.android.smailer.AccountsHelper.Companion.accounts
import com.bopr.android.smailer.NotificationsHelper.Companion.NTF_SERVICE_ACCOUNT
import com.bopr.android.smailer.NotificationsHelper.Companion.notifications
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_ACCOUNT
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_FILTER_RECIPIENTS
import com.bopr.android.smailer.Settings.Companion.settings
import com.bopr.android.smailer.control.ControlCommandExecutor
import com.bopr.android.smailer.messenger.mail.GoogleMailSession
import com.bopr.android.smailer.messenger.mail.MailMessage
import com.bopr.android.smailer.ui.RemoteControlActivity
import com.bopr.android.smailer.util.Logger
import com.bopr.android.smailer.util.commaSplit
import com.bopr.android.smailer.util.containsEmail
import com.bopr.android.smailer.util.extractEmail
import com.google.api.services.gmail.GmailScopes.MAIL_GOOGLE_COM

/**
 * Checks service mailbox for messages containing control commands and performs it.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal class MailControlProcessor(private val context: Context) {

    private val settings = context.settings
    private val notifications = context.notifications
    private val interpreter = MailControlCommandInterpreter()
    private val query = "subject:Re:[${context.getString(R.string.app_name)}] label:inbox"
    private val commandExecutor = ControlCommandExecutor(context)

    fun checkMailbox(onSuccess: (Int) -> Unit = {}, onError: (Throwable) -> Unit = {}) {
        val accountName = settings.getString(PREF_REMOTE_CONTROL_ACCOUNT)
        val account = context.accounts.getGoogleAccount(accountName) ?: run {
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
                        val command = interpreter.interpret(it)
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
            val recipients = settings.getMailRecipients().commaSplit()
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

        private val log = Logger("MailControl")
    }
}