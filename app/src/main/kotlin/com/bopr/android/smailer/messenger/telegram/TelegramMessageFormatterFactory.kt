package com.bopr.android.smailer.messenger.telegram

import android.content.Context
import com.bopr.android.smailer.provider.battery.BatteryLevelData
import com.bopr.android.smailer.provider.telephony.PhoneEventData

class TelegramMessageFormatterFactory(private val context: Context) {

    fun createFormatter(data: Any): TelegramMessageFormatter {
        return when (data) {
            is PhoneEventData ->
                TelegramPhoneEventFormatter(context, data)

            is BatteryLevelData ->
                TelegramBatteryLevelFormatter(context, data)

            else ->
                throw IllegalArgumentException("No formatter for ${data::class}")
        }
    }

}
