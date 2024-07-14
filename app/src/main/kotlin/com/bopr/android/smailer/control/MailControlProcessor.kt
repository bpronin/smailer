package com.bopr.android.smailer.control

import android.accounts.Account
import android.content.Context
import com.bopr.android.smailer.AccountHelper
import com.bopr.android.smailer.NotificationsHelper
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.consumer.mail.MailMessage
import com.bopr.android.smailer.external.GoogleMail
import com.bopr.android.smailer.util.commaSplit
import com.bopr.android.smailer.util.containsEmail
import com.bopr.android.smailer.util.extractEmail
import com.bopr.android.smailer.util.hasInternetConnection
import com.google.android.gms.common.internal.Preconditions.checkNotMainThread
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
    private val executor = ControlCommandExecutor(context)

    fun checkMailbox(): Int {
        checkNotMainThread() /* gmail won't work in main thread */
        if (!checkInternet()) return 0
        val account = requireAccount() ?: return 0

        val session = GoogleMail(context, account, MAIL_GOOGLE_COM)
        val messages = session.list(query)

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

                            command.acceptor != settings.getDeviceName() ->
                                log.debug("Not my mail")

                            else -> {
                                session.markAsRead(message)
                                executor.execute(command)
                                session.trash(message)
                            }
                        }
                    }
                }
            }
        }

        return messages.size
    }

    private fun acceptMessage(message: MailMessage): Boolean {
        if (settings.isRemoteControlRecipientsFilterEnabled()) {
            val address = extractEmail(message.from)!!
            val recipients = commaSplit(settings.getEmailRecipients())
            if (!recipients.containsEmail(address)) {
                log.debug("Address $address rejected")

                return false
            }
        }
        return true
    }

    private fun checkInternet(): Boolean {
        /* check it before all to avoid awaiting timeout while sending */
        return context.hasInternetConnection().also {
            if (!it) log.warn("No internet connection")
        }
    }

    private fun requireAccount(): Account? {
        val accountName = settings.getRemoteControlAccountName()
        val googleAccount = accountHelper.getGoogleAccount(accountName)
        return googleAccount.also {
            if (it == null) {
                notifications.showRemoteAccountError()
                log.warn("Service account [$accountName] not found")
            }
        }
    }

    companion object {

        private val log = LoggerFactory.getLogger("MailControlProcessor")
    }
}