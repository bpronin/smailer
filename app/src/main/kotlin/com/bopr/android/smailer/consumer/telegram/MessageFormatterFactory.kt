package com.bopr.android.smailer.consumer.telegram

import android.content.Context
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.provider.telephony.PhoneEventInfo
import com.bopr.android.smailer.util.parseLocale

class MessageFormatterFactory(private val context: Context) {

    private val settings = Settings(context)

    fun createFormatter(event: PhoneEventInfo): MessageFormatter {
        return when (event) {
            is PhoneEventInfo -> PhoneEventTelegramFormatter(context, event)
//            is BatteryEvent -> BatteryEventMailFormatter(context, event)
            else -> throw IllegalArgumentException("No formatter for ${event::class}")
        }
    }

}
