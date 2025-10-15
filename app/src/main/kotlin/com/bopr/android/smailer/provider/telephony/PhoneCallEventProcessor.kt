package com.bopr.android.smailer.provider.telephony

import android.content.Context
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.bopr.android.smailer.Settings.Companion.settings
import com.bopr.android.smailer.data.Database.Companion.database
import com.bopr.android.smailer.provider.EventProcessor
import com.bopr.android.smailer.util.Bits

/**
 * Precesses phone events.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class PhoneCallEventProcessor(context: Context) : EventProcessor<PhoneCallData>(context) {

    private val settings = context.settings
    private val database = context.database

    override fun getBypassReason(info: PhoneCallData): Bits {
        return PhoneCallFilter(
            settings.getPhoneProcessTriggers(),
            database.phoneBlacklist,
            database.phoneWhitelist,
            database.textBlacklist,
            database.textWhitelist
        ).test(info)
    }

    companion object {

        fun Context.processPhoneCall(info: PhoneCallData) {
            PhoneCallEventProcessor(this).add(info)
            WorkManager.getInstance(this).enqueue(
                OneTimeWorkRequest.Builder(PhoneCallProcessWorker::class.java).build()
            )
        }
    }

}