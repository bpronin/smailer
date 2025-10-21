package com.bopr.android.smailer.messenger.telegram

import android.content.Context
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_MESSAGE_CONTENT_BODY
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_MESSAGE_CONTENT_CALLER
import com.bopr.android.smailer.messenger.Event
import com.bopr.android.smailer.provider.telephony.PhoneCallData
import com.bopr.android.smailer.util.phoneCallTypeText
import com.bopr.android.smailer.util.formatDuration
import com.bopr.android.smailer.util.formatPhoneNumber
import com.bopr.android.smailer.util.getContactName

/**
 * Formats Telegram message from phone events.
 *
 * @author Boris Pronin ([boris280471@gmail.com](mailto:boris280471@gmail.com))
 */
class PhoneCallTelegramFormatter(
    context: Context,
    event: Event,
    private val info: PhoneCallData
) : BaseTelegramFormatter(
    context,
    info.startTime,
    event.processTime,
    event.location
) {

    override fun getTitle(): String {
        return string(phoneCallTypeText(info))
    }

    override fun getBody(): String {
        if (!settings.hasTelegramMessageContent(VAL_PREF_MESSAGE_CONTENT_BODY)) return ""

        return when {
            info.isMissed ->
                string(R.string.you_had_missed_call)

            info.isSms ->
                info.text!!

            else -> {
                val duration = formatDuration(info.callDuration)
                if (info.isIncoming) {
                    string(R.string.you_had_incoming_call, duration)
                } else {
                    string(R.string.you_had_outgoing_call, duration)
                }
            }
        }
    }

    override fun getSenderName(): String {
        if (!settings.hasTelegramMessageContent(VAL_PREF_MESSAGE_CONTENT_CALLER)) return ""

        return string(
            when {
                info.isSms -> R.string.sender_phone
                info.isIncoming -> R.string.caller_phone
                else -> R.string.called_phone
            },
            formatPhoneNumber(info.phone),
            context.getContactName(info.phone)?.let { " ($it)" } ?: ""
        )
    }


}