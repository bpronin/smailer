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
import com.bopr.android.smailer.util.GeoLocation
import com.bopr.android.smailer.util.httpEncoded
import java.text.DateFormat
import java.text.DateFormat.SHORT

abstract class BaseTelegramEventFormatter(
    private val context: Context,
    private val eventTime: Long?,
    private val dispatchTime: Long?,
    private val location: GeoLocation?
) :
    TelegramMessageFormatter(context) {

    protected val settings = Settings(context)

    private val newline = "\n".httpEncoded()

    override fun formatMessage(): String {
        return buildString {

            /* header */

            val headerText = getHeaderText()
            val eventTimeText = getEventTimeText()
            headerText?.run {
                append("<b>")
                append(this)
                append("</b>")
                append(newline)
            }
            eventTimeText?.run {
                append("<b>")
                append(this)
                append("</b>")
                append(newline)
            }
            if (headerText != null || eventTimeText != null)
                append(newline)

            /* body */

            getMessage()?.run {
                append(this)
            }

            /* footer */

            val senderText = getSenderText()
            val processTimeText = getDispatchTimeText()
            val deviceNameText = getDeviceNameText()

            if (senderText != null || processTimeText != null || deviceNameText != null)
                append(newline)

            senderText?.run {
                append(newline)
                append("<i>")
                append(this)
                append("</i>")
            }
            if (!deviceNameText.isNullOrEmpty() || !processTimeText.isNullOrEmpty()) {
                append(newline)
                append("<i>")
                append(
                    string(
                        R.string.sent_time_device,
                        deviceNameText.orEmpty(), processTimeText.orEmpty()
                    )
                )
                append("</i>")
            }
            getLocationText()?.run {
                append(newline)
                append("<i>")
                append(string(R.string.last_known_location, this))
                append("</i>")
            }
        }
    }

    protected abstract fun getTitle(): String?

    protected abstract fun getMessage(): String?

    protected abstract fun getSenderName(): String?

    private fun getHeaderText(): String? {
        return if (settings.hasTelegramMessageContent(VAL_PREF_MESSAGE_CONTENT_HEADER))
            getTitle()
        else null
    }

    private fun getDeviceNameText(): String? {
        return if (settings.hasTelegramMessageContent(VAL_PREF_MESSAGE_CONTENT_DEVICE_NAME))
            string(R.string._from_device, settings.getDeviceName())
        else null
    }

    private fun getEventTimeText(): String? {
        return if (eventTime != null &&
            settings.hasTelegramMessageContent(VAL_PREF_MESSAGE_CONTENT_EVENT_TIME)
        )
            DateFormat.getDateTimeInstance(SHORT, SHORT).format(eventTime)
        else null
    }

    private fun getDispatchTimeText(): String? {
        return if (dispatchTime != null &&
            settings.hasTelegramMessageContent(VAL_PREF_MESSAGE_CONTENT_DISPATCH_TIME)
        ) {
            string(
                R.string._on_date_at_time,
                DateFormat.getDateInstance(SHORT).format(dispatchTime),
                DateFormat.getTimeInstance().format(dispatchTime)
            )
        } else null
    }

    private fun getSenderText(): String? {
        return if (settings.hasTelegramMessageContent(VAL_PREF_MESSAGE_CONTENT_CALLER))
            getSenderName()
        else null
    }

    private fun getLocationText(): String? {
        return if (settings.hasTelegramMessageContent(VAL_PREF_MESSAGE_CONTENT_LOCATION))
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
        else null
    }

    protected fun string(@StringRes resId: Int, vararg formatArgs: Any?): String =
        context.getString(resId, *formatArgs)

    protected fun string(@StringRes resId: Int) = context.getString(resId)

}