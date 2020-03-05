package com.bopr.android.smailer

import android.content.Intent
import androidx.core.app.JobIntentService
import org.slf4j.LoggerFactory

/**
 * Service that processes pending phone events.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class PendingCallProcessorService : JobIntentService() {

    override fun onHandleWork(intent: Intent) {
        log.trace("Handling intent: $intent")

        Database(this).use {
            CallProcessor(this, it).processPending()
        }
    }

    companion object {

        private val log = LoggerFactory.getLogger("PendingCallProcessorService")
    }
}