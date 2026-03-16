package com.bopr.android.smailer.messenger

import android.content.Context
import com.bopr.android.smailer.messenger.mail.MailMessenger
import com.bopr.android.smailer.messenger.telegram.TelegramMessenger
import com.bopr.android.smailer.messenger.telephony.SmsMessenger
import com.bopr.android.smailer.messenger.pocketbase.PocketbaseMessenger
import com.bopr.android.smailer.util.Logger
import com.bopr.android.smailer.util.Mockable

/**
 * Dispatch message to appropriate messenger.
 *
 * @author Boris Pronin ([boris280471@gmail.com](mailto:boris280471@gmail.com))
 */
@Mockable
class MessageDispatcher(context: Context) {

    private val messengers: Array<Messenger> = arrayOf(
        MailMessenger(context),
        TelegramMessenger(context),
        SmsMessenger(context),
//        PocketbaseMessenger(context)
    )

    suspend fun prepare(): Boolean {
        log.debug("Preparing")

        var prepared = false
        messengers.forEach {
            prepared = prepared or it.prepare()
        }
        return prepared
    }

    suspend fun dispatch(event: Event, onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        log.debug("Dispatching: $event")

        messengers.forEach { it.send(event, onSuccess, onError) }
    }

    companion object {
        private val log = Logger("MessageDispatcher")
    }
}