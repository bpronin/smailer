package com.bopr.android.smailer.messenger.telegram

import android.content.Context
import com.bopr.android.smailer.messenger.Event
import com.bopr.android.smailer.provider.battery.BatteryData
import com.bopr.android.smailer.provider.telephony.PhoneCallData

class TelegramFormatterFactory(private val context: Context) {

    fun createFormatter(event: Event): TelegramFormatter {
        val payload = event.payload
        return when (payload) {
            is PhoneCallData ->
                PhoneCallTelegramFormatter(context, event, payload)
            is BatteryData ->
                BatteryLevelTelegramFormatter(context, event, payload)
            else ->
                throw IllegalArgumentException("No formatter for $payload")
        }
    }

}
