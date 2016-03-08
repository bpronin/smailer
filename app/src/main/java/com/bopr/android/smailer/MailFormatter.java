package com.bopr.android.smailer;

import android.content.Context;
import android.content.res.Resources;
import android.location.Location;
import android.text.TextUtils;

import com.bopr.android.smailer.util.ContactUtil;
import com.bopr.android.smailer.util.DeviceUtil;
import com.bopr.android.smailer.util.StringUtil;

import java.text.DateFormat;
import java.util.Date;
import java.util.Set;

import static com.bopr.android.smailer.settings.Settings.VAL_EMAIL_CONTENT_CONTACT_NAME;
import static com.bopr.android.smailer.settings.Settings.VAL_EMAIL_CONTENT_DEVICE_NAME;
import static com.bopr.android.smailer.settings.Settings.VAL_EMAIL_CONTENT_LOCATION;
import static com.bopr.android.smailer.settings.Settings.VAL_EMAIL_CONTENT_MESSAGE_TIME;
import static com.bopr.android.smailer.util.StringUtil.formatLocation;


/**
 * Formats email parts.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class MailFormatter {

    private Context context;
    private MailerProperties properties;
    private MailMessage message;
    private Resources resources;

    public MailFormatter(Context context, MailerProperties properties, MailMessage message) {
        this.context = context;
        this.resources = context.getResources();
        this.properties = properties;
        this.message = message;
    }

    public String getSubject() {
        int resId;

        if (message.isMissed()) {
            resId = R.string.email_subject_missed_call_pattern;
        } else if (message.isSms()) {
            if (message.isIncoming()) {
                resId = R.string.email_subject_incoming_sms_pattern;
            } else {
                resId = R.string.email_subject_outgoing_sms_pattern;
            }
        } else {
            if (message.isIncoming()) {
                resId = R.string.email_subject_incoming_call_pattern;
            } else {
                resId = R.string.email_subject_outgoing_call_pattern;
            }
        }

        return resources.getString(R.string.email_subject_prefix) + " "
                + String.format(resources.getString(resId), message.getPhone());
    }

    public String getBody() {
        StringBuilder builder = new StringBuilder();
        builder
                .append("<html>")
                .append("<head>")
                .append("<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\">")
                .append("</head>")
                .append("<body>")
                .append(getBodyText());

        StringBuilder footer = getBodyFooter();
        if (!TextUtils.isEmpty(footer)) {
            builder
                    .append("<hr style=\"border: none; background-color: #cccccc; height: 1px;\">")
                    .append(resources.getString(R.string.email_content_sent_prefix))
                    .append(" ")
                    .append(footer);
        }

        builder
                .append("</body>")
                .append("</html>");

        return builder.toString();
    }

    private String getBodyText() {
        if (message.isMissed()) {
            return resources.getString(R.string.email_body_missed_call);
        } else if (message.isSms()) {
            return message.getBody();
        } else {
            String pattern;
            if (message.isIncoming()) {
                pattern = resources.getString(R.string.email_body_incoming_call_pattern);
            } else {
                pattern = resources.getString(R.string.email_body_outgoing_call_pattern);
            }
            String duration = StringUtil.formatDuration(message.getCallDuration());
            return String.format(pattern, duration);
        }
    }

    private StringBuilder getBodyFooter() {
        StringBuilder builder = new StringBuilder();
        Set<String> options = properties.getContentOptions();
        if (options != null) {
            if (options.contains(VAL_EMAIL_CONTENT_DEVICE_NAME)) {
                appendDeviceName(builder);
            }
            if (options.contains(VAL_EMAIL_CONTENT_CONTACT_NAME)) {
                appendContactName(builder);
            }
            if (options.contains(VAL_EMAIL_CONTENT_MESSAGE_TIME)) {
                appendTime(builder);
            }
            if (options.contains(VAL_EMAIL_CONTENT_LOCATION)) {
                appendLocation(builder);
            }
        }
        return builder;
    }

    private void appendContactName(StringBuilder builder) {
        String contactName = ContactUtil.getContactName(context, message.getPhone());
        if (!TextUtils.isEmpty(contactName)) {
            builder
                    .append(resources.getString(R.string.email_content_by_prefix))
                    .append(" ")
                    .append(contactName)
                    .append("<br>");
        }
    }

    private void appendDeviceName(StringBuilder builder) {
        builder
                .append(resources.getString(R.string.email_content_from_prefix))
                .append(" ")
                .append(DeviceUtil.getDeviceName())
                .append("<br>");
    }

    private void appendTime(StringBuilder builder) {
        Date time = message.getStartTime();
        if (time != null) {
            builder
                    .append(resources.getString(R.string.email_content_time_prefix))
                    .append(" ")
                    .append(DateFormat.getDateTimeInstance().format(time))
                    .append("<br>");
        }
    }

    private void appendLocation(StringBuilder builder) {
        Location location = message.getLocation();
        if (location != null) {
            builder
                    .append(resources.getString(R.string.email_content_location_prefix))
                    .append(" ")
                    .append("<a href=\"http://maps.google.com/maps/place/")
                    .append(location.getLatitude())
                    .append(",")
                    .append(location.getLongitude())
                    .append("\">")
                    .append(formatLocation(location, "&#176;", "\'", "\"", "N", "S", "W", "E"))
                    .append("</a>")
                    .append("<br>");
        }
    }

}
