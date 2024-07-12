package com.bopr.android.smailer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_BOOT_COMPLETED
import com.bopr.android.smailer.Environment.startApplicationServices
import org.slf4j.LoggerFactory

/**
 * Starts application at device boot.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class BootReceiver : BroadcastReceiver() {

    private val log = LoggerFactory.getLogger("BootReceiver")

    override fun onReceive(context: Context, intent: Intent) {
        log.trace("Received intent: {}", intent)

        if (intent.action == ACTION_BOOT_COMPLETED) {
            context.startApplicationServices()
        }
    }
}