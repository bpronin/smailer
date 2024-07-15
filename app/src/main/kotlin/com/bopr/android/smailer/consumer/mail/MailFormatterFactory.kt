package com.bopr.android.smailer.consumer.mail

import android.content.Context
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_ACCOUNT
import com.bopr.android.smailer.provider.telephony.PhoneEventInfo
import com.bopr.android.smailer.util.parseLocale
import com.bopr.android.smailer.util.tryGetContactName

class MailFormatterFactory(private val context: Context) {

    private val settings = Settings(context)

    fun createFormatter(event: PhoneEventInfo): MailFormatter {
        return when (event) {
            is PhoneEventInfo -> MailPhoneEventFormatter(
                context = context,
                event = event,
                contactName = tryGetContactName(context, event.phone),
                deviceName = settings.getDeviceName(),
                options = settings.getEmailContent(),
                serviceAccount = settings.getString(PREF_REMOTE_CONTROL_ACCOUNT),
                phoneSearchUrl = settings.getPhoneSearchUrl(),
                locale = parseLocale(settings.getMessageLocale())
            )
//            is BatteryEvent -> BatteryEventMailFormatter(context)
            else -> throw IllegalArgumentException("No formatter for ${event::class}")
        }
    }

}
