package com.bopr.android.smailer.processor

import android.content.Context
import com.bopr.android.smailer.processor.mail.MailEventProcessor
import com.bopr.android.smailer.processor.telegram.TelegramEventProcessor
import com.bopr.android.smailer.provider.Event
import com.bopr.android.smailer.util.Mockable
import org.slf4j.LoggerFactory

/**
 * Dispatch events to event processors.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
@Mockable
class EventDispatcher(context: Context) {

    private val processors = arrayOf(
        MailEventProcessor(context),
        TelegramEventProcessor(context)
    )

    fun dispatch(event: Event) {
        processors
            .filter { it.isEnabled() }
            .forEach {
                runCatching { it.process(event) }
                    .onFailure { log.error("Event processing failed", it) }
            }
    }

    companion object {

        private val log = LoggerFactory.getLogger("EventDispatcher")
    }
}