package com.bopr.android.smailer.processor.mail

import android.content.Context
import com.bopr.android.smailer.provider.telephony.PhoneEventData

class MailFormatterFactory(private val context: Context) {

    fun createFormatter(data: Any): MailFormatter {
        return when (data) {
            is PhoneEventData -> MailPhoneEventFormatter(
                context = context,
                event = data
            )
//            is BatteryEvent -> BatteryEventMailFormatter(context)
            else -> throw IllegalArgumentException("No formatter for ${data::class}")
        }
    }

}
