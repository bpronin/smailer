package com.bopr.android.smailer.messenger.mail

import android.content.Context
import com.bopr.android.smailer.messenger.Event
import com.bopr.android.smailer.provider.battery.BatteryData
import com.bopr.android.smailer.provider.telephony.PhoneCallData

class MailFormatterFactory(private val context: Context) {

    fun createFormatter(event: Event): MailFormatter {
        return when (val data = event.payload) {
            is PhoneCallData ->
                PhoneCallMailFormatter(context, event, data)
            is BatteryData ->
                BatteryLevelMailFormatter(context, event, data)
            else ->
                throw IllegalArgumentException("No formatter for $data")
        }
    }

}
