package com.bopr.android.smailer.messenger

import android.content.Context
import com.bopr.android.smailer.messenger.mail.MailMessenger
import com.bopr.android.smailer.messenger.pocketbase.PocketbaseMessenger
import com.bopr.android.smailer.messenger.telegram.TelegramMessenger
import com.bopr.android.smailer.messenger.telephony.SmsMessenger
import com.bopr.android.smailer.util.Logger
import com.bopr.android.smailer.util.Mockable
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

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

    fun hasEnabled() = messengers.any { it.isEnabled }

    suspend fun dispatch(event: Event) = coroutineScope {
        log.debug("Dispatching: $event")
        messengers.map {
            async {
                if (it.initialize()) {
                    it.send(event)
                }
            }
        }.awaitAll()
    }

    companion object {
        private val log = Logger("MessengerDispatcher")
    }
}