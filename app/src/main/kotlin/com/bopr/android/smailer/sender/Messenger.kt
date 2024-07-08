package com.bopr.android.smailer.sender

import android.content.Context
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.util.Mockable

/**
 * Sends messages using configured transports.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
@Mockable
class Messenger(context: Context) {

    private val settings = Settings(context)
    private val emailTransports by lazyOf(GoogleMail(context))
    private val telegramTransports by lazyOf(TelegramBot(context))

    fun sendMessages(vararg messages: EventMessage) {
        if (settings.telegramMessengerEnabled) telegramTransports.sendMessages(*messages)
        if (settings.emailMessengerEnabled) emailTransports.sendMessages(*messages)
    }
}