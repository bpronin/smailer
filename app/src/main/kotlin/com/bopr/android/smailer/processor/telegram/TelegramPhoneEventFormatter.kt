package com.bopr.android.smailer.processor.telegram

import android.content.Context
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_MESSAGE_CONTENT_BODY
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_MESSAGE_CONTENT_CALLER
import com.bopr.android.smailer.provider.telephony.PhoneEventData
import com.bopr.android.smailer.util.eventTypeText
import com.bopr.android.smailer.util.formatDuration
import com.bopr.android.smailer.util.formatPhoneNumber
import com.bopr.android.smailer.util.getContactName

/**
 * Formats Telegram message from phone event.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class TelegramPhoneEventFormatter(private val context: Context, private val event: PhoneEventData) :
    BaseTelegramEventFormatter(context, event.startTime, event.processTime, event.location) {

    override fun getTitle(): String {
        return string(eventTypeText(event))
    }

    override fun getBody(): String {
        if (!settings.hasTelegramMessageContent(VAL_PREF_MESSAGE_CONTENT_BODY)) return ""

        return when {
            event.isMissed ->
                string(R.string.you_had_missed_call)

            event.isSms ->
                event.text!!

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

    override fun getSenderName(): String {
        if (!settings.hasTelegramMessageContent(VAL_PREF_MESSAGE_CONTENT_CALLER)) return ""

        return string(
            when {
                event.isSms -> R.string.sender_phone
                event.isIncoming -> R.string.caller_phone
                else -> R.string.called_phone
            },
            formatPhoneNumber(event.phone),
            context.getContactName(event.phone)?.let { " ($it)" } ?: ""
        )
    }


}