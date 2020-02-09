package com.bopr.android.smailer;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bopr.android.smailer.util.TagFormatter;
import com.bopr.android.smailer.util.Util;
import com.google.common.collect.ImmutableSet;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.text.TextUtils.htmlEncode;
import static com.bopr.android.smailer.Settings.VAL_PREF_EMAIL_CONTENT_CONTACT;
import static com.bopr.android.smailer.Settings.VAL_PREF_EMAIL_CONTENT_DEVICE_NAME;
import static com.bopr.android.smailer.Settings.VAL_PREF_EMAIL_CONTENT_HEADER;
import static com.bopr.android.smailer.Settings.VAL_PREF_EMAIL_CONTENT_LOCATION;
import static com.bopr.android.smailer.Settings.VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME;
import static com.bopr.android.smailer.Settings.VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME_SENT;
import static com.bopr.android.smailer.Settings.VAL_PREF_EMAIL_CONTENT_REMOTE_COMMAND_LINKS;
import static com.bopr.android.smailer.util.AddressUtil.escapePhone;
import static com.bopr.android.smailer.util.ContentUtils.isReadContactsPermissionsDenied;
import static com.bopr.android.smailer.util.ResourceUtil.eventTypeText;
import static com.bopr.android.smailer.util.Util.formatDuration;
import static com.bopr.android.smailer.util.Util.isEmpty;
import static java.lang.String.valueOf;

/**
 * ResourceUtil email subject and body.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
class MailFormatter {

    private static final String SUBJECT_PATTERN = "[{app_name}] {source} {phone}";
    private static final String BODY_PATTERN = "<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\">" +
            "</head><body>{header}{message}{footer_line}{footer}{remote_line}{remote_links}</body></html>";
    private static final String LINE = "<hr style=\"border: none; background-color: #cccccc; height: 1px;\">";
    private static final String HEADER_PATTERN = "<strong>{header}</strong><br><br>";
    private static final String GOOGLE_MAP_LINK_PATTERN = "<a href=\"https://www.google.com/maps/" +
            "place/{latitude}+{longitude}/@{latitude},{longitude}\">{location}</a>";
    private static final String PHONE_LINK_PATTERN = "<a href=\"tel:{phone}\" style=\"text-decoration: none\">&#9742;</a>{text}";
//    private static final String REPLY_LINKS_PATTERN = "<br><small><strong>{title}</strong></small><ul>{links}</ul>";
    private static final String REPLY_LINKS_PATTERN = "<ul>{links}</ul>";
    private static final String MAIL_TO_PATTERN = "<a href=\"mailto:{address}?subject={subject}&amp;body={body}\">{link_title}</a>";
    private static final String REMOTE_CONTROL_LINK_PATTERN = "<li><small>" + MAIL_TO_PATTERN + "</small></li>";
    private static final String GOOGLE_SEARCH_PATTERN = "<a href=\"https://www.google.com/search?q={query}\">{text}</a>";

    private final PhoneEvent event;
    private final Context context;
    private Resources resources;
    private Locale locale;
    private String contactName;
    private String deviceName;
    private Set<String> contentOptions;
    private Date sendTime;
    private String serviceAccount;
    private TagFormatter formatter;

    MailFormatter(Context context, PhoneEvent event) {
        this.event = event;
        this.context = context;
        locale = Locale.getDefault();
        resources = context.getResources();
        formatter = new TagFormatter(resources);
        contentOptions = ImmutableSet.of();
    }

    /**
     * Sets contact name to be used in email body.
     *
     * @param contactName name
     */
    void setContactName(String contactName) {
        this.contactName = contactName;
    }

    /**
     * Sets device name to be used in email body.
     *
     * @param deviceName name
     */
    void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    /**
     * Sets email content options.
     *
     * @param contentOptions set of options
     */
    void setContentOptions(Set<String> contentOptions) {
        this.contentOptions = contentOptions;
    }

    /**
     * Sets custom mail locale.
     *
     * @param code locale code as "en_EN"
     */
    void setLocale(@Nullable String code) {
        Locale locale = Util.stringToLocale(code);
        this.locale = locale != null ? locale : Locale.getDefault();

        updateResources();
    }

    /**
     * Sets email send time
     *
     * @param sendTime time
     */
    void setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }

    /**
     * Sets service account email address
     *
     * @param serviceAddress address
     */
    void setServiceAccount(String serviceAddress) {
        this.serviceAccount = serviceAddress;
    }

    /**
     * Returns formatted email subject.
     *
     * @return email subject
     */
    @NonNull
    String formatSubject() {
        return formatter.pattern(SUBJECT_PATTERN)
                .put("app_name", R.string.app_name)
                .put("source", formatTrigger())
                .put("phone", escapePhone(event.getPhone()))
                .format();
    }

    /**
     * Returns formatted email body.
     *
     * @return email body
     */
    @NonNull
    String formatBody() {
        String footer = formatFooter();
        String remoteLinks = formatRemoteControlLinks();

        return formatter
                .pattern(BODY_PATTERN)
                .put("header", formatHeader())
                .put("message", formatMessage())
                .put("footer_line", !isEmpty(footer) ? LINE : null)
                .put("footer", footer)
                .put("remote_line", !isEmpty(remoteLinks) ? LINE : null)
                .put("remote_links", remoteLinks)
                .format();
    }

    private int formatTrigger() {
        int resourceId;

        if (event.isMissed()) {
            resourceId = R.string.missed_call_from;
        } else if (event.isSms()) {
            if (event.isIncoming()) {
                resourceId = R.string.incoming_sms_from;
            } else {
                resourceId = R.string.outgoing_sms_to;
            }
        } else {
            if (event.isIncoming()) {
                resourceId = R.string.incoming_call_from;
            } else {
                resourceId = R.string.outgoing_call_to;
            }
        }

        return resourceId;
    }

    @NonNull
    private String formatMessage() {
        if (event.isMissed()) {
            return resources.getString(R.string.you_had_missed_call);
        } else if (event.isSms()) {
            return replaceUrls(event.getText());
        } else {
            int pattern;
            if (event.isIncoming()) {
                pattern = R.string.you_had_incoming_call;
            } else {
                pattern = R.string.you_had_outgoing_call;
            }
            return formatter
                    .pattern(pattern)
                    .put("duration", formatDuration(event.getCallDuration()))
                    .format();
        }
    }

    @Nullable
    private String formatHeader() {
        if (contentOptions != null && contentOptions.contains(VAL_PREF_EMAIL_CONTENT_HEADER)) {
            return formatter
                    .pattern(HEADER_PATTERN)
                    .put("header", eventTypeText(event))
                    .format();
        }
        return null;
    }

    @Nullable
    private String formatFooter() {
        if (contentOptions != null) {
            String callerText = contentOptions.contains(VAL_PREF_EMAIL_CONTENT_CONTACT) ? formatCaller() : null;
            String timeText = contentOptions.contains(VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME) ? formatEventTime() : null;
            String deviceNameText = contentOptions.contains(VAL_PREF_EMAIL_CONTENT_DEVICE_NAME) ? formatDeviceName() : null;
            String sendTimeText = contentOptions.contains(VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME_SENT) ? formatSendTime() : null;
            String locationText = contentOptions.contains(VAL_PREF_EMAIL_CONTENT_LOCATION) ? formatLocation() : null;

            StringBuilder text = new StringBuilder();

            if (!isEmpty(callerText)) {
                text.append(callerText);
            }

            if (!isEmpty(timeText)) {
                if (!isEmpty(text)) {
                    text.append("<br>");
                }
                text.append(timeText);
            }

            if (!isEmpty(locationText)) {
                if (!isEmpty(text)) {
                    text.append("<br>");
                }
                text.append(locationText);
            }

            if (!isEmpty(deviceNameText) || !isEmpty(sendTimeText)) {
                if (!isEmpty(text)) {
                    text.append("<br>");
                }

                text.append(formatter.pattern(R.string.sent_time_device)
                        .put("device_name", deviceNameText)
                        .put("time", sendTimeText));
            }

            if (!isEmpty(text)) {
                text.insert(0, "<small>");
                text.append("</small>");
            }

            return text.toString();
        }
        return null;
    }

    @NonNull
    private String formatCaller() {
        int resourceId;
        if (event.isSms()) {
            resourceId = R.string.sender_phone;
        } else {
            if (event.isIncoming()) {
                resourceId = R.string.caller_phone;
            } else {
                resourceId = R.string.called_phone;
            }
        }

        String name = this.contactName;
        String phoneQuery = encodeUrl(event.getPhone());
        if (isEmpty(name)) {
            if (isReadContactsPermissionsDenied(context)) { /* base context here */
                name = resources.getString(R.string.contact_no_permission_read_contact);
            } else {
                name = formatter.pattern(GOOGLE_SEARCH_PATTERN)
                        .put("query", phoneQuery)
                        .put("text", R.string.unknown_contact)
                        .format();
            }
        }

        return formatter.pattern(resourceId)
                .put("phone", formatter.pattern(PHONE_LINK_PATTERN)
                        .put("phone", phoneQuery)
                        .put("text", event.getPhone())
                        .format())
                .put("name", name)
                .format();
    }

    @Nullable
    private String formatDeviceName() {
        if (!isEmpty(deviceName)) {
            return " " + formatter.pattern(R.string._from_device)
                    .put("device_name", deviceName)
                    .format();
        }
        return null;
    }

    @Nullable
    private String formatEventTime() {
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);
        return formatter.pattern(R.string.time_time)
                .put("time", df.format(new Date(event.getStartTime())))
                .format();
    }

    @NonNull
    private String formatSendTime() {
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);
        return " " + formatter.pattern(R.string._at_time)
                .put("time", df.format(sendTime))
                .format();
    }

    @NonNull
    private String formatLocation() {
        GeoCoordinates location = event.getLocation();
        if (location != null) {
            return formatter.pattern(R.string.last_known_location)
                    .put("location", formatter
                            .pattern(GOOGLE_MAP_LINK_PATTERN)
                            .put("latitude", valueOf(location.getLatitude()))
                            .put("longitude", valueOf(location.getLongitude()))
                            .put("location", Util.formatLocation(location, "&#176;", "\'", "\"", "N", "S", "W", "E"))
                            .format())
                    .format();
        } else {
            return formatter.pattern(R.string.last_known_location)
                    .put("location", GeoLocator.isPermissionsDenied(context) /* base context here */
                            ? R.string.no_permission_read_location
                            : R.string.geolocation_disabled)
                    .format();
        }
    }

    @Nullable
    private String formatRemoteControlLinks() {
        if (contentOptions.contains(VAL_PREF_EMAIL_CONTENT_REMOTE_COMMAND_LINKS) && !isEmpty(serviceAccount)) {
            return formatter
                    .pattern(REPLY_LINKS_PATTERN)
                    .put("title", formatter
                            .pattern(R.string.reply_ot_app)
                            .put("app_name", R.string.app_name))
                    .put("links", formatRemoteControlLinksList())
                    .format();
        }
        return null;
    }

    @NonNull
    private String formatRemoteControlLinksList() {
        String phone = escapePhone(event.getPhone());

        String phoneTask = formatCommonTaskBody(R.string.add_phone_to_blacklist_reply_body, phone);
        String textTask = formatCommonTaskBody(R.string.add_text_to_blacklist_reply_body, event.getText());
        String sentTask = formatSendSmsRemoteTaskBody(phone);

        return formatRemoteControlLink(R.string.add_phone_to_blacklist, phoneTask) +
                formatRemoteControlLink(R.string.add_text_to_blacklist, textTask) +
                formatRemoteControlLink(R.string.send_sms_to_sender, sentTask);
    }

    @NonNull
    private String formatRemoteControlLink(int titleRes, String body) {
        return formatter
                .pattern(REMOTE_CONTROL_LINK_PATTERN)
                .put("address", serviceAccount)
                .put("subject", htmlEncode("Re: " + formatSubject()))
                .put("body", htmlEncode(formatServiceMailBody(body)))
                .put("link_title", titleRes)
                .format();
    }

    @NonNull
    private String formatCommonTaskBody(int patternRes, String argument) {
        return formatter
                .pattern(patternRes)
                .put("argument", argument)
                .format();
    }

    @NonNull
    private String formatSendSmsRemoteTaskBody(String phone) {
        return formatter
                .pattern(R.string.send_sms_to_sender_reply_body)
                .put("sms_text", "Sample text")
                .put("phone", phone)
                .format();
    }

    @NonNull
    private String formatServiceMailBody(String task) {
        return formatter
                .pattern("To device \"{device}\": %0d%0a {task}")
                .put("device", deviceName)
                .put("task", task)
                .format();
    }

    @NonNull
    private String replaceUrls(String s) {
        Matcher matcher = Pattern.compile("((?i:http|https|rtsp|ftp|file)://[\\S]+)").matcher(s);

        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String url = matcher.group(1);
            matcher.appendReplacement(sb, "<a href=\"" + url + "\">" + url + "</a>");
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    @NonNull
    private String encodeUrl(String text) {
        try {
            return URLEncoder.encode(text, "UTF-8");
        } catch (UnsupportedEncodingException x) {
            throw new RuntimeException(x);
        }
    }

    private void updateResources() {
        if (locale != Locale.getDefault()) {
            Configuration configuration = context.getResources().getConfiguration();
            configuration.setLocale(locale);
            resources = context.createConfigurationContext(configuration).getResources();
        } else {
            resources = context.getResources();
        }
        formatter = new TagFormatter(resources);
    }

}
