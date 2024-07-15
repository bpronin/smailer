package com.bopr.android.smailer.consumer.telegram

import android.content.Context
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.provider.telephony.PhoneEventInfo

class MessageFormatterFactory(private val context: Context) {

    private val settings = Settings(context)

    fun createFormatter(event: PhoneEventInfo): MessageFormatter {
        return when (event) {
            is PhoneEventInfo -> TelegramPhoneEventFormatter(context, event)
//            is BatteryEvent -> BatteryEventMailFormatter(context, event)
            else -> throw IllegalArgumentException("No formatter for ${event::class}")
        }
    }

}
