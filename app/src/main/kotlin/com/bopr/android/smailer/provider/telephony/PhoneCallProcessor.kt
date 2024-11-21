package com.bopr.android.smailer.provider.telephony

import android.content.Context
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.bopr.android.smailer.Settings.Companion.settings
import com.bopr.android.smailer.provider.Processor
import com.bopr.android.smailer.util.Bits

/**
 * Precesses phone events.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class PhoneCallProcessor(context: Context) : Processor<PhoneCallInfo>(context) {

    override fun getBypassReason(info: PhoneCallInfo): Bits {
        return PhoneCallFilter(
            settings.getPhoneProcessTriggers(),
            database.phoneBlacklist,
            database.phoneWhitelist,
            database.textBlacklist,
            database.textWhitelist
        ).test(info)
    }

    companion object {

        fun Context.processPhoneCall(info: PhoneCallInfo) {
            PhoneCallProcessor(this).add(info)
            WorkManager.getInstance(this).enqueue(
                OneTimeWorkRequest.Builder(PhoneCallProcessWorker::class.java).build()
            )
        }
    }

}