package com.bopr.android.smailer

import android.Manifest.permission
import android.content.Context
import android.content.res.Resources
import android.text.TextUtils.htmlEncode
import androidx.annotation.StringRes
import com.bopr.android.smailer.GeoLocator.Companion.isLocationPermissionsGranted
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_EMAIL_CONTENT_CONTACT
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_EMAIL_CONTENT_DEVICE_NAME
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_EMAIL_CONTENT_HEADER
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_EMAIL_CONTENT_LOCATION
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME_SENT
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_EMAIL_CONTENT_REMOTE_COMMAND_LINKS
import com.bopr.android.smailer.util.AddressUtil.escapePhone
import com.bopr.android.smailer.util.AndroidUtil.checkPermission
import com.bopr.android.smailer.util.TextUtil.formatDuration
import com.bopr.android.smailer.util.UiUtil.eventTypePrefix
import com.bopr.android.smailer.util.UiUtil.eventTypeText
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.text.DateFormat
import java.util.*
import java.util.regex.Pattern

/**
 * Formats email subject and body.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class MailFormatter(private val context: Context, private val event: PhoneEvent) {

    var contactName: String? = null
    var deviceName: String? = null
    var sendTime: Date? = null
    var serviceAccount: String? = null
    var options: Set<String> = setOf()
    var locale: Locale = updateLocale(Locale.getDefault())
        set(value) {
            field = updateLocale(value)
        }
    private lateinit var resources: Resources
    private lateinit var timeFormat: DateFormat

    /**
     * Returns formatted email subject.
     *
     * @return email subject
     */
    fun formatSubject(): String {
        return "[${getString(R.string.app_name)}] ${getString(eventTypePrefix(event))} ${escapePhone(event.phone)}"
    }

    /**
     * Returns formatted email body.
     *
     * @return email body
     */
    fun formatBody(): String {
        val footer = formatFooter()
        val links = formatReplyLinks()
        return "<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\">" +
                "</head><body>" +
                formatHeader() +
                formatMessage() +
                (if (footer.isNotEmpty()) "$LINE<small>$footer</small>" else "") +
                (if (links.isNotEmpty()) "$LINE$links" else "") +
                "</body></html>"
    }

    private fun formatMessage(): String {
        return when {
            event.isMissed -> {
                getString(R.string.you_had_missed_call)
            }
            event.isSms -> {
                replaceUrlsWithLinks(event.text!!)
            }
            else -> {
                val duration = formatDuration(event.callDuration)
                if (event.isIncoming) {
                    getString(R.string.you_had_incoming_call, duration)
                } else {
                    getString(R.string.you_had_outgoing_call, duration)
                }
            }
        }
    }

    private fun formatHeader(): String {
        return if (options.contains(VAL_PREF_EMAIL_CONTENT_HEADER)) {
            return "<strong>${eventTypeText(event)}</strong><br><br>"
        } else ""
    }

    private fun formatFooter(): String {
        val callerText = formatCaller()
        val timeText = formatEventTime()
        val deviceNameText = formatDeviceName()
        val sendTimeText = formatSendTime()
        val locationText = formatLocation()

        val sb = StringBuilder()

        sb.append(callerText)

        if (timeText.isNotEmpty()) {
            if (sb.isNotEmpty()) sb.append("<br>")
            sb.append(timeText)
        }

        if (locationText.isNotEmpty()) {
            if (sb.isNotEmpty()) sb.append("<br>")
            sb.append(locationText)
        }

        if (deviceNameText.isNotEmpty() || sendTimeText.isNotEmpty()) {
            if (sb.isNotEmpty()) sb.append("<br>")
            sb.append(getString(R.string.sent_time_device, deviceNameText, sendTimeText))
        }

        return sb.toString()
    }

    private fun formatCaller(): String {
        if (options.contains(VAL_PREF_EMAIL_CONTENT_CONTACT)) {
            val phoneUrl = encodeUrl(event.phone)
            val phoneLink = "<a href=\"tel:$phoneUrl\" style=\"text-decoration: none\">&#9742;</a>${event.phone}"

            val contact = if (contactName.isNullOrEmpty()) {
                if (checkPermission(context, permission.READ_CONTACTS)) {
                    "<a href=\"https://www.google.com/search?q=$phoneUrl\">" +
                            "${getString(R.string.unknown_contact)}</a>"
                } else {
                    getString(R.string.contact_no_permission_read_contact)
                }
            } else {
                contactName
            }

            val patternRes = when {
                event.isSms ->
                    R.string.sender_phone
                event.isIncoming ->
                    R.string.caller_phone
                else ->
                    R.string.called_phone
            }
            return getString(patternRes, phoneLink, contact)
        }
        return ""
    }

    private fun formatEventTime(): String {
        return if (options.contains(VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME)) {
            getString(R.string.time_time, timeFormat.format(Date(event.startTime)))
        } else ""
    }

    private fun formatSendTime(): String {
        return if (options.contains(VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME_SENT) && sendTime != null) {
            getString(R.string._at_time, timeFormat.format(sendTime!!))
        } else ""
    }

    private fun formatDeviceName(): String {
        return if (options.contains(VAL_PREF_EMAIL_CONTENT_DEVICE_NAME) && !deviceName.isNullOrEmpty()) {
            getString(R.string._from_device, deviceName)
        } else ""
    }

    private fun formatLocation(): String {
        return if (options.contains(VAL_PREF_EMAIL_CONTENT_LOCATION)) {
            val coordinates = event.location
            return if (coordinates != null) {
                val lt = coordinates.latitude
                val ln = coordinates.longitude
                val text = coordinates.format(degreeSymbol = "&#176;")
                val link = "<a href=\"https://www.google.com/maps/place/$lt+$ln/@$lt,$ln\">$text</a>"
                getString(R.string.last_known_location, link)
            } else {
                val text = if (isLocationPermissionsGranted(context)) {
                    getString(R.string.geolocation_disabled)
                } else {
                    getString(R.string.no_permission_read_location)
                }
                getString(R.string.last_known_location, text)
            }
        } else ""
    }

    private fun formatReplyLinks(): String {
        return if (options.contains(VAL_PREF_EMAIL_CONTENT_REMOTE_COMMAND_LINKS)
                && !serviceAccount.isNullOrEmpty()) {
            //val title = getString(R.string.reply_ot_app, R.string.app_name)

            val phone = escapePhone(event.phone)
            val smsText = event.text ?: ""
            val subject = formatSubject()

            "<ul>${formatReplyLink(R.string.add_phone_to_blacklist, "add phone $phone to blacklist", subject) +
                    formatReplyLink(R.string.add_text_to_blacklist, "add text \"$smsText\" to blacklist", subject) +
                    formatReplyLink(R.string.send_sms_to_sender, "send SMS message \"Sample text\" to $phone", subject)}</ul>"
        } else ""
    }

    private fun formatReplyLink(@StringRes titleRes: Int, body: String, subject: String): String {
        return "<li><small>" +
                "<a href=\"" +
                "mailto:$serviceAccount?" +
                "subject=${htmlEncode("Re: $subject")}&amp;" +
                "body=${htmlEncode("To device \"$deviceName\": %0d%0a $body")}" +
                "\">${getString(titleRes)}</a>" +
                "</small></li>"
    }

    private fun updateLocale(locale: Locale): Locale {
        resources = if (locale == Locale.getDefault()) {
            context.resources
        } else {
            val configuration = context.resources.configuration
            configuration.setLocale(locale)
            context.createConfigurationContext(configuration).resources
        }
        timeFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale)
        return locale
    }

    private fun replaceUrlsWithLinks(s: String): String {
        val sb = StringBuffer()

        val matcher = WEB_URL_PATTERN.matcher(s)
        while (matcher.find()) {
            val url = matcher.group()
            matcher.appendReplacement(sb, "<a href=\"$url\">$url</a>")
        }
        matcher.appendTail(sb)

        return sb.toString()
    }

    private fun encodeUrl(s: String): String {
        return try {
            URLEncoder.encode(s, "UTF-8")
        } catch (x: UnsupportedEncodingException) {
            throw RuntimeException(x)
        }
    }

    private fun getString(@StringRes resId: Int, vararg formatArgs: Any?): String {
        return resources.getString(resId, *formatArgs)
    }

    private fun getString(@StringRes resId: Int) = resources.getString(resId)

    companion object {

        private val WEB_URL_PATTERN = Pattern.compile("(?:\\S+)://\\S+")
        private const val LINE = "<hr style=\"border: none; background-color: #cccccc; height: 1px;\">"
    }

}