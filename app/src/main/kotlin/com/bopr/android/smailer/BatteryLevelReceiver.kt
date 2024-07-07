package com.bopr.android.smailer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_BATTERY_LOW
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_LOW_BATTERY_LEVEL
import com.bopr.android.smailer.sender.EventMessage
import com.bopr.android.smailer.sender.Messenger
import com.bopr.android.smailer.util.deviceName
import com.bopr.android.smailer.util.hasInternetConnection
import com.bopr.android.smailer.util.runInBackground
import org.slf4j.LoggerFactory

/**
 * Starts email processing on low battery level.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class BatteryLevelReceiver : BroadcastReceiver() {

    private val log = LoggerFactory.getLogger("BatteryLevelReceiver")

    override fun onReceive(context: Context, intent: Intent) {
        log.trace("Received intent: {}", intent)

        if (intent.action == ACTION_BATTERY_LOW && checkInternet(context)) {
            log.trace("Low battery level detected")

            val settings = Settings(context)
            if (settings.emailTriggers.contains(VAL_PREF_LOW_BATTERY_LEVEL)) {
                val validator = RecipientsValidator(Notifications(context))
                if (validator.checkRecipients(settings.emailRecipients)) {
                    runInBackground { sendMessage(context) }
                }
            }
        }
    }

    private fun checkInternet(context: Context): Boolean {
        /* check it before all to avoid awaiting timeout while sending */
        return context.hasInternetConnection().also {
            if (!it) log.warn("No internet connection")
        }
    }

    private fun sendMessage(context: Context) {
        Messenger(context).sendMessages(
            EventMessage(
                subject = "[SMailer] Battery level",
                text = "Device: " + deviceName() + "<br> Battery level is low."
            )
        )
    }
}