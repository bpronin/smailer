package com.bopr.android.smailer.processor.telegram

import android.content.Context
import androidx.annotation.StringRes
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_MESSAGE_CONTENT_CALLER
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_MESSAGE_CONTENT_DEVICE_NAME
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_MESSAGE_CONTENT_DISPATCH_TIME
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_MESSAGE_CONTENT_EVENT_TIME
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_MESSAGE_CONTENT_HEADER
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_MESSAGE_CONTENT_LOCATION
import com.bopr.android.smailer.util.GeoCoordinates
import com.bopr.android.smailer.util.httpEncoded
import java.text.DateFormat.SHORT
import java.text.DateFormat.getDateTimeInstance

abstract class BaseTelegramEventFormatter(
    private val context: Context,
    private val eventTime: Long?,
    private val dispatchTime: Long?,
    private val location: GeoCoordinates?
) :
    TelegramMessageFormatter(context) {

    protected val settings = Settings(context)
    private val timeFormat = getDateTimeInstance(SHORT, SHORT)
    private val newline = "\n".httpEncoded()

    override fun formatMessage(): String {
        return buildString {
            appendHeader()
            append(getBodyText())
            appendFooter()
        }
    }

    abstract fun getHeaderText(): String?

    abstract fun getBodyText(): String?

    abstract fun getSenderText(): String?

    private fun getDeviceNameText(): String? {
        return if (settings.hasTelegramMessageContent(VAL_PREF_MESSAGE_CONTENT_DEVICE_NAME)) {
            string(R.string._from_device, settings.getDeviceName())
        } else null
    }

    private fun getEventTimeText(): String? {
        return if (eventTime != null &&
            settings.hasTelegramMessageContent(VAL_PREF_MESSAGE_CONTENT_EVENT_TIME)
        ) {
            string(R.string._at_time, timeFormat.format(eventTime))
        } else null
    }

    private fun getDispatchTimeText(): String? {
        return if (dispatchTime != null &&
            settings.hasTelegramMessageContent(VAL_PREF_MESSAGE_CONTENT_DISPATCH_TIME)
        ) {
            string(R.string._at_time, timeFormat.format(dispatchTime))
        } else null
    }

    private fun getLocationText(): String? {
        return if (settings.hasTelegramMessageContent(VAL_PREF_MESSAGE_CONTENT_LOCATION)) {
            location?.run {
                buildString {
                    append("<a href=\"")
                    append("https://www.google.com/maps/place/")
                    append("$latitude+$longitude")
                    append("/@")
                    append("$latitude,$longitude")
                    append("\">")
                    append(format(degreeSymbol = "&#176;", separator = ", ").httpEncoded())
                    append("</a>")
                }
            } ?: string(R.string.unknown_location)
        } else null
    }

    private fun StringBuilder.appendHeader() {
        if (!settings.hasTelegramMessageContent(VAL_PREF_MESSAGE_CONTENT_HEADER)) return

        append("<b>")
        append(getHeaderText())
        append(getEventTimeText())
        append("</b>")
        append(newline)
        append(newline)
    }

    private fun StringBuilder.appendFooter() {
        append(newline)
        appendSender()

        val deviceNameText = getDeviceNameText()
        val processTimeText = getDispatchTimeText()
        if (deviceNameText != null || processTimeText != null) {
            append(newline)
            append("<i>")
            append(string(R.string.sent_time_device, deviceNameText, processTimeText))
            append("</i>")
        }

        getLocationText()?.run {
            append(newline)
            append("<i>")
            append(string(R.string.last_known_location, this))
            append("</i>")
        }
    }

    private fun StringBuilder.appendSender() {
        if (!settings.hasTelegramMessageContent(VAL_PREF_MESSAGE_CONTENT_CALLER)) return

        getSenderText()?.run {
            append(newline)
            append("<i>")
            append(this)
            append("</i>")
        }
    }

    protected fun string(@StringRes resId: Int, vararg formatArgs: Any?): String =
        context.getString(resId, *formatArgs)

    protected fun string(@StringRes resId: Int) = context.getString(resId)

}