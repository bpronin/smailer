package com.bopr.android.smailer.consumer.telegram

import android.content.Context
import com.bopr.android.smailer.consumer.EventMessengerTransport
import com.bopr.android.smailer.provider.telephony.PhoneEventInfo
import com.bopr.android.smailer.external.Telegram

/**
 * Telegram transport.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class TelegramTransport(context: Context) : EventMessengerTransport(context) {

    private val telegram = Telegram(context)

    override fun sendMessageFor(
        event: PhoneEventInfo,
        onSuccess: () -> Unit,
        onError: (error: Exception) -> Unit
    ) {
        telegram.sendMessage(formatMessage(event), onSuccess, onError)
    }

    private fun formatMessage(event: PhoneEventInfo): String {
        return event.toString()
    }

}
