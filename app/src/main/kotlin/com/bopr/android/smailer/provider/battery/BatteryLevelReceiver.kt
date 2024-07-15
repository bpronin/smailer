package com.bopr.android.smailer.provider.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.slf4j.LoggerFactory

/**
 * Starts email processing on low battery level.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class BatteryLevelReceiver : BroadcastReceiver() {

    private val log = LoggerFactory.getLogger("BatteryLevelReceiver")

    override fun onReceive(context: Context, intent: Intent) {
        log.debug("Received intent: {}", intent)

        if (intent.action == Intent.ACTION_BATTERY_LOW) {
            log.debug("Low battery level detected")

//            if (Settings(context).isBatteryEventsEnabled()) {
//                val event = Event(
//                    BatteryLevelInfo(
//                        "Battery level",
//                        "Device: " + deviceName() + "<br> Battery level is low."
//                    )
//                )
//                EventMessenger(context).sendMessageFor(event)
//            }
        }
    }

}