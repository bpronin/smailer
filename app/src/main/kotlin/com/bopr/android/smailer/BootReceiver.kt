package com.bopr.android.smailer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_BOOT_COMPLETED
import com.bopr.android.smailer.AppStartup.startupApplication
import com.bopr.android.smailer.util.Logger

/**
 * Starts application at device boot.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        log.debug("Received intent: $intent")

        if (intent.action == ACTION_BOOT_COMPLETED) {
            context.startupApplication()
        }
    }

    companion object {

        private val log = Logger("BootReceiver")
    }
}