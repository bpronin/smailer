package com.bopr.android.smailer.consumer

import android.content.Context
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.consumer.mail.MailTransport
import com.bopr.android.smailer.consumer.telegram.TelegramTransport
import com.bopr.android.smailer.provider.telephony.PhoneEventInfo
import com.bopr.android.smailer.transport.GoogleMailSession
import com.bopr.android.smailer.transport.Telegram
import com.bopr.android.smailer.util.Mockable
import org.slf4j.LoggerFactory

/**
 * Sends messages using configured transports.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
@Mockable
class EventMessenger(context: Context) {

    private val log = LoggerFactory.getLogger("Messenger")
    private val settings = Settings(context)
    private val emailTransport by lazyOf(MailTransport(context))
    private val telegramTransport by lazyOf(TelegramTransport(context))

    fun sendMessageFor(event: PhoneEventInfo) {
        if (settings.getTelegramMessengerEnabled()) silentSend(telegramTransport, event)
        if (settings.getEmailMessengerEnabled()) silentSend(emailTransport, event)
    }

    private fun silentSend(transport: EventMessengerTransport, event: PhoneEventInfo) {
        try {
            transport.sendMessage(event,
                onSuccess = {
                    log.debug("Message sent successfully.")
                },
                onError = { error ->
                    log.error("Error while sending message.", error)
                })
        } catch (x: Exception) {
            log.error("Send message failed", x)
        }
        //todo: consider to show notification on errors
    }
}