package com.bopr.android.smailer.remote

import android.content.Intent
import androidx.core.app.JobIntentService
import org.slf4j.LoggerFactory

/**
 * Remote control service.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class RemoteControlService : JobIntentService() {

    private lateinit var processor: RemoteControlProcessor

    override fun onCreate() {
        super.onCreate()
        processor = RemoteControlProcessor(this)
    }

    override fun onHandleWork(intent: Intent) {
        log.trace("Handling intent: $intent")

        try {
            processor.checkMailbox()
        } catch (x: Exception) {
            log.warn("Failed handling service mail: ", x)
        }
    }

    companion object {

        private val log = LoggerFactory.getLogger("RemoteControlService")
    }
}