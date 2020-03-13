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

    private val log = LoggerFactory.getLogger("RemoteControlService")

    override fun onHandleWork(intent: Intent) {
        log.trace("Handling intent: $intent")

        RemoteControlProcessor(this).checkMailbox()
    }
}