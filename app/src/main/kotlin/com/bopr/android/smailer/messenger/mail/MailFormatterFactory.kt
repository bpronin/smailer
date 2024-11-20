package com.bopr.android.smailer.messenger.mail

import android.content.Context
import com.bopr.android.smailer.messenger.Event
import com.bopr.android.smailer.provider.battery.BatteryInfo
import com.bopr.android.smailer.provider.telephony.PhoneCallInfo

class MailFormatterFactory(private val context: Context) {

    fun createFormatter(event: Event): MailFormatter {
        val data = event.payload
        return when (data) {
            is PhoneCallInfo ->
                PhoneCallMailFormatter(context, event, data)
            is BatteryInfo ->
                BatteryLevelMailFormatter(context, event, data)
            else ->
                throw IllegalArgumentException("No formatter for $data")
        }
    }

}
