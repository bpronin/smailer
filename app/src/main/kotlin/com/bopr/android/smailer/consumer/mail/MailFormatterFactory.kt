package com.bopr.android.smailer.consumer.mail

import android.content.Context
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.provider.telephony.PhoneEventInfo
import com.bopr.android.smailer.util.parseLocale
import com.bopr.android.smailer.util.tryGetContactName

class MailFormatterFactory(private val context: Context) {

    private val settings = Settings(context)

    fun get(event: PhoneEventInfo): MailFormatter {
        return PhoneEventMailFormatter(
            context = context,
            event = event,
            contactName = tryGetContactName(context, event.phone),
            deviceName = settings.getDeviceAlias(),
            options = settings.getEmailContent(),
            serviceAccount = settings.getRemoteControlAccountName(),
            phoneSearchUrl = settings.getPhoneSearchUrl(),
            locale = parseLocale(settings.getEmailLocale())
        )

//        return when (event.payload) {
//            is PhoneEventInfo -> PhoneEventMailFormatter(
//                context = context,
//                event = event.payload,
//                contactName = tryGetContactName(context, event.payload.phone),
//                deviceName = settings.getDeviceAlias(),
//                options = settings.getEmailContent(),
//                serviceAccount = settings.getRemoteControlAccountName(),
//                phoneSearchUrl = settings.getPhoneSearchUrl(),
//                locale = parseLocale(settings.getEmailLocale())
//            )
////            is BatteryEvent -> BatteryEventMailFormatter(context)
//            else -> throw IllegalArgumentException("No formatter for ${event::class}")
//        }
    }

}
