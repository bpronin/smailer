package com.bopr.android.smailer.messenger.mail

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import androidx.annotation.StringRes
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_MESSAGE_CONTENT_CALLER
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_MESSAGE_CONTENT_CONTROL_LINKS
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_MESSAGE_CONTENT_CREATION_TIME
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_MESSAGE_CONTENT_DEVICE_NAME
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_MESSAGE_CONTENT_DISPATCH_TIME
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_MESSAGE_CONTENT_HEADER
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_MESSAGE_CONTENT_LOCATION
import com.bopr.android.smailer.Settings.Companion.settings
import com.bopr.android.smailer.util.GeoLocation
import com.bopr.android.smailer.util.checkPermission
import com.bopr.android.smailer.util.localeResources
import com.bopr.android.smailer.util.parseLocale
import java.text.DateFormat.LONG
import java.text.DateFormat.getDateTimeInstance
import java.util.Date
import java.util.Locale

abstract class BaseMailFormatter(
    protected val context: Context,
    private val eventTime: Long?,
    private val processTime: Long?,
    private val location: GeoLocation?
) : MailFormatter {

    protected val settings = context.settings
    private val locale: Locale = parseLocale(settings.getMessageLocale())
    private val resources = context.localeResources(locale)
    private val timeFormat = getDateTimeInstance(LONG, LONG, locale)

    protected abstract fun getSubject(): String?

    protected abstract fun getTitle(): String?

    protected abstract fun getMessage(): String?

    protected abstract fun getSenderName(): String?

    protected abstract fun getReplyLinks(): List<String>?

    /**
     * Returns formatted email subject.
     *
     * @return email subject
     */
    override fun formatSubject(): String {
        return "[${string(R.string.app_name)}] ${getSubject()}"
    }

    /**
     * Returns formatted email body.
     *
     * @return email body
     */
    override fun formatBody(): String {
        val footer = formatFooter()
        val links = formatReplyLinks()
        return "<!DOCTYPE html>" +
                "<html lang=\"en\">" +
                "<head>" +
                "<title>${string(R.string.app_name)} message</title>" +
                "<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\">" +
                "</head>" +
                "<body $BODY_STYLE>" +
                formatHeader() +
                formatMessage() +
                (if (footer.isNotEmpty()) "$LINE<small $FOOTER_STYLE>$footer</small>" else "") +
                (if (links.isNotEmpty()) "$LINE$links" else "") +
                "</body>" +
                "</html>"
    }

    private fun formatMessage(): String {
        return getMessage().orEmpty()
    }

    private fun formatHeader(): String {
        return if (settings.hasMailContent(VAL_PREF_MESSAGE_CONTENT_HEADER)) {
            return "<strong $HEADER_STYLE>${getTitle()}</strong><br><br>"
        } else ""
    }

    private fun formatFooter(): String {
        val callerText = formatSender()
        val timeText = formatCreationTime()
        val deviceNameText = formatDeviceName()
        val sendTimeText = formatDispatchTime()
        val locationText = formatLocation()

        return buildString {
            append(callerText)

            if (timeText.isNotEmpty()) {
                if (isNotEmpty()) append("<br>")
                append(timeText)
            }

            if (locationText.isNotEmpty()) {
                if (isNotEmpty()) append("<br>")
                append(locationText)
            }

            if (deviceNameText.isNotEmpty() || sendTimeText.isNotEmpty()) {
                if (isNotEmpty()) append("<br>")
                append(string(R.string.sent_time_device, deviceNameText, sendTimeText))
            }
        }
    }

    private fun formatSender(): String {
        return if (settings.hasMailContent(VAL_PREF_MESSAGE_CONTENT_CALLER)) {
            getSenderName().orEmpty()
        } else ""
    }

    private fun formatCreationTime(): String {
        return if (eventTime != null && settings.hasMailContent(
                VAL_PREF_MESSAGE_CONTENT_CREATION_TIME
            )
        ) {
            string(R.string.time_time, timeFormat.format(Date(eventTime)))
        } else ""
    }

    private fun formatDispatchTime(): String {
        return if (processTime != null && settings.hasMailContent(
                VAL_PREF_MESSAGE_CONTENT_DISPATCH_TIME
            )
        ) {
            string(R.string._at_time, timeFormat.format(processTime))
        } else ""
    }

    private fun formatDeviceName(): String {
        return if (settings.hasMailContent(VAL_PREF_MESSAGE_CONTENT_DEVICE_NAME)) {
            string(R.string._from_device, settings.getDeviceName())
        } else ""
    }

    private fun formatLocation(): String {
        if (!settings.hasMailContent(VAL_PREF_MESSAGE_CONTENT_LOCATION)) return ""

        return if (location != null) {
            val lt = location.latitude
            val ln = location.longitude
            val text = location.format(degreeSymbol = "&#176;", separator = ",&nbsp;")
            val link =
                "<a href=\"https://www.google.com/maps/place/$lt+$ln/@$lt,$ln\">$text</a>"
            string(R.string.last_known_location, link)
        } else {
            val text =
                if (context.checkPermission(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION)) {
                    string(R.string.geolocation_disabled)
                } else {
                    string(R.string.no_permission_read_location)
                }
            string(R.string.last_known_location, text)
        }
    }

    private fun formatReplyLinks(): String {
        if (!settings.hasMailContent(VAL_PREF_MESSAGE_CONTENT_CONTROL_LINKS)) return ""

        return getReplyLinks()?.let { links ->
            return buildString {
                append("<ul>")
                for (link in links) {
                    append("<li>")
                    append(link)
                    append("</li>")
                }
                append("</ul>")
            }
        } ?: ""
    }

    protected fun string(@StringRes resId: Int, vararg formatArgs: Any?): String =
        resources.getString(resId, *formatArgs)

    protected fun string(@StringRes resId: Int) = resources.getString(resId)

    protected fun href(link: String, text: String): String {
        return "<a href=\"$link\">$text</a>"
    }

    companion object {

        private const val LINE =
            "<hr style=\"border: none; background-color: #e0e0e0; height: 1px;\">"
        private const val BODY_STYLE =
            "style=\"font-family:'Segoe UI', Tahoma, Verdana, Arial, sans-serif;\""
        private const val HEADER_STYLE = "style=\"color: #707070;\""
        private const val FOOTER_STYLE = "style=\"color: #707070;\""
    }

}