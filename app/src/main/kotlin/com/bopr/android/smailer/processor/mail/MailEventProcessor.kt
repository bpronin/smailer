package com.bopr.android.smailer.processor.mail

import android.accounts.Account
import android.content.Context
import com.bopr.android.smailer.AccountHelper
import com.bopr.android.smailer.NotificationsHelper
import com.bopr.android.smailer.NotificationsHelper.Companion.GOOGLE_ACCESS_ERROR
import com.bopr.android.smailer.NotificationsHelper.Companion.GOOGLE_ACCOUNT_ERROR
import com.bopr.android.smailer.NotificationsHelper.Companion.GOOGLE_MAIL_ERROR
import com.bopr.android.smailer.NotificationsHelper.Companion.RECIPIENTS_ERROR
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_MESSENGER_ENABLED
import com.bopr.android.smailer.processor.EventProcessor
import com.bopr.android.smailer.provider.Event
import com.bopr.android.smailer.ui.EmailSettingsActivity
import com.bopr.android.smailer.ui.RecipientsActivity
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

    override fun isEnabled(): Boolean {
        return settings.getBoolean(PREF_EMAIL_MESSENGER_ENABLED)
    }

    override fun process(event: Event) {
        val recipients = checkRecipients(settings.getEmailRecipients()) ?: return
        val account = checkAccount(accountHelper.getPrimaryGoogleAccount()) ?: return
        val formatter = formatters.createFormatter(event.payload)

        GoogleMailSession(context, account, GMAIL_SEND).send(
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
                RECIPIENTS_ERROR,
                context.getString(R.string.no_recipients_specified),
                RecipientsActivity::class
            )
            return null
        }

        if (!isValidEmailAddressList(recipients)) {
            log.warn("Recipients are invalid")

            notifications.notifyError(
                RECIPIENTS_ERROR,
                context.getString(R.string.invalid_recipient),
                RecipientsActivity::class
            )
            return null
        }

        return recipients
    }

    fun checkAccount(account: Account?): Account? {
        return account ?: run {
            log.warn("Invalid account")

            notifications.notifyError(
                GOOGLE_ACCOUNT_ERROR,
                context.getString(R.string.sender_account_not_found),
                EmailSettingsActivity::class
            )
            null
        }
    }

    private fun notifySendError(error: Throwable) {
        var messageRes = R.string.unable_send_email
        var errorCode = GOOGLE_MAIL_ERROR

        if (error is UserRecoverableAuthIOException) {
            /* this happens when app has no permission to access google account or
               sender account has been removed from outside of the device */
            errorCode = GOOGLE_ACCESS_ERROR
            messageRes = R.string.no_access_to_google_account
        }

        notifications.notifyError(
            errorCode,
            context.getString(messageRes),
            EmailSettingsActivity::class
        )
    }

    companion object {

        private val log = LoggerFactory.getLogger("MailTransport")
    }
}