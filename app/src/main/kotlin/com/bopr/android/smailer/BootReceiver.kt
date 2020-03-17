package com.bopr.android.smailer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bopr.android.smailer.Environment.startServices
import org.slf4j.LoggerFactory

/**
 * Starts application at device boot.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class BootReceiver : BroadcastReceiver() {

    private val log = LoggerFactory.getLogger("BootReceiver")

    override fun onReceive(context: Context, intent: Intent) {
        log.debug("Received intent: $intent")

        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            startServices(context)
        }
    }
}