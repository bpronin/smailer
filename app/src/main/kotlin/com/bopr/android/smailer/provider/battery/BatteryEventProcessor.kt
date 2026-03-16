package com.bopr.android.smailer.provider.battery

import android.content.Context
import androidx.work.WorkerParameters
import com.bopr.android.smailer.Settings.Companion.PREF_DISPATCH_BATTERY_LEVEL
import com.bopr.android.smailer.Settings.Companion.settings
import com.bopr.android.smailer.messenger.Event.Companion.FLAG_BYPASS_TRIGGER_OFF
import com.bopr.android.smailer.provider.EventProcessor
import com.bopr.android.smailer.provider.EventProcessorWorker
import com.bopr.android.smailer.util.Bits
import com.bopr.android.smailer.util.Singleton

/**
 * Processes battery events.
 *
 * @author Boris Pronin ([boris280471@gmail.com](mailto:boris280471@gmail.com))
 */
class BatteryEventProcessor(private val context: Context) :
    EventProcessor<BatteryData>(context) {

    override fun getBypassReason(payload: BatteryData): Bits {
        var flags = Bits()
        if (!context.settings.getBoolean(PREF_DISPATCH_BATTERY_LEVEL)) {
            flags += FLAG_BYPASS_TRIGGER_OFF
        }
        return flags
    }

    internal class ProcessWorker(context: Context, workerParams: WorkerParameters) :
        EventProcessorWorker(context, workerParams) {

        override suspend fun doProcessEvents() {
            applicationContext.processPendingBatteryEvents()
        }
    }

    companion object {
        private val singleton = Singleton { BatteryEventProcessor(it) }

        internal fun Context.scheduleProcessBatteryEvent(data: BatteryData) {
            singleton.getInstance(this).scheduleProcess(data, ProcessWorker::class)
        }

        internal suspend fun Context.processPendingBatteryEvents() {
            singleton.getInstance(this).processPending()
        }
    }
}

