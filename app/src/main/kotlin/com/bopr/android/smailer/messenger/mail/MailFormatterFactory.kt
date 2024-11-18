package com.bopr.android.smailer.messenger.mail

import android.content.Context
import com.bopr.android.smailer.provider.battery.BatteryInfo
import com.bopr.android.smailer.provider.telephony.PhoneCallInfo

class MailFormatterFactory(private val context: Context) {

    fun createFormatter(data: Any): MailFormatter {
        return when (data) {
            is PhoneCallInfo -> PhoneCallMailFormatter(context, data)

            is BatteryInfo -> BatteryLevelMailFormatter(context, data)

            else -> throw IllegalArgumentException("No formatter for ${data::class}")
        }
    }

}
