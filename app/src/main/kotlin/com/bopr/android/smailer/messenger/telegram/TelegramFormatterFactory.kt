package com.bopr.android.smailer.messenger.telegram

import android.content.Context
import com.bopr.android.smailer.provider.battery.BatteryInfo
import com.bopr.android.smailer.provider.telephony.PhoneCallInfo

class TelegramFormatterFactory(private val context: Context) {

    fun createFormatter(data: Any): TelegramFormatter {
        return when (data) {
            is PhoneCallInfo ->
                PhoneCallTelegramFormatter(context, data)

            is BatteryInfo ->
                BatteryLevelTelegramFormatter(context)

            else ->
                throw IllegalArgumentException("No formatter for ${data::class}")
        }
    }

}
