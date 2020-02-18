package com.bopr.android.smailer

import android.content.Context
import android.content.res.Resources
import android.text.TextUtils.htmlEncode
import androidx.annotation.StringRes
import com.bopr.android.smailer.GeoLocator.Companion.isPermissionsDenied
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_EMAIL_CONTENT_CONTACT
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_EMAIL_CONTENT_DEVICE_NAME
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_EMAIL_CONTENT_HEADER
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_EMAIL_CONTENT_LOCATION
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME_SENT
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_EMAIL_CONTENT_REMOTE_COMMAND_LINKS
import com.bopr.android.smailer.util.AddressUtil.escapePhone
import com.bopr.android.smailer.util.ContentUtils.isReadContactsPermissionsDenied
import com.bopr.android.smailer.util.TagFormatter
import com.bopr.android.smailer.util.TextUtil.formatDuration
import com.bopr.android.smailer.util.TextUtil.isNotEmpty
import com.bopr.android.smailer.util.TextUtil.isNullOrBlank
import com.bopr.android.smailer.util.Util.requireNonNull
import com.bopr.android.smailer.util.ui.UiUtil.eventTypePrefix
import com.bopr.android.smailer.util.ui.UiUtil.eventTypeText
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

    private var contactName: String? = null
    private var deviceName: String? = null
    private var sendTime: Date? = null
    private var serviceAccount: String? = null
    private var contentOptions: Set<String> = setOf()
    private lateinit var resources: Resources
    private lateinit var dateTimeFormat: DateFormat
    private lateinit var formatter: TagFormatter

    init {
        setLocale(Locale.getDefault())
    }

    /**
     * Sets mail locale.
     */
    fun setLocale(locale: Locale) {
        resources = if (locale === Locale.getDefault()) {
            context.resources
        } else {
            val configuration = context.resources.configuration
            configuration.setLocale(locale)
            context.createConfigurationContext(configuration).resources
        }
        formatter = TagFormatter(resources)
        dateTimeFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale)
    }

    /**
     * Sets email content options.
     *
     * @param contentOptions set of options
     */
    fun setContentOptions(contentOptions: Set<String>) {
        this.contentOptions = contentOptions
    }

    /**
     * Sets contact name to be used in email body.
     *
     * @param contactName name
     */
    fun setContactName(contactName: String?) {
        this.contactName = contactName
    }

    /**
     * Sets device name to be used in email body.
     *
     * @param deviceName name
     */
    fun setDeviceName(deviceName: String?) {
        this.deviceName = deviceName
    }

    /**
     * Sets email send time
     *
     * @param sendTime time
     */
    fun setSendTime(sendTime: Date?) {
        this.sendTime = sendTime
    }

    /**
     * Sets service account email address
     *
     * @param serviceAddress address
     */
    fun setServiceAccount(serviceAddress: String?) {
        serviceAccount = serviceAddress
    }

    /**
     * Returns formatted email subject.
     *
     * @return email subject
     */
    fun formatSubject(): String {
        return formatter
                .pattern(SUBJECT_PATTERN)
                .put("app_name", R.string.app_name)
                .put("source", eventTypePrefix(event))
                .put("phone", escapePhone(event.phone))
                .format()
    }

    /**
     * Returns formatted email body.
     *
     * @return email body
     */
    fun formatBody(): String {
        val footer = formatFooter()
        val links = formatRemoteControlLinks()
        return formatter
                .pattern(BODY_PATTERN)
                .put("header", formatHeader())
                .put("message", formatMessage())
                .put("footer_line", if (isNotEmpty(footer)) LINE else "")
                .put("footer", if (isNotEmpty(footer)) "<small>$footer</small>" else "")
                .put("remote_line", if (isNotEmpty(links)) LINE else "")
                .put("remote_links", links)
                .format()
    }

    private fun formatMessage(): String {
        return when {
            event.isMissed -> {
                resources.getString(R.string.you_had_missed_call)
            }
            event.isSms -> {
                replaceUrls(requireNonNull(event.text))
            }
            else -> {
                val patternRes = if (event.isIncoming) {
                    R.string.you_had_incoming_call
                } else {
                    R.string.you_had_outgoing_call
                }
                formatter
                        .pattern(patternRes)
                        .put("duration", formatDuration(event.callDuration))
                        .format()
            }
        }
    }

    private fun formatHeader(): String {
        return if (contentOptions.contains(VAL_PREF_EMAIL_CONTENT_HEADER)) {
            formatter
                    .pattern(HEADER_PATTERN)
                    .put("header", eventTypeText(event))
                    .format()
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

        if (isNotEmpty(timeText)) {
            if (isNotEmpty(sb)) {
                sb.append("<br>")
            }
            sb.append(timeText)
        }

        if (isNotEmpty(locationText)) {
            if (isNotEmpty(sb)) {
                sb.append("<br>")
            }
            sb.append(locationText)
        }

        if (isNotEmpty(deviceNameText) || isNotEmpty(sendTimeText)) {
            if (isNotEmpty(sb)) {
                sb.append("<br>")
            }
            sb.append(formatter
                    .pattern(R.string.sent_time_device)
                    .put("device_name", deviceNameText)
                    .put("time", sendTimeText))
        }

        return sb.toString()
    }

    private fun formatCaller(): String {
        if (contentOptions.contains(VAL_PREF_EMAIL_CONTENT_CONTACT)) {
            val patternRes = if (event.isSms) {
                R.string.sender_phone
            } else {
                if (event.isIncoming) {
                    R.string.caller_phone
                } else {
                    R.string.called_phone
                }
            }

            val phoneQuery = encodeUrl(event.phone)
            var name = contactName
            if (isNullOrBlank(name)) {
                name = if (isReadContactsPermissionsDenied(context)) {
                    resources.getString(R.string.contact_no_permission_read_contact)
                } else {
                    formatter
                            .pattern(GOOGLE_SEARCH_PATTERN)
                            .put("query", phoneQuery)
                            .put("text", R.string.unknown_contact)
                            .format()
                }
            }

            return formatter
                    .pattern(patternRes)
                    .put("phone", formatter
                            .pattern(PHONE_LINK_PATTERN)
                            .put("phone", phoneQuery)
                            .put("text", event.phone)
                            .format())
                    .put("name", name)
                    .format()
        }
        return ""
    }

    private fun formatEventTime(): String {
        return if (contentOptions.contains(VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME)) {
            formatter
                    .pattern(R.string.time_time)
                    .put("time", dateTimeFormat.format(Date(event.startTime)))
                    .format()
        } else ""
    }

    private fun formatSendTime(): String {
        return if (contentOptions.contains(VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME_SENT) && sendTime != null) {
            " " + formatter
                    .pattern(R.string._at_time)
                    .put("time", dateTimeFormat.format(sendTime!!))
                    .format()
        } else ""
    }

    private fun formatDeviceName(): String {
        return if (contentOptions.contains(VAL_PREF_EMAIL_CONTENT_DEVICE_NAME) && !isNullOrBlank(deviceName)) {
            " " + formatter
                    .pattern(R.string._from_device)
                    .put("device_name", deviceName)
                    .format()
        } else ""
    }

    private fun formatLocation(): String {
        if (contentOptions.contains(VAL_PREF_EMAIL_CONTENT_LOCATION)) {
            val coordinates = event.location
            return if (coordinates != null) {
                formatter
                        .pattern(R.string.last_known_location)
                        .put("location", formatter
                                .pattern(GOOGLE_MAP_LINK_PATTERN)
                                .put("latitude", coordinates.latitude.toString())
                                .put("longitude", coordinates.longitude.toString())
                                .put("location", coordinates.format(degreeSymbol = "&#176;"))
                                .format())
                        .format()
            } else {
                val locationText = if (isPermissionsDenied(context)) {
                    R.string.no_permission_read_location
                } else {
                    R.string.geolocation_disabled
                }
                formatter
                        .pattern(R.string.last_known_location)
                        .put("location", locationText)
                        .format()
            }
        }
        return ""
    }

    private fun formatRemoteControlLinks(): String {
        return if (contentOptions.contains(VAL_PREF_EMAIL_CONTENT_REMOTE_COMMAND_LINKS)
                && !isNullOrBlank(serviceAccount)) {
            formatter
                    .pattern(REPLY_LINKS_PATTERN)
                    .put("title", formatter
                            .pattern(R.string.reply_ot_app)
                            .put("app_name", R.string.app_name))
                    .put("links", formatRemoteControlLinksList())
                    .format()
        } else ""
    }

    private fun formatRemoteControlLinksList(): String {
        val phone = escapePhone(event.phone)
        val text = event.text

        val phoneTask = formatRemoteTaskBody(R.string.add_phone_to_blacklist_reply_body, phone)
        val sentTask = formatSendSmsRemoteTaskBody(phone)
        val textTask = text?.let { formatRemoteTaskBody(R.string.add_text_to_blacklist_reply_body, text) }
                ?: ""

        return formatRemoteControlLink(R.string.add_phone_to_blacklist, phoneTask) +
                formatRemoteControlLink(R.string.add_text_to_blacklist, textTask) +
                formatRemoteControlLink(R.string.send_sms_to_sender, sentTask)
    }

    private fun formatRemoteControlLink(@StringRes titleRes: Int, body: String): String {
        return formatter
                .pattern(REMOTE_CONTROL_LINK_PATTERN)
                .put("address", serviceAccount)
                .put("subject", htmlEncode("Re: " + formatSubject()))
                .put("body", htmlEncode(formatServiceMailBody(body)))
                .put("link_title", titleRes)
                .format()
    }

    private fun formatRemoteTaskBody(@StringRes patternRes: Int, argument: String): String {
        return formatter
                .pattern(patternRes)
                .put("argument", argument)
                .format()
    }

    private fun formatSendSmsRemoteTaskBody(phone: String): String {
        return formatter
                .pattern(R.string.send_sms_to_sender_reply_body)
                .put("sms_text", "Sample text")
                .put("phone", phone)
                .format()
    }

    private fun formatServiceMailBody(task: String): String {
        return formatter
                .pattern("To device \"{device}\": %0d%0a {task}")
                .put("device", deviceName)
                .put("task", task)
                .format()
    }

    private fun replaceUrls(s: String): String {
        val sb = StringBuffer()

        val matcher = Pattern.compile("((?i:http|https|rtsp|ftp|file)://[\\S]+)").matcher(s)
        while (matcher.find()) {
            val url = matcher.group(1)
            matcher.appendReplacement(sb, "<a href=\"$url\">$url</a>")
        }
        matcher.appendTail(sb)

        return sb.toString()
    }

    private fun encodeUrl(text: String): String {
        return try {
            URLEncoder.encode(text, "UTF-8")
        } catch (x: UnsupportedEncodingException) {
            throw RuntimeException(x)
        }
    }

    companion object {

        private const val SUBJECT_PATTERN = "[{app_name}] {source} {phone}"
        private const val BODY_PATTERN = "<html><head><meta http-equiv=\"content-type\" " +
                "content=\"text/html; charset=utf-8\">" +
                "</head><body>{header}{message}{footer_line}{footer}{remote_line}{remote_links}</body></html>"
        private const val LINE = "<hr style=\"border: none; background-color: #cccccc; height: 1px;\">"
        private const val HEADER_PATTERN = "<strong>{header}</strong><br><br>"
        private const val GOOGLE_MAP_LINK_PATTERN = "<a href=\"https://www.google.com/maps/" +
                "place/{latitude}+{longitude}/@{latitude},{longitude}\">{location}</a>"
        private const val PHONE_LINK_PATTERN = "<a href=\"tel:{phone}\" style=\"text-decoration: " +
                "none\">&#9742;</a>{text}"
        private const val REPLY_LINKS_PATTERN = "<ul>{links}</ul>"
        private const val MAIL_TO_PATTERN = "<a href=\"mailto:{address}?subject={subject}&amp;" +
                "body={body}\">{link_title}</a>"
        private const val REMOTE_CONTROL_LINK_PATTERN = "<li><small>$MAIL_TO_PATTERN</small></li>"
        private const val GOOGLE_SEARCH_PATTERN = "<a href=\"https://www.google.com/search?q={query}\">{text}</a>"
    }

}