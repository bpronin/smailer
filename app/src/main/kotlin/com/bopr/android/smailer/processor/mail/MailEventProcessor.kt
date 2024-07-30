package com.bopr.android.smailer.processor.mail

import android.accounts.Account
import android.content.Context
import com.bopr.android.smailer.AccountHelper
import com.bopr.android.smailer.NotificationsHelper
import com.bopr.android.smailer.NotificationsHelper.Companion.NTF_GOOGLE_ACCESS
import com.bopr.android.smailer.NotificationsHelper.Companion.NTF_GOOGLE_ACCOUNT
import com.bopr.android.smailer.NotificationsHelper.Companion.NTF_MAIL
import com.bopr.android.smailer.NotificationsHelper.Companion.NTF_MAIL_RECIPIENTS
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_MESSENGER_ENABLED
import com.bopr.android.smailer.processor.EventProcessor
import com.bopr.android.smailer.provider.Event
import com.bopr.android.smailer.ui.EmailSettingsActivity
import com.bopr.android.smailer.ui.EmailRecipientsActivity
import com.bopr.android.smailer.util.Mockable
import com.bopr.android.smailer.util.isValidEmailAddressList
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.services.gmail.GmailScopes.GMAIL_SEND
import org.slf4j.LoggerFactory

/**
 * Mail transport.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
@Mockable
internal class MailEventProcessor(context: Context) : EventProcessor(context) {

    private val settings = Settings(context)
    private val accountHelper = AccountHelper(context)
    private val formatters = MailFormatterFactory(context)
    private val notifications by lazy { NotificationsHelper(context) }
    private lateinit var account: Account
    private lateinit var session: GoogleMailSession

    override fun isEnabled(): Boolean {
        return settings.getBoolean(PREF_EMAIL_MESSENGER_ENABLED)
    }

    override fun prepare(): Boolean {
        account = checkAccount(accountHelper.getPrimaryGoogleAccount()) ?: run { return false }
        session = GoogleMailSession(context, account, GMAIL_SEND)
        return true
    }

    override fun process(event: Event) {
        val recipients = checkRecipients(settings.getEmailRecipients()) ?: return
        val formatter = formatters.createFormatter(event.payload)

        session.send(
            MailMessage(
                subject = formatter.formatSubject(),
                body = formatter.formatBody(),
                from = account.name,
                recipients = recipients
            ),
            onSuccess = {
                log.debug("Successfully sent")
            },
            onError = { error ->
                log.error("Send failed", error)

                notifySendError(error)
            }
        )
    }

    fun checkRecipients(recipients: String?): String? {
        if (recipients.isNullOrBlank()) {
            log.warn("No recipients")

            notifications.notifyError(
                NTF_MAIL_RECIPIENTS,
                context.getString(R.string.no_recipients_specified),
                EmailRecipientsActivity::class
            )
            return null
        }

        if (!isValidEmailAddressList(recipients)) {
            log.warn("Recipients are invalid")

            notifications.notifyError(
                NTF_MAIL_RECIPIENTS,
                context.getString(R.string.invalid_recipient),
                EmailRecipientsActivity::class
            )
            return null
        }

        return recipients
    }

    fun checkAccount(account: Account?): Account? {
        return account ?: run {
            log.warn("Invalid account")

            notifications.notifyError(
                NTF_GOOGLE_ACCOUNT,
                context.getString(R.string.sender_account_not_found),
                EmailSettingsActivity::class
            )
            null
        }
    }

    private fun notifySendError(error: Throwable) {
        if (error is UserRecoverableAuthIOException) {
            /* this may happen when app has no permission to access google account or
               sender account has been removed from outside of the device */
            notifications.notifyError(
                NTF_GOOGLE_ACCESS,
                context.getString(R.string.no_access_to_google_account),
                EmailSettingsActivity::class
            )
        } else {
            notifications.notifyError(
                NTF_MAIL,
                context.getString(R.string.unable_send_email),
                EmailSettingsActivity::class
            )
        }
    }

    companion object {

        private val log = LoggerFactory.getLogger("MailEventProcessor")
    }
}