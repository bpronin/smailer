package com.bopr.android.smailer.consumer.telegram

import android.content.Context
import androidx.annotation.StringRes
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.provider.telephony.PhoneEventInfo
import com.bopr.android.smailer.util.eventTypeText
import com.bopr.android.smailer.util.formatDuration
import com.bopr.android.smailer.util.httpEncoded
import com.bopr.android.smailer.util.localeResources
import com.bopr.android.smailer.util.parseLocale
import com.bopr.android.smailer.util.tryGetContactName

/**
 * Formats Telegram message from phone event.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class PhoneEventTelegramFormatter(private val context: Context, private val event: PhoneEventInfo) :
    MessageFormatter(context) {

    private val settings = Settings(context)
    private val locale = parseLocale(settings.getMessageLocale())

    //    private val deviceName = settings.getDeviceAlias()
    private val resources = context.localeResources(locale)

    //    private val timeFormat = getDateTimeInstance(LONG, LONG, locale)
    private val newline = "\n".httpEncoded()

    override fun formatMessage(): String {
        return "${formatHeader()}${formatBody()}${formatFooter()}"
    }

    private fun formatHeader(): String {
        return if (settings.isTelegramMessageHeaderEnabled()) {
            "<b>" + string(eventTypeText(event)) + "</b>" + newline + newline
        } else ""
    }

    private fun formatFooter(): String {
        return if (settings.isTelegramMessageFooterEnabled()) {
            newline + newline + "<i>" + formatCaller() + "</i>"
        } else ""
    }

    private fun formatBody(): String {
        return when {
            event.isMissed -> string(R.string.you_had_missed_call)

            event.isSms -> event.text!!

            else -> {
                val duration = formatDuration(event.callDuration)
                if (event.isIncoming) {
                    string(R.string.you_had_incoming_call, duration)
                } else {
                    string(R.string.you_had_outgoing_call, duration)
                }
            }
        }
    }


    private fun formatCaller(): String {
        val patternRes = when {
            event.isSms -> R.string.sender_phone
            event.isIncoming -> R.string.caller_phone
            else -> R.string.called_phone
        }

        val contact = tryGetContactName(context, event.phone) ?: string(R.string.unknown_contact)
        return string(patternRes, event.phone, contact)
    }

    private fun formatDeviceName(): String {
        return if (settings.isTelegramMessageDeviceNameEnabled()) {
            string(R.string._from_device, settings.getDeviceName())
        } else ""
    }

    private fun string(@StringRes resId: Int, vararg formatArgs: Any?): String =
        resources.getString(resId, *formatArgs)

    private fun string(@StringRes resId: Int) = resources.getString(resId)

}