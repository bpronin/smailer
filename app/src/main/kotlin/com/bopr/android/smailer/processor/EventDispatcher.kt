package com.bopr.android.smailer.processor

import android.content.Context
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_MESSENGER_ENABLED
import com.bopr.android.smailer.Settings.Companion.PREF_TELEGRAM_MESSENGER_ENABLED
import com.bopr.android.smailer.processor.mail.MailTransport
import com.bopr.android.smailer.processor.telegram.TelegramTransport
import com.bopr.android.smailer.provider.Event
import com.bopr.android.smailer.util.Mockable
import org.slf4j.LoggerFactory

/**
 * Sends messages using configured transports.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
@Mockable
class EventDispatcher(context: Context) {

    private val settings = Settings(context)
    private val emailTransport by lazyOf(MailTransport(context))
    private val telegramTransport by lazyOf(TelegramTransport(context))

    fun dispatch(event: Event) {
        if (settings.getBoolean(PREF_TELEGRAM_MESSENGER_ENABLED))
            process(telegramTransport, event)
        if (settings.getBoolean(PREF_EMAIL_MESSENGER_ENABLED))
            process(emailTransport, event)
    }

    private fun process(processor: EventProcessor, event: Event) {
        try {
            processor.process(event,
                onSuccess = {
                    log.debug("Event processed successfully")
                },
                onError = { error ->
                    log.error("Error while processing event", error)
                })
        } catch (x: Exception) {
            log.error("Event processing failed", x)
        }
    }

    companion object {

        private val log = LoggerFactory.getLogger("EventDispatcher")
    }
}