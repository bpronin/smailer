package com.bopr.android.smailer.provider.battery

import android.content.Context
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.bopr.android.smailer.Settings.Companion.PREF_DISPATCH_BATTERY_LEVEL
import com.bopr.android.smailer.Settings.Companion.settings
import com.bopr.android.smailer.messenger.Event.Companion.FLAG_BYPASS_TRIGGER_OFF
import com.bopr.android.smailer.provider.EventProcessor
import com.bopr.android.smailer.util.Bits

/**
 * Processes battery events.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class BatteryEventEventProcessor(private val context: Context) : EventProcessor<BatteryData>(context) {

    override fun getBypassReason(data: BatteryData): Bits {
        var flags = Bits()
        if (!context.settings.getBoolean(PREF_DISPATCH_BATTERY_LEVEL)) {
            flags += FLAG_BYPASS_TRIGGER_OFF
        }
        return flags
    }

    companion object {

        fun Context.processBatteryEvent(info: BatteryData) {
            BatteryEventEventProcessor(this).add(info)
            WorkManager.getInstance(this).enqueue(
                OneTimeWorkRequest.Builder(BatteryEventProcessWorker::class.java).build()
            )
        }
    }
}