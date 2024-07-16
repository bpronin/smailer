package com.bopr.android.smailer.provider.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_BATTERY_LOW
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.processor.EventDispatcher
import com.bopr.android.smailer.provider.Event
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

        if (intent.action == ACTION_BATTERY_LOW) {
            log.debug("Low battery level detected")

            if (Settings(context).getBoolean(Settings.PREF_PROCESS_BATTERY_LEVEL)) {
                val data = BatteryLevelData("low battery")
                val event = Event(payload = data)
                EventDispatcher(context).dispatch(event)
            }
        }
    }

}