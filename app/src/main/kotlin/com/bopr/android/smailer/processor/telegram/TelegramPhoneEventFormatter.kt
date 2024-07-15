package com.bopr.android.smailer.processor.telegram

import android.content.Context
import androidx.annotation.StringRes
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.PREF_TELEGRAM_MESSAGE_SHOW_CALLER
import com.bopr.android.smailer.Settings.Companion.PREF_TELEGRAM_MESSAGE_SHOW_DEVICE_NAME
import com.bopr.android.smailer.Settings.Companion.PREF_TELEGRAM_MESSAGE_SHOW_EVENT_TIME
import com.bopr.android.smailer.Settings.Companion.PREF_TELEGRAM_MESSAGE_SHOW_HEADER
import com.bopr.android.smailer.Settings.Companion.PREF_TELEGRAM_MESSAGE_SHOW_LOCATION
import com.bopr.android.smailer.Settings.Companion.PREF_TELEGRAM_MESSAGE_SHOW_PROCESS_TIME
import com.bopr.android.smailer.provider.telephony.PhoneEventData
import com.bopr.android.smailer.util.eventTypeText
import com.bopr.android.smailer.util.formatDuration
import com.bopr.android.smailer.util.httpEncoded
import com.bopr.android.smailer.util.tryGetContactName
import java.text.DateFormat.SHORT
import java.text.DateFormat.getDateTimeInstance

/**
 * Formats Telegram message from phone event.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class TelegramPhoneEventFormatter(private val context: Context, private val event: PhoneEventData) :
    TelegramMessageFormatter(context) {

    private val settings = Settings(context)
    private val timeFormat = getDateTimeInstance(SHORT, SHORT)
    private val newline = "\n".httpEncoded()

    override fun formatMessage(): String {
        return buildString {
            appendHeader()
            appendBody()
            appendFooter()
        }
    }

    private fun StringBuilder.appendHeader() {
        if (!settings.getBoolean(PREF_TELEGRAM_MESSAGE_SHOW_HEADER)) return

        append("<b>")
        append(string(eventTypeText(event)))
        append(getEventTimeText())
        append("</b>")
        append(newline)
        append(newline)
    }

    private fun StringBuilder.appendBody() {
        when {
            event.isMissed ->
                append(string(R.string.you_had_missed_call))

            event.isSms ->
                append(event.text!!)

            else -> {
                val duration = formatDuration(event.callDuration)
                if (event.isIncoming) {
                    append(string(R.string.you_had_incoming_call, duration))
                } else {
                    append(string(R.string.you_had_outgoing_call, duration))
                }
            }
        }
    }

    private fun StringBuilder.appendFooter() {
        append(newline)
        appendCallerText()

        val deviceNameText = getDeviceNameText()
        val processTimeText = getProcessTimeText()
        if (deviceNameText.isNotEmpty() || processTimeText.isNotEmpty()) {
            append(newline)
            append("<i>")
            append(string(R.string.sent_time_device, deviceNameText, processTimeText))
            append("</i>")
        }

        val locationText = getLocationText()
        if (locationText.isNotEmpty()) {
            append(newline)
            append("<i>")
            append(string(R.string.last_known_location, locationText))
            append("</i>")
        }
    }

    private fun StringBuilder.appendCallerText() {
        if (!settings.getBoolean(PREF_TELEGRAM_MESSAGE_SHOW_CALLER)) return

        val patternRes = when {
            event.isSms -> R.string.sender_phone
            event.isIncoming -> R.string.caller_phone
            else -> R.string.called_phone
        }

        val contact = tryGetContactName(context, event.phone) ?: string(R.string.unknown_contact)

        append(newline)
        append("<i>")
        append(string(patternRes, event.phone, contact))
        append("</i>")
    }

    private fun getDeviceNameText(): String {
        return if (settings.getBoolean(PREF_TELEGRAM_MESSAGE_SHOW_DEVICE_NAME)) {
            string(R.string._from_device, settings.getDeviceName())
        } else ""
    }

    private fun getEventTimeText(): String {
        return if (settings.getBoolean(PREF_TELEGRAM_MESSAGE_SHOW_EVENT_TIME)) {
            string(R.string._at_time, timeFormat.format(event.startTime))
        } else ""
    }

    private fun getProcessTimeText(): String {
        return if (settings.getBoolean(PREF_TELEGRAM_MESSAGE_SHOW_PROCESS_TIME)) {
            string(R.string._at_time, timeFormat.format(event.processTime))
        } else ""
    }

    private fun getLocationText(): String {
        return if (settings.getBoolean(PREF_TELEGRAM_MESSAGE_SHOW_LOCATION)) {
            event.location?.run {
                val text = format(degreeSymbol = "&#176;", separator = ", ")
                "<a href=\"" +
                        "https://www.google.com/maps/place/$latitude+$longitude/@$latitude,$longitude" +
                        "\">$text</a>"
            } ?: string(R.string.unknown_location)
        } else ""
    }

    private fun string(@StringRes resId: Int, vararg formatArgs: Any?): String =
        context.getString(resId, *formatArgs)

    private fun string(@StringRes resId: Int) = context.getString(resId)

}