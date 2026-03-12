package com.bopr.android.smailer.messenger.telegram

import android.content.Context
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_MESSAGE_CONTENT_BODY
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_MESSAGE_CONTENT_CALLER
import com.bopr.android.smailer.messenger.Event
import com.bopr.android.smailer.provider.telephony.PhoneCallData
import com.bopr.android.smailer.util.formatDuration
import com.bopr.android.smailer.util.formatPhoneNumber
import com.bopr.android.smailer.util.getContactName
import com.bopr.android.smailer.util.phoneCallAddressFormat
import com.bopr.android.smailer.util.phoneCallTypeText

/**
 * Formats Telegram message from phone events.
 *
 * @author Boris Pronin ([boris280471@gmail.com](mailto:boris280471@gmail.com))
 */
class PhoneCallTelegramFormatter(
    context: Context,
    event: Event,
    private val data: PhoneCallData
) : BaseTelegramFormatter(
    context,
    data.startTime,
    event.processTime,
    event.location
) {

    override fun getTitle(): String {
        return string(phoneCallTypeText(data))
    }

    override fun getBody(): String {
        if (!settings.hasTelegramMessageContent(VAL_PREF_MESSAGE_CONTENT_BODY)) return ""

        return when {
            data.isMissed ->
                string(R.string.you_had_missed_call)

            data.isSms ->
                data.text!!

            else -> {
                val duration = formatDuration(data.callDuration)
                if (data.isIncoming) {
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
            phoneCallAddressFormat(data),
            formatPhoneNumber(data.phone),
            context.getContactName(data.phone)?.let { " ($it)" } ?: ""
        )
    }

}