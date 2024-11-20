package com.bopr.android.smailer.provider.battery

import android.content.Context
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.bopr.android.smailer.Settings.Companion.PREF_DISPATCH_BATTERY_LEVEL
import com.bopr.android.smailer.messenger.Event.Companion.FLAG_BYPASS_TRIGGER_OFF
import com.bopr.android.smailer.provider.Processor
import com.bopr.android.smailer.util.Bits

/**
 * Processes battery events.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class BatteryEventProcessor(context: Context) : Processor<BatteryInfo>(context) {

    override fun getBypassReason(data: BatteryInfo): Bits {
        var flags = Bits()
        if (!settings.getBoolean(PREF_DISPATCH_BATTERY_LEVEL)) {
            flags += FLAG_BYPASS_TRIGGER_OFF
        }
        return flags
    }

    companion object {

        fun Context.processBatteryEvent(info: BatteryInfo) {
            BatteryEventProcessor(this).add(info)
            WorkManager.getInstance(this).enqueue(
                OneTimeWorkRequest.Builder(BatteryEventProcessWorker::class.java).build()
            )
        }
    }
}