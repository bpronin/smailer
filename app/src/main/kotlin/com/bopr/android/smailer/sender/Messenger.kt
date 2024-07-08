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
    private val emailTransports by lazyOf(GoogleMail(context))
    private val telegramTransports by lazyOf(TelegramBot(context))

    fun sendMessages(vararg messages: EventMessage) {
        if (settings.telegramMessengerEnabled)
            telegramTransports.silentSendMessages(*messages)
        if (settings.emailMessengerEnabled)
            emailTransports.silentSendMessages(*messages)
    }

    private fun MessengerTransport.silentSendMessages(vararg messages: EventMessage){
        try {
            sendMessages(*messages)
        } catch (x: Exception) {
            log.error("Send message failed", x)
        }
    }
}