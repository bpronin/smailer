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

    private val availableProcessors = arrayOf(
        MailEventProcessor(context),
        TelegramEventProcessor(context)
    )

    private lateinit var preparedProcessors: List<EventProcessor>

    fun prepare() {
        preparedProcessors = availableProcessors.filter { it.isEnabled() && it.prepare() }

        log.debug("Prepared")
    }

    fun dispatch(event: Event) {
        preparedProcessors.forEach { it.process(event) }

        log.debug("Dispatched {}", event)
    }

    companion object {

        private val log = LoggerFactory.getLogger("EventDispatcher")
    }
}