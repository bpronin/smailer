package com.bopr.android.smailer.provider.telephony

import android.content.Context
import androidx.work.WorkerParameters
import com.bopr.android.smailer.Settings.Companion.settings
import com.bopr.android.smailer.data.Database.Companion.database
import com.bopr.android.smailer.provider.EventProcessor
import com.bopr.android.smailer.provider.EventProcessorWorker
import com.bopr.android.smailer.util.Bits
import com.bopr.android.smailer.util.Singleton

/**
 * Precesses phone events.
 *
 * @author Boris Pronin ([boris280471@gmail.com](mailto:boris280471@gmail.com))
 */
class PhoneCallEventProcessor private constructor(private val context: Context) :
    EventProcessor<PhoneCallData>(context) {

    override fun getBypassReason(payload: PhoneCallData): Bits {
        val filter = context.run {
            PhoneCallFilter(
                settings.getPhoneProcessTriggers(),
                database.phoneBlacklist.drain(),
                database.phoneWhitelist.drain(),
                database.textBlacklist.drain(),
                database.textWhitelist.drain()
            )
        }

        return filter.test(payload)
    }

    internal class ProcessWorker(context: Context, workerParams: WorkerParameters) :
        EventProcessorWorker(context, workerParams) {

        override fun doProcessEvents() {
            applicationContext.processPendingPhoneCalls()
        }
    }

    companion object {
        private val singleton = Singleton { PhoneCallEventProcessor(it) }

        internal fun Context.scheduleProcessPhoneCall(data: PhoneCallData) {
            singleton.getInstance(this).scheduleProcess(data, ProcessWorker::class)
        }

        internal fun Context.processPendingPhoneCalls() {
            singleton.getInstance(this).processPending()
        }
    }

}