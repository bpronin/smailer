package com.bopr.android.smailer

import android.app.IntentService
import android.content.Context
import android.content.Intent
import org.slf4j.LoggerFactory

/**
 * Service that processes phone event.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class CallProcessorService : IntentService("call-processor") {

    override fun onHandleIntent(intent: Intent?) {
        log.debug("Running")

        intent?.getParcelableExtra<PhoneEvent>(EXTRA_EVENT)?.let {
            try {
                CallProcessor(this).process(it)
            } catch (x: Exception) {
                log.warn("Processing failed: ", x)
            }
        }
    }

    companion object {

        private val log = LoggerFactory.getLogger("CallProcessorService")
        private const val EXTRA_EVENT = "event"

        /**
         * Start the service.
         *
         * @param context context
         * @param event   event
         */
        fun startCallProcessingService(context: Context, event: PhoneEvent) {
            log.debug("Starting service for: $event")

            context.startService(Intent(context, CallProcessorService::class.java)
                    .putExtra(EXTRA_EVENT, event))
        }
    }
}