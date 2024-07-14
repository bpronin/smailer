package com.bopr.android.smailer.consumer.mail

import android.content.Context
import com.bopr.android.smailer.AccountHelper
import com.bopr.android.smailer.NotificationsHelper
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.consumer.EventMessengerTransport
import com.bopr.android.smailer.provider.telephony.PhoneEventInfo
import com.bopr.android.smailer.external.GoogleMail
import com.bopr.android.smailer.util.Mockable
import com.bopr.android.smailer.util.isValidEmailAddressList
import com.google.api.services.gmail.GmailScopes.GMAIL_SEND
import org.slf4j.LoggerFactory

/**
 * Mail transport.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
@Mockable
internal class MailTransport(context: Context) :
    EventMessengerTransport(context) {

    private val settings = Settings(context)
    private val accountHelper = AccountHelper(context)
    private val formatters = MailFormatterFactory(context)
    private val notifications by lazy { NotificationsHelper(context) }

    override fun sendMessageFor(
        event: PhoneEventInfo,
        onSuccess: () -> Unit,
        onError: (error: Exception) -> Unit
    ) {
        val recipients = settings.getEmailRecipients()
        if (!checkRecipients(recipients)) return

        val account = accountHelper.requirePrimaryGoogleAccount()
        val formatter = formatters.get(event)

        val mailMessage = MailMessage(
            subject = formatter.formatSubject(),
            body = formatter.formatBody(),
            from = account.name,
            recipients = recipients
        )

        try {
            GoogleMail(context, account, GMAIL_SEND).send(mailMessage)
            onSuccess()
        } catch (x: Exception) {
            onError(x)
        }
    }

    fun checkRecipients(recipients: String?): Boolean {
        if (recipients.isNullOrBlank()){
            notifications.showRecipientsError(R.string.no_recipients_specified)
            return false
        }

        if (!isValidEmailAddressList(recipients)) {
            notifications.showRecipientsError(R.string.invalid_recipient)
            return false
        }

        return true
    }

    companion object {

        private val log = LoggerFactory.getLogger("MailTransport")
    }
}