package com.bopr.android.smailer

import android.content.Context
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
        log.debug("Handling intent: $intent")

        CallProcessor(this).processPending()
    }

    companion object {

        private val log = LoggerFactory.getLogger("PendingCallProcessorService")
        private const val JOB_ID = 1000

        fun startPendingCallProcessorService(context: Context) {
            log.debug("Starting service")

            enqueueWork(context, PendingCallProcessorService::class.java, JOB_ID,
                    Intent(context, PendingCallProcessorService::class.java))
        }
    }
}