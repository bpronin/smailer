package com.bopr.android.smailer.provider.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_BATTERY_LOW
import com.bopr.android.smailer.R
import com.bopr.android.smailer.provider.battery.BatteryEventEventProcessor.Companion.processBatteryEvent
import com.bopr.android.smailer.util.Logger

/**
 * Processes battery events.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class BatteryLevelReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        log.debug("Received intent: $intent")

        if (intent.action == ACTION_BATTERY_LOW) {
            log.debug("Low battery level detected")

            context.processBatteryEvent(
                BatteryData(context.getString(R.string.low_battery_level))
            )
        }
    }

    companion object {

        private val log = Logger("BatteryLevelReceiver")
    }
}