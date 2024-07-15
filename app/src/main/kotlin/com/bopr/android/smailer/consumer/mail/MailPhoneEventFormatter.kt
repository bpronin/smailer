package com.bopr.android.smailer.consumer.mail

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.READ_CONTACTS
import android.content.Context
import android.text.TextUtils.htmlEncode
import androidx.annotation.StringRes
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings.Companion.DEFAULT_PHONE_SEARCH_URL
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_EMAIL_CONTENT_CONTACT
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_EMAIL_CONTENT_DEVICE_NAME
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_EMAIL_CONTENT_HEADER
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_EMAIL_CONTENT_LOCATION
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME_SENT
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_EMAIL_CONTENT_REMOTE_COMMAND_LINKS
import com.bopr.android.smailer.provider.telephony.PhoneEventInfo
import com.bopr.android.smailer.util.checkPermission
import com.bopr.android.smailer.util.escapePhone
import com.bopr.android.smailer.util.eventTypePrefix
import com.bopr.android.smailer.util.eventTypeText
import com.bopr.android.smailer.util.formatDuration
import com.bopr.android.smailer.util.htmlReplaceUrlsWithLinks
import com.bopr.android.smailer.util.httpEncoded
import com.bopr.android.smailer.util.localeResources
import com.bopr.android.smailer.util.normalizePhone
import org.slf4j.LoggerFactory
import java.text.DateFormat.LONG
import java.text.DateFormat.getDateTimeInstance
import java.util.Date
import java.util.Locale

/**
 * Formats email subject and body from phone event.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class MailPhoneEventFormatter(
    private val context: Context,
    private val event: PhoneEventInfo,
    private val contactName: String? = null,
    private val deviceName: String? = null,
    private val serviceAccount: String? = null,
    private val options: Set<String> = setOf(),
    private val phoneSearchUrl: String = DEFAULT_PHONE_SEARCH_URL,
    locale: Locale = Locale.getDefault()
) : MailFormatter(context) {

    private val resources = context.localeResources(locale)
    private val timeFormat = getDateTimeInstance(LONG, LONG, locale)

    /**
     * Returns formatted email subject.
     *
     * @return email subject
     */
    override fun formatSubject(): String {
        return "[${string(R.string.app_name)}] ${string(eventTypePrefix(event))} ${escapePhone(event.phone)}"
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
        return when {
            event.isMissed -> {
                string(R.string.you_had_missed_call)
            }

            event.isSms -> {
                event.text!!.htmlReplaceUrlsWithLinks()
            }

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

    private fun formatHeader(): String {
        return if (options.contains(VAL_PREF_EMAIL_CONTENT_HEADER)) {
            return "<strong $HEADER_STYLE>${string(eventTypeText(event))}</strong><br><br>"
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
            sb.append(string(R.string.sent_time_device, deviceNameText, sendTimeText))
        }

        return sb.toString()
    }

    private fun formatCaller(): String {
        if (options.contains(VAL_PREF_EMAIL_CONTENT_CONTACT)) {
            val telLink = "<a href=\"tel:${event.phone.httpEncoded()}\"" +
                    " style=\"text-decoration: none;\">&#9742;</a>${event.phone}"

            val contact = if (contactName.isNullOrEmpty()) {
                if (context.checkPermission(READ_CONTACTS)) {
                    val url = phoneSearchUrl.replace(PHONE_SEARCH_TAG, normalizePhone(event.phone))
                    "<a href=\"$url\">${string(R.string.unknown_contact)}</a>"
                } else {
                    log.warn("No permission to read contacts")
                    string(R.string.unknown_contact)
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
            return string(patternRes, telLink, contact)
        }
        return ""
    }

    private fun formatEventTime(): String {
        return if (options.contains(VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME)) {
            string(R.string.time_time, timeFormat.format(Date(event.startTime)))
        } else ""
    }

    private fun formatSendTime(): String {
        return if (options.contains(VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME_SENT) && event.processTime != null) {
            string(R.string._at_time, timeFormat.format(event.processTime))
        } else ""
    }

    private fun formatDeviceName(): String {
        return if (options.contains(VAL_PREF_EMAIL_CONTENT_DEVICE_NAME) && !deviceName.isNullOrEmpty()) {
            string(R.string._from_device, deviceName)
        } else ""
    }

    private fun formatLocation(): String {
        return if (options.contains(VAL_PREF_EMAIL_CONTENT_LOCATION)) {
            val coordinates = event.location
            return if (coordinates != null) {
                val lt = coordinates.latitude
                val ln = coordinates.longitude
                val text = coordinates.format(degreeSymbol = "&#176;", separator = ",&nbsp;")
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
        } else ""
    }

    private fun formatReplyLinks(): String {
        return if (options.contains(VAL_PREF_EMAIL_CONTENT_REMOTE_COMMAND_LINKS)
            && !serviceAccount.isNullOrEmpty()
        ) {

            val phone = escapePhone(event.phone)
            val smsText = event.text ?: ""
            val subject = formatSubject()

            "<ul>${
                formatReplyLink(
                    R.string.add_phone_to_blacklist,
                    "add phone $phone to blacklist",
                    subject
                ) +
                        formatReplyLink(
                            R.string.add_text_to_blacklist,
                            "add text \"$smsText\" to blacklist",
                            subject
                        ) +
                        formatReplyLink(
                            R.string.send_sms_to_sender,
                            "send SMS message \"Sample text\" to $phone",
                            subject
                        )
            }</ul>"
        } else ""
    }

    private fun formatReplyLink(@StringRes titleRes: Int, body: String, subject: String): String {
        /* Important! There should not be line breaks in href values. Note: Kotlin's """ """ adds line breaks */
        return "<li>" +
                "<a href=\"mailto:$serviceAccount?subject=${htmlEncode("Re: $subject")}&amp;" +
                "body=${htmlEncode("To device \"$deviceName\": %0d%0a $body")}\">" +
                "<small>${string(titleRes)}</small>" +
                "</a>" +
                "</li>"
    }

    private fun string(@StringRes resId: Int, vararg formatArgs: Any?): String =
        resources.getString(resId, *formatArgs)

    private fun string(@StringRes resId: Int) = resources.getString(resId)

    companion object {

        private val log = LoggerFactory.getLogger("MailFormatter")

        const val PHONE_SEARCH_TAG = "{phone}"
        private const val LINE =
            "<hr style=\"border: none; background-color: #e0e0e0; height: 1px;\">"
        private const val BODY_STYLE =
            "style=\"font-family:'Segoe UI', Tahoma, Verdana, Arial, sans-serif;\""
        private const val HEADER_STYLE = "style=\"color: #707070;\""
        private const val FOOTER_STYLE = "style=\"color: #707070;\""
    }

}