package com.bopr.android.smailer.messenger.mail

import android.accounts.Account
import android.content.Context
import com.bopr.android.smailer.AccountsHelper.Companion.accounts
import com.bopr.android.smailer.NotificationData
import com.bopr.android.smailer.NotificationsHelper.Companion.NTF_GOOGLE_ACCESS
import com.bopr.android.smailer.NotificationsHelper.Companion.NTF_GOOGLE_ACCOUNT
import com.bopr.android.smailer.NotificationsHelper.Companion.NTF_MAIL
import com.bopr.android.smailer.NotificationsHelper.Companion.NTF_MAIL_RECIPIENTS
import com.bopr.android.smailer.NotificationsHelper.Companion.notifications
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings.Companion.PREF_MAIL_MESSENGER_ENABLED
import com.bopr.android.smailer.Settings.Companion.settings
import com.bopr.android.smailer.messenger.Event
import com.bopr.android.smailer.messenger.Event.Companion.SENT_BY_MAIL
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
 * Email messenger.
 *
 * @author Boris Pronin ([boris280471@gmail.com](mailto:boris280471@gmail.com))
 */
@Mockable
internal class MailMessenger(private val context: Context) : Messenger(context, SENT_BY_MAIL) {

    private val formatters = MailFormatterFactory(context)
    private var account: Account? = null
    private var session: GoogleMailSession? = null
    override val isInitialized get() = session != null

    override suspend fun doInitialize() {
        if (context.settings.getBoolean(PREF_MAIL_MESSENGER_ENABLED)) {
            account = checkAccount(context.accounts.getPrimaryGoogleAccount())?.also {
                session = GoogleMailSession(context, it, GMAIL_SEND)
                log.debug("Email session created")
            }
        }
    }

    override suspend fun doSend(event: Event) {
        session?.run {
            val recipients = checkRecipients(context.settings.getMailRecipients()) ?: return
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
                },
                onError = { error ->
                    log.error("Send failed", error)
                })
        }
    }

    fun checkRecipients(recipients: String?): String? {
        if (recipients.isNullOrBlank()) {
            log.warn("No recipients")

            context.notifications.notifyError(
                NotificationData(
                    id = NTF_MAIL_RECIPIENTS,
                    text = context.getString(R.string.no_recipients_specified),
                    target = MailRecipientsActivity::class
                )
            )
            return null
        }

        if (!isValidEmailAddressList(recipients)) {
            log.warn("Recipients are invalid")

            context.notifications.notifyError(
                NotificationData(
                    id = NTF_MAIL_RECIPIENTS,
                    text = context.getString(R.string.invalid_recipient),
                    target = MailRecipientsActivity::class
                )
            )
            return null
        }

        return recipients
    }

    fun checkAccount(account: Account?): Account? {
        return account ?: run {
            log.warn("Invalid account")

            context.notifications.notifyError(
                NotificationData(
                    id = NTF_GOOGLE_ACCOUNT,
                    text = context.getString(R.string.sender_account_not_found),
                    target = MailSettingsActivity::class
                )
            )
            null
        }
    }

    override fun getSuccessNotification() = NotificationData(
        title = context.getString(R.string.email_successfully_send),
        target = MainActivity::class
    )

    override fun getErrorNotification(error: Throwable) =
        if (error is UserRecoverableAuthIOException) {
            /* this may happen when app has no permission to access google account or
               sender account has been removed from other place */
            NotificationData(
                id = NTF_GOOGLE_ACCESS,
                text = context.getString(R.string.no_access_to_google_account),
                target = MailSettingsActivity::class
            )

        } else {
            NotificationData(
                id = NTF_MAIL,
                text = context.getString(R.string.unable_send_email),
                target = MailSettingsActivity::class
            )
        }

    companion object {
        private val log = Logger("MailMessenger")
    }
}