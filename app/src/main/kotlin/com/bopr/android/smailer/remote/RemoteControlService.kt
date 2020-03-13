package com.bopr.android.smailer.remote

import android.content.Intent
import androidx.core.app.JobIntentService
import com.bopr.android.smailer.Database
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

        Database(this).use {
            RemoteControlProcessor(this, it).checkMailbox()
        }
    }
}