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

    private val messengers = arrayOf(
        MailMessenger(context),
        TelegramMessenger(context),
        SmsMessenger(context)
    )

    fun initialize(): Boolean {
        log.debug("Initializing")

        var initialized = false
        messengers.forEach {
            initialized = initialized or it.initialize()
        }
        return initialized
    }

    fun dispatch(
        message: Message,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        log.debug("Dispatching").verb(message)

        messengers.forEach { it.sendMessage(message, onSuccess, onError) }
    }

    companion object {

        private val log = Logger("MessageDispatcher")
    }
}