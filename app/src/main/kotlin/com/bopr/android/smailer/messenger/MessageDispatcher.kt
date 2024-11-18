package com.bopr.android.smailer.messenger

import android.content.Context
import com.bopr.android.smailer.messenger.mail.MailMessenger
import com.bopr.android.smailer.messenger.telegram.TelegramMessenger
import com.bopr.android.smailer.messenger.telephony.SmsMessenger
import com.bopr.android.smailer.util.Logger
import com.bopr.android.smailer.util.Mockable

/**
 * Dispatch message to appropriate messenger.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
@Mockable
class MessageDispatcher(context: Context) {

    private val availableMessengers = arrayOf(
        MailMessenger(context),
        TelegramMessenger(context),
        SmsMessenger(context)
    )

    private lateinit var preparedMessengers: List<Messenger>

    fun initialize() {
        preparedMessengers = availableMessengers.filter { it.isEnabled() && it.initialize() }

        log.debug("Initialized")
    }

    fun dispatch(
        message: Message,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        for (messenger in preparedMessengers) {
            // TODO: what if some of messengers succeeded and some failed ?
            messenger.sendMessage(message, onSuccess, onError)
        }

        log.debug("Dispatched").verb(message)
    }

    companion object {

        private val log = Logger("MessageDispatcher")
    }
}