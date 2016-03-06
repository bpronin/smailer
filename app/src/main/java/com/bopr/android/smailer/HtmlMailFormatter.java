package com.bopr.android.smailer;

import android.content.Context;
import android.content.res.Resources;
import android.location.Location;
import android.text.TextUtils;

import com.bopr.android.smailer.util.DeviceUtil;
import com.bopr.android.smailer.util.StringUtil;

import java.text.SimpleDateFormat;
import java.util.Locale;


/**
 * Class MailFormatter.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class HtmlMailFormatter {

    private Context context;
    private MailerProperties properties;
    private MailMessage message;

    public HtmlMailFormatter(Context context, MailerProperties properties, MailMessage message) {
        this.context = context;
        this.properties = properties;
        this.message = message;
    }

    public String getSubject() {
        Resources r = context.getResources();
        return r.getString(R.string.email_subject_prefix) + " "
                + String.format(r.getString(R.string.email_subject_incoming_sms_pattern), message.getPhone());
    }

    public String getBody() {
        Resources r = context.getResources();

        StringBuilder builder = new StringBuilder();
        builder
                .append("<html>")
                .append("<head>")
                .append("<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\">")
                .append("</head>")
                .append("<body>")
                .append(message.getBody());

        StringBuilder footer = getBodyFooter();
        if (!TextUtils.isEmpty(footer))
            builder
                    .append("<hr style=\"border: none; background-color: #cccccc; height: 1px;\">")
                    .append(r.getString(R.string.email_content_sent_prefix))
                    .append(" ")
                    .append(footer);

        builder
                .append("</body>")
                .append("</html>");

        return builder.toString();
    }

    private StringBuilder getBodyFooter() {
        Resources r = context.getResources();
        StringBuilder builder = new StringBuilder();

        if (properties.isContentDeviceName()) {
            builder
                    .append(r.getString(R.string.email_content_from_prefix))
                    .append(" ")
                    .append(DeviceUtil.getDeviceName())
                    .append("<br>");
        }

        if (properties.isContentTime()) {
            SimpleDateFormat format = new SimpleDateFormat(r.getString(R.string.email_content_time_pattern), Locale.getDefault());
            builder
                    .append(r.getString(R.string.email_content_time_prefix))
                    .append(" ")
                    .append(format.format(message.getTime()))
                    .append("<br>");
        }

        if (properties.isContentLocation()) {
            builder
                    .append(r.getString(R.string.email_content_location_prefix))
                    .append(" ")
                    .append(formatGoogleMapsLink(message.getLocation()))
                    .append("<br>");
        }

        return builder;
    }

    private StringBuilder formatGoogleMapsLink(Location location) {
        return new StringBuilder()
                .append("<a href=\"http://maps.google.com/maps/place/")
                .append(location.getLatitude())
                .append(",")
                .append(location.getLongitude())
                .append("\">")
                .append(StringUtil.formatLocation(location, "&#176;", "\'", "\"", "N", "S", "W", "E"))
                .append("</a>");
    }

}
