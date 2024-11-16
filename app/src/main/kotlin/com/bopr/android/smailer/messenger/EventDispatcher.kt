package com.bopr.android.smailer.messenger

import android.content.Context
import com.bopr.android.smailer.messenger.mail.MailMessenger
import com.bopr.android.smailer.messenger.telegram.TelegramMessenger
import com.bopr.android.smailer.messenger.telephony.SmsMessenger
import com.bopr.android.smailer.provider.Event
import com.bopr.android.smailer.util.Mockable
import com.bopr.android.smailer.util.Logger

/**
 * Dispatch event to appropriate messenger.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
@Mockable
class EventDispatcher(context: Context) {

    private val availableMessengers = arrayOf(
        MailMessenger(context),
        TelegramMessenger(context),
        SmsMessenger(context)
    )

    private lateinit var preparedMessengers: List<Messenger>

    fun prepare() {
        preparedMessengers = availableMessengers.filter { it.isEnabled() && it.prepare() }

        log.debug("Prepared")
    }

    fun dispatch(
        event: Event,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        // TODO: use coroutines or threads
        var lastError: Throwable? = null

        preparedMessengers.forEach {
            it.sendMessage(event,
                onSuccess = {

                },
                onError = { error ->
                    lastError = error
                })
        }

        log.debug("Dispatched $event")

        if (lastError == null) onSuccess() else onError(lastError)
    }

    companion object {

        private val log = Logger("EventDispatcher")
    }
}