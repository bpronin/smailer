package com.bopr.android.smailer.consumer.mail

import android.content.Context
import com.bopr.android.smailer.AccountManager
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.consumer.EventMessengerTransport
import com.bopr.android.smailer.provider.telephony.PhoneEventInfo
import com.bopr.android.smailer.transport.GoogleMailSession
import com.bopr.android.smailer.util.Mockable
import com.google.api.services.gmail.GmailScopes.GMAIL_SEND

/**
 * Mail transport.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
@Mockable
internal class MailTransport(context: Context) : EventMessengerTransport(context) {

    private val settings = Settings(context)
    private val accountManager = AccountManager(context)
    private val formatters = MailFormatterFactory(context)

    override fun sendMessage(
        event: PhoneEventInfo,
        onSuccess: () -> Unit,
        onError: (error: Exception) -> Unit
    ) {
        val account = accountManager.requirePrimaryGoogleAccount()
        val formatter = formatters.get(event)

        val mailMessage = MailMessage(
            subject = formatter.formatSubject(),
            body = formatter.formatBody(),
            from = account.name,
            recipients = settings.getEmailRecipientsPlain()
        )

        try {
            GoogleMailSession(context, account, GMAIL_SEND).send(mailMessage)
            onSuccess()
        } catch (x: Exception) {
            onError(x)
        }

    }

}