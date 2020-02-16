package com.bopr.android.smailer;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.bopr.android.smailer.util.TagFormatter;
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
import static com.bopr.android.smailer.util.TextUtil.formatCoordinates;
import static com.bopr.android.smailer.util.TextUtil.formatDuration;
import static com.bopr.android.smailer.util.TextUtil.isNotEmpty;
import static com.bopr.android.smailer.util.TextUtil.isNullOrBlank;
import static com.bopr.android.smailer.util.UiUtil.eventTypePrefix;
import static com.bopr.android.smailer.util.UiUtil.eventTypeText;
import static com.bopr.android.smailer.util.Util.requireNonNull;
import static java.lang.String.valueOf;

/**
 * Formats email subject and body.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
class MailFormatter {

    private static final String SUBJECT_PATTERN = "[{app_name}] {source} {phone}";
    private static final String BODY_PATTERN = "<html><head><meta http-equiv=\"content-type\" " +
            "content=\"text/html; charset=utf-8\">" +
            "</head><body>{header}{message}{footer_line}{footer}{remote_line}{remote_links}</body></html>";
    private static final String LINE = "<hr style=\"border: none; background-color: #cccccc; height: 1px;\">";
    private static final String HEADER_PATTERN = "<strong>{header}</strong><br><br>";
    private static final String GOOGLE_MAP_LINK_PATTERN = "<a href=\"https://www.google.com/maps/" +
            "place/{latitude}+{longitude}/@{latitude},{longitude}\">{location}</a>";
    private static final String PHONE_LINK_PATTERN = "<a href=\"tel:{phone}\" style=\"text-decoration: " +
            "none\">&#9742;</a>{text}";
    private static final String REPLY_LINKS_PATTERN = "<ul>{links}</ul>";
    private static final String MAIL_TO_PATTERN = "<a href=\"mailto:{address}?subject={subject}&amp;" +
            "body={body}\">{link_title}</a>";
    private static final String REMOTE_CONTROL_LINK_PATTERN = "<li><small>" + MAIL_TO_PATTERN + "</small></li>";
    private static final String GOOGLE_SEARCH_PATTERN = "<a href=\"https://www.google.com/search?q={query}\">{text}</a>";

    private final PhoneEvent event;
    private final Context context;
    private Resources resources;
    private String contactName;
    private String deviceName;
    private Set<String> contentOptions;
    private Date sendTime;
    private String serviceAccount;
    private TagFormatter formatter;
    private DateFormat dateTimeFormat;

    MailFormatter(Context context, PhoneEvent event) {
        this.event = event;
        this.context = context;
        contentOptions = ImmutableSet.of();
        setLocale(Locale.getDefault());
    }

    /**
     * Sets mail locale.
     */
    void setLocale(@NonNull Locale locale) {
        if (locale == Locale.getDefault()) {
            resources = context.getResources();
        } else {
            Configuration configuration = context.getResources().getConfiguration();
            configuration.setLocale(locale);
            resources = context.createConfigurationContext(configuration).getResources();
        }
        formatter = new TagFormatter(resources);
        dateTimeFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);
    }

    /**
     * Sets email content options.
     *
     * @param contentOptions set of options
     */
    void setContentOptions(@NonNull Set<String> contentOptions) {
        this.contentOptions = contentOptions;
    }

    /**
     * Sets contact name to be used in email body.
     *
     * @param contactName name
     */
    void setContactName(@Nullable String contactName) {
        this.contactName = contactName;
    }

    /**
     * Sets device name to be used in email body.
     *
     * @param deviceName name
     */
    void setDeviceName(@Nullable String deviceName) {
        this.deviceName = deviceName;
    }

    /**
     * Sets email send time
     *
     * @param sendTime time
     */
    void setSendTime(@Nullable Date sendTime) {
        this.sendTime = sendTime;
    }

    /**
     * Sets service account email address
     *
     * @param serviceAddress address
     */
    void setServiceAccount(@Nullable String serviceAddress) {
        this.serviceAccount = serviceAddress;
    }

    /**
     * Returns formatted email subject.
     *
     * @return email subject
     */
    @NonNull
    String formatSubject() {
        return formatter
                .pattern(SUBJECT_PATTERN)
                .put("app_name", R.string.app_name)
                .put("source", eventTypePrefix(event))
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
        String links = formatRemoteControlLinks();

        return formatter
                .pattern(BODY_PATTERN)
                .put("header", formatHeader())
                .put("message", formatMessage())
                .put("footer_line", isNotEmpty(footer) ? LINE : "")
                .put("footer", isNotEmpty(footer) ? "<small>" + footer + "</small>" : "")
                .put("remote_line", isNotEmpty(links) ? LINE : "")
                .put("remote_links", links)
                .format();
    }

    @NonNull
    private String formatMessage() {
        if (event.isMissed()) {
            return resources.getString(R.string.you_had_missed_call);
        } else if (event.isSms()) {
            return replaceUrls(requireNonNull(event.getText()));
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

    @NonNull
    private String formatHeader() {
        if (contentOptions.contains(VAL_PREF_EMAIL_CONTENT_HEADER)) {
            return formatter
                    .pattern(HEADER_PATTERN)
                    .put("header", eventTypeText(event))
                    .format();
        }
        return "";
    }

    @NonNull
    private String formatFooter() {
        String callerText = formatCaller();
        String timeText = formatEventTime();
        String deviceNameText = formatDeviceName();
        String sendTimeText = formatSendTime();
        String locationText = formatLocation();

        StringBuilder sb = new StringBuilder();

        sb.append(callerText);

        if (isNotEmpty(timeText)) {
            if (isNotEmpty(sb)) {
                sb.append("<br>");
            }
            sb.append(timeText);
        }

        if (isNotEmpty(locationText)) {
            if (isNotEmpty(sb)) {
                sb.append("<br>");
            }
            sb.append(locationText);
        }

        if (isNotEmpty(deviceNameText) || isNotEmpty(sendTimeText)) {
            if (isNotEmpty(sb)) {
                sb.append("<br>");
            }

            sb.append(formatter
                    .pattern(R.string.sent_time_device)
                    .put("device_name", deviceNameText)
                    .put("time", sendTimeText));
        }

        return sb.toString();
    }

    @NonNull
    private String formatCaller() {
        if (contentOptions.contains(VAL_PREF_EMAIL_CONTENT_CONTACT)) {
            int patternRes;
            if (event.isSms()) {
                patternRes = R.string.sender_phone;
            } else {
                if (event.isIncoming()) {
                    patternRes = R.string.caller_phone;
                } else {
                    patternRes = R.string.called_phone;
                }
            }

            String phoneQuery = encodeUrl(event.getPhone());
            String name = this.contactName;
            if (isNullOrBlank(name)) {
                if (isReadContactsPermissionsDenied(context)) {
                    name = resources.getString(R.string.contact_no_permission_read_contact);
                } else {
                    name = formatter
                            .pattern(GOOGLE_SEARCH_PATTERN)
                            .put("query", phoneQuery)
                            .put("text", R.string.unknown_contact)
                            .format();
                }
            }

            return formatter
                    .pattern(patternRes)
                    .put("phone", formatter
                            .pattern(PHONE_LINK_PATTERN)
                            .put("phone", phoneQuery)
                            .put("text", event.getPhone())
                            .format())
                    .put("name", name)
                    .format();
        }
        return "";
    }

    @NonNull
    private String formatDeviceName() {
        if (contentOptions.contains(VAL_PREF_EMAIL_CONTENT_DEVICE_NAME) && !isNullOrBlank(deviceName)) {
            return " " + formatter
                    .pattern(R.string._from_device)
                    .put("device_name", deviceName)
                    .format();
        }
        return "";
    }

    @NonNull
    private String formatEventTime() {
        if (contentOptions.contains(VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME)) {
            return formatter
                    .pattern(R.string.time_time)
                    .put("time", dateTimeFormat.format(new Date(event.getStartTime())))
                    .format();
        }
        return "";
    }

    @NonNull
    private String formatSendTime() {
        if (contentOptions.contains(VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME_SENT) && sendTime != null) {
            return " " + formatter
                    .pattern(R.string._at_time)
                    .put("time", dateTimeFormat.format(sendTime))
                    .format();
        }
        return "";
    }

    @NonNull
    private String formatLocation() {
        if (contentOptions.contains(VAL_PREF_EMAIL_CONTENT_LOCATION)) {
            GeoCoordinates coordinates = event.getLocation();
            if (coordinates != null) {
                return formatter
                        .pattern(R.string.last_known_location)
                        .put("location", formatter
                                .pattern(GOOGLE_MAP_LINK_PATTERN)
                                .put("latitude", valueOf(coordinates.getLatitude()))
                                .put("longitude", valueOf(coordinates.getLongitude()))
                                .put("location", formatCoordinates(coordinates, "&#176;", "\'", "\"", "N", "S", "W", "E"))
                                .format())
                        .format();
            } else {
                return formatter
                        .pattern(R.string.last_known_location)
                        .put("location", GeoLocator.isPermissionsDenied(context)
                                ? R.string.no_permission_read_location
                                : R.string.geolocation_disabled)
                        .format();
            }
        }
        return "";
    }

    @NonNull
    private String formatRemoteControlLinks() {
        if (contentOptions.contains(VAL_PREF_EMAIL_CONTENT_REMOTE_COMMAND_LINKS) && !isNullOrBlank(serviceAccount)) {
            return formatter
                    .pattern(REPLY_LINKS_PATTERN)
                    .put("title", formatter
                            .pattern(R.string.reply_ot_app)
                            .put("app_name", R.string.app_name))
                    .put("links", formatRemoteControlLinksList())
                    .format();
        }
        return "";
    }

    @NonNull
    private String formatRemoteControlLinksList() {
        String phone = escapePhone(event.getPhone());
        String text = event.getText();

        String phoneTask = formatRemoteTaskBody(R.string.add_phone_to_blacklist_reply_body, phone);
        String textTask = text != null ? formatRemoteTaskBody(R.string.add_text_to_blacklist_reply_body, text) : "";
        String sentTask = formatSendSmsRemoteTaskBody(phone);

        return formatRemoteControlLink(R.string.add_phone_to_blacklist, phoneTask) +
                formatRemoteControlLink(R.string.add_text_to_blacklist, textTask) +
                formatRemoteControlLink(R.string.send_sms_to_sender, sentTask);
    }

    @NonNull
    private String formatRemoteControlLink(@StringRes int titleRes, @NonNull String body) {
        return formatter
                .pattern(REMOTE_CONTROL_LINK_PATTERN)
                .put("address", serviceAccount)
                .put("subject", htmlEncode("Re: " + formatSubject()))
                .put("body", htmlEncode(formatServiceMailBody(body)))
                .put("link_title", titleRes)
                .format();
    }

    @NonNull
    private String formatRemoteTaskBody(@StringRes int patternRes, @NonNull String argument) {
        return formatter
                .pattern(patternRes)
                .put("argument", argument)
                .format();
    }

    @NonNull
    private String formatSendSmsRemoteTaskBody(@NonNull String phone) {
        return formatter
                .pattern(R.string.send_sms_to_sender_reply_body)
                .put("sms_text", "Sample text")
                .put("phone", phone)
                .format();
    }

    @NonNull
    private String formatServiceMailBody(@NonNull String task) {
        return formatter
                .pattern("To device \"{device}\": %0d%0a {task}")
                .put("device", deviceName)
                .put("task", task)
                .format();
    }

    @NonNull
    private String replaceUrls(@NonNull String s) {
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
    private String encodeUrl(@NonNull String text) {
        try {
            return URLEncoder.encode(text, "UTF-8");
        } catch (UnsupportedEncodingException x) {
            throw new RuntimeException(x);
        }
    }

}
