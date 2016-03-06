package com.bopr.android.smailer;

import android.content.Context;
import android.content.res.Resources;
import android.location.Location;
import android.text.TextUtils;

import com.bopr.android.smailer.util.ContactUtil;
import com.bopr.android.smailer.util.DeviceUtil;

import java.text.DateFormat;
import java.util.Date;

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
        return resources.getString(R.string.email_subject_prefix) + " "
                + String.format(resources.getString(R.string.email_subject_incoming_sms_pattern), message.getPhone());
    }

    public String getBody() {
        StringBuilder builder = new StringBuilder();
        builder
                .append("<html>")
                .append("<head>")
                .append("<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\">")
                .append("</head>")
                .append("<body>")
                .append(message.getBody());

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

    private StringBuilder getBodyFooter() {
        StringBuilder builder = new StringBuilder();
        if (properties.isContentDeviceName()) {
            appendDeviceName(builder);
        }
        if (properties.isContentContactName()) {
            appendContactName(builder);
        }
        if (properties.isContentTime()) {
            appendTime(builder);
        }
        if (properties.isContentLocation()) {
            appendLocation(builder);
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
        Date time = message.getTime();
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
