package com.bopr.android.smailer.messenger.mail

import android.accounts.Account
import android.content.Context
import com.bopr.android.smailer.AccountHelper
import com.bopr.android.smailer.NotificationsHelper
import com.bopr.android.smailer.NotificationsHelper.Companion.NTF_GOOGLE_ACCESS
import com.bopr.android.smailer.NotificationsHelper.Companion.NTF_GOOGLE_ACCOUNT
import com.bopr.android.smailer.NotificationsHelper.Companion.NTF_MAIL
import com.bopr.android.smailer.NotificationsHelper.Companion.NTF_MAIL_RECIPIENTS
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings.Companion.PREF_MAIL_MESSENGER_ENABLED
import com.bopr.android.smailer.Settings.Companion.PREF_NOTIFY_SEND_SUCCESS
import com.bopr.android.smailer.Settings.Companion.settings
import com.bopr.android.smailer.messenger.Event
import com.bopr.android.smailer.messenger.Event.Companion.FLAG_SENT_BY_MAIL
import com.bopr.android.smailer.messenger.Messenger
import com.bopr.android.smailer.ui.MailRecipientsActivity
import com.bopr.android.smailer.ui.MailSettingsActivity
import com.bopr.android.smailer.ui.MainActivity
import com.bopr.android.smailer.util.Logger
import com.bopr.android.smailer.util.Mockable
import com.bopr.android.smailer.util.isValidEmailAddressList
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.services.gmail.GmailScopes.GMAIL_SEND

/**
 * Mail transport.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
@Mockable
internal class MailMessenger(private val context: Context) : Messenger {

    private val accountHelper = AccountHelper(context)
    private val formatters = MailFormatterFactory(context)
    private val notifications by lazy { NotificationsHelper(context) }
    private var account: Account? = null
    private var session: GoogleMailSession? = null

    override fun requireContext() = context

    override fun initialize(): Boolean {
        if (settings.getBoolean(PREF_MAIL_MESSENGER_ENABLED)) {
            account = checkAccount(accountHelper.getPrimaryGoogleAccount())?.also {
                session = GoogleMailSession(context, it, GMAIL_SEND)

                log.debug("Initialized")

                return true
            }
        }
        return false
    }

    override fun sendMessage(
        event: Event,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        if (FLAG_SENT_BY_MAIL in event.processFlags) return

        session?.run {
            log.debug("Sending").verb(event)

            val recipients = checkRecipients(settings.getMailRecipients()) ?: return
            val formatter = formatters.createFormatter(event)

            send(
                MailMessage(
                    subject = formatter.formatSubject(),
                    body = formatter.formatBody(),
                    from = account?.name,
                    recipients = recipients
                ),
                onSuccess = {
                    log.debug("Successfully sent")

                    event.processFlags += FLAG_SENT_BY_MAIL
                    notifySendSuccess()
                    onSuccess()
                },
                onError = { error ->
                    log.error("Send failed", error)

                    event.processFlags -= FLAG_SENT_BY_MAIL
                    notifySendError(error)
                    onError(error)
                })
        }
    }

    fun checkRecipients(recipients: String?): String? {
        if (recipients.isNullOrBlank()) {
            log.warn("No recipients")

            notifications.notifyError(
                NTF_MAIL_RECIPIENTS,
                context.getString(R.string.no_recipients_specified),
                MailRecipientsActivity::class
            )
            return null
        }

        if (!isValidEmailAddressList(recipients)) {
            log.warn("Recipients are invalid")

            notifications.notifyError(
                NTF_MAIL_RECIPIENTS,
                context.getString(R.string.invalid_recipient),
                MailRecipientsActivity::class
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
                MailSettingsActivity::class
            )
            null
        }
    }

    private fun notifySendSuccess() {
        if (settings.getBoolean(PREF_NOTIFY_SEND_SUCCESS))
            notifications.notifyInfo(
                title = context.getString(R.string.email_successfully_send),
                target = MainActivity::class
            )
    }

    private fun notifySendError(error: Throwable) {
        if (error is UserRecoverableAuthIOException) {
            /* this may happen when app has no permission to access google account or
               sender account has been removed from outside of the device */
            notifications.notifyError(
                NTF_GOOGLE_ACCESS,
                context.getString(R.string.no_access_to_google_account),
                MailSettingsActivity::class
            )
        } else {
            notifications.notifyError(
                NTF_MAIL,
                context.getString(R.string.unable_send_email),
                MailSettingsActivity::class
            )
        }
    }

    companion object {

        private val log = Logger("MailMessenger")
    }
}