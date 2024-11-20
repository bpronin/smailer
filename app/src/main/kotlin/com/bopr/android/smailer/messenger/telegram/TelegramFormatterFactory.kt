package com.bopr.android.smailer.messenger.telegram

import android.content.Context
import com.bopr.android.smailer.messenger.Event
import com.bopr.android.smailer.provider.battery.BatteryInfo
import com.bopr.android.smailer.provider.telephony.PhoneCallInfo

class TelegramFormatterFactory(private val context: Context) {

    fun createFormatter(event: Event): TelegramFormatter {
        val payload = event.payload
        return when (payload) {
            is PhoneCallInfo ->
                PhoneCallTelegramFormatter(context, event, payload)
            is BatteryInfo ->
                BatteryLevelTelegramFormatter(context, event, payload)
            else ->
                throw IllegalArgumentException("No formatter for $payload")
        }
    }

}
