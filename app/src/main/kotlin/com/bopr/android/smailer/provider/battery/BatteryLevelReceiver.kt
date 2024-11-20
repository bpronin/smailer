package com.bopr.android.smailer.provider.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_BATTERY_LOW
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.PREF_DISPATCH_BATTERY_LEVEL
import com.bopr.android.smailer.messenger.MessageDispatcher
import com.bopr.android.smailer.messenger.Event
import com.bopr.android.smailer.provider.Processor
import com.bopr.android.smailer.util.Logger

/**
 * Processes battery events.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class BatteryLevelReceiver : Processor<BatteryInfo>, BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        log.debug("Received intent: $intent" )

        if (intent.action == ACTION_BATTERY_LOW) {
            log.debug("Low battery level detected")

            if (Settings(context).getBoolean(PREF_DISPATCH_BATTERY_LEVEL)) {
                val info = BatteryInfo("low battery")
                val event = Event(payload = info)
                MessageDispatcher(context).dispatch(event, {}, {})
            }
        }
    }

    override fun add(data: BatteryInfo) {
        TODO("Not yet implemented")
    }

    override fun process(): Int {
        TODO("Not yet implemented")
    }

    companion object {

        private val log = Logger("BatteryLevelReceiver")
    }
}