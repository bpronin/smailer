package com.bopr.android.smailer.sender

import android.content.Context
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.util.Mockable
import org.slf4j.LoggerFactory

/**
 * Sends messages using configured transports.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
@Mockable
class Messenger(context: Context) {

    private val log = LoggerFactory.getLogger("Messenger")
    private val settings = Settings(context)
    private val emailTransport by lazyOf(GoogleMail(context))
    private val telegramTransport by lazyOf(Telegram(context))

    fun sendMessage(message: EventMessage) {
        if (settings.telegramMessengerEnabled)
            silentSendMessage(telegramTransport, message)
        if (settings.emailMessengerEnabled)
            silentSendMessage(emailTransport, message)
    }

    private fun silentSendMessage(transport: Transport, message: EventMessage) {
        try {
            transport.sendMessage(message) { resultCode ->
                log.info("Send message result: $resultCode")
//                log.error("Error while sending message.", resultCode)
            }
        } catch (x: Exception) {
            log.error("Send message failed", x)
        }
        //todo: consider to show notification on errors
    }
}