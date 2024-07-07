package com.bopr.android.smailer.sender

import android.content.Context
import androidx.collection.arrayMapOf
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_MESSAGING_TRANSPORT_EMAIL
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_MESSAGING_TRANSPORT_TELEGRAM
import org.slf4j.LoggerFactory

/**
 * Sends messages using configured transports.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class Messenger(val context: Context) {

    private val transports = arrayMapOf(
        VAL_PREF_MESSAGING_TRANSPORT_EMAIL to lazyOf(GoogleMail(context)),
        VAL_PREF_MESSAGING_TRANSPORT_TELEGRAM to lazyOf(TelegramBot(context)),
    )

    fun sendMessages(vararg messages: EventMessage) {
        Settings(context).messagingTransports.forEach { id ->
            transports[id]?.run {
                value.sendMessages(*messages)
            }
        }
    }
}