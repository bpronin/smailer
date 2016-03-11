package com.bopr.android.smailer;

import android.content.res.Resources;

import com.bopr.android.smailer.util.TagFormatter;

import java.text.DateFormat;
import java.util.Date;
import java.util.Set;

import static com.bopr.android.smailer.settings.Settings.VAL_PREF_EMAIL_CONTENT_CALLER;
import static com.bopr.android.smailer.settings.Settings.VAL_PREF_EMAIL_CONTENT_DEVICE_NAME;
import static com.bopr.android.smailer.settings.Settings.VAL_PREF_EMAIL_CONTENT_LOCATION;
import static com.bopr.android.smailer.settings.Settings.VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME;
import static com.bopr.android.smailer.util.StringUtil.formatDuration;
import static com.bopr.android.smailer.util.StringUtil.formatLocation;
import static com.bopr.android.smailer.util.StringUtil.isEmpty;
import static com.bopr.android.smailer.util.TagFormatter.from;


/**
 * Formats email parts.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class MailFormatter {

    private static final String SUBJECT_PATTERN = "[{app_name}] {source} {phone}";
    private static final String BODY_PATTERN = "<html>" +
            "<head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head>" +
            "<body>{message}{line}{footer}</body></html>";
    private static final String LINE = "<hr style=\"border: none; background-color: #cccccc; height: 1px;\">";
    private static final String GOOGLE_MAP_LINK_PATTERN = "<a href=\"http://maps.google.com/maps/place/{latitude},{longitude}\">{location}</a>";
    private static final String PHONE_LINK_PATTERN = "<a href=\"tel:{phone}\">{phone}{name}</a>";

    private final Resources resources;
    private final MailerProperties properties;
    private final MailMessage message;
    private final String contactName;
    private final String deviceName;

    public MailFormatter(MailMessage message, Resources resources, MailerProperties properties,
                         String contactName, String deviceName
    ) {
        this.resources = resources;
        this.message = message;
        this.properties = properties;
        this.contactName = contactName;
        this.deviceName = deviceName;
    }

    public String getSubject() {
        return from(SUBJECT_PATTERN, resources)
                .putResource("app_name", R.string.app_name)
                .put("source", getSourceText())
                .put("phone", message.getPhone())
                .format();
    }

    private String getSourceText() {
        int resourceId;

        if (message.isMissed()) {
            resourceId = R.string.email_subject_missed_call;
        } else if (message.isSms()) {
            if (message.isIncoming()) {
                resourceId = R.string.email_subject_incoming_sms;
            } else {
                resourceId = R.string.email_subject_outgoing_sms;
            }
        } else {
            if (message.isIncoming()) {
                resourceId = R.string.email_subject_incoming_call;
            } else {
                resourceId = R.string.email_subject_outgoing_call;
            }
        }

        return resources.getString(resourceId);
    }

    private String getMessageText() {
        if (message.isMissed()) {
            return resources.getString(R.string.email_body_missed_call);
        } else if (message.isSms()) {
            return message.getBody();
        } else {
            int pattern;
            if (message.isIncoming()) {
                pattern = R.string.email_body_incoming_call;
            } else {
                pattern = R.string.email_body_outgoing_call;
            }
            return from(pattern, resources)
                    .put("duration", formatDuration(message.getCallDuration()))
                    .format();
        }
    }

    public String getBody() {
        String footerText = getFooterText();
        TagFormatter formatter = from(BODY_PATTERN)
                .put("message", getMessageText())
                .put("footer", footerText);
        if (!isEmpty(footerText)) {
            formatter.put("line", LINE);
        }
        return formatter.format();
    }

    private String getFooterText() {
        Set<String> options = properties.getContentOptions();
        if (options != null) {
            String callerText = options.contains(VAL_PREF_EMAIL_CONTENT_CALLER) ? getCallerText() : null;
            String deviceNameText = options.contains(VAL_PREF_EMAIL_CONTENT_DEVICE_NAME) ? getDeviceNameText() : null;
            String timeText = options.contains(VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME) ? getTimeText() : null;
            String locationText = options.contains(VAL_PREF_EMAIL_CONTENT_LOCATION) ? getLocationText() : null;

            StringBuilder text = new StringBuilder();

            if (!isEmpty(callerText)) {
                text.append(callerText);
            }

            if (!isEmpty(locationText)) {
                if (!isEmpty(callerText)) {
                    text.append("<br>");
                }
                text.append(locationText);
            }

            if (!isEmpty(deviceNameText) || !isEmpty(timeText)) {
                if (!isEmpty(callerText) || !isEmpty(locationText)) {
                    text.append("<br>");
                }
                text.append(from(R.string.email_body_sent, resources)
                        .put("device_name", deviceNameText)
                        .put("time", timeText));
            }

            return text.toString();
        }
        return null;
    }

    private String getCallerText() {

        int resourceId;
        if (message.isSms()) {
            resourceId = R.string.email_body_sender;
        } else {
            if (message.isIncoming()) {
                resourceId = R.string.email_body_caller;
            } else {
                resourceId = R.string.email_body_called;
            }
        }

        return from(resourceId, resources)
                .put("phone", from(PHONE_LINK_PATTERN)
                        .put("phone", message.getPhone())
                        .put("name", !isEmpty(contactName) ? " (" + contactName + ")" : null))
                .format();
    }

    private String getDeviceNameText() {
        if (!isEmpty(deviceName)) {
            return " " + from(R.string.email_body_from, resources)
                    .put("device_name", deviceName)
                    .format();
        }
        return null;
    }

    private String getTimeText() {
        Date time = message.getStartTime();
        if (time != null) {
            return " " + from(R.string.email_body_time, resources)
                    .put("time", DateFormat.getDateTimeInstance().format(time))
                    .format();
        }
        return null;
    }

    private String getLocationText() {
        Double latitude = message.getLatitude();
        Double longitude = message.getLongitude();
        if (latitude != null && longitude != null) {
            return from(R.string.email_body_location, resources)
                    .put("location", from(GOOGLE_MAP_LINK_PATTERN)
                            .put("latitude", latitude)
                            .put("longitude", longitude)
                            .put("location", formatLocation(latitude, longitude, "&#176;", "\'", "\"", "N", "S", "W", "E"))
                            .format())
                    .format();
        }
        return null;
    }

}
