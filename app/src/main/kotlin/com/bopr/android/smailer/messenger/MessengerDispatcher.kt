package com.bopr.android.smailer.messenger

import android.content.Context
import com.bopr.android.smailer.messenger.mail.MailMessenger
import com.bopr.android.smailer.messenger.pocketbase.PocketbaseMessenger
import com.bopr.android.smailer.messenger.telegram.TelegramMessenger
import com.bopr.android.smailer.messenger.telephony.SmsMessenger
import com.bopr.android.smailer.util.Logger
import com.bopr.android.smailer.util.Mockable

/**
 * Dispatch message to appropriate messenger.
 *
 * @author Boris Pronin ([boris280471@gmail.com](mailto:boris280471@gmail.com))
 */
@Mockable
class MessengerDispatcher(context: Context) {

    private val messengers: Array<Messenger> = arrayOf(
        MailMessenger(context),
        TelegramMessenger(context),
        SmsMessenger(context),
        PocketbaseMessenger(context)
    )

    suspend fun initialize(): Boolean {
        log.debug("Initializing")
        return messengers.fold(false) { acc, messenger ->
            messenger.initialize() or acc
        }
    }

    suspend fun dispatch(event: Event) {
        log.debug("Dispatching: $event")
        messengers.forEach { it.send(event) }
    }

    companion object {
        private val log = Logger("MessengerDispatcher")
    }
}