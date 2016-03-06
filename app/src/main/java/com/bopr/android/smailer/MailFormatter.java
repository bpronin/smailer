package com.bopr.android.smailer;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;

import com.bopr.android.smailer.util.DeviceUtil;
import com.bopr.android.smailer.util.StringUtil;

import java.text.SimpleDateFormat;
import java.util.Locale;

import static com.bopr.android.smailer.util.StringUtil.*;


/**
 * Class MailFormatter.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class MailFormatter {

    private Context context;
    private MailerProperties properties;
    private MailMessage message;

    public MailFormatter(Context context, MailerProperties properties, MailMessage message) {
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
        builder.append(message.getBody());

        String footer = getBodyFooter();
        if (!TextUtils.isEmpty(footer))
        builder
                .append("\n")
                .append(r.getString(R.string.email_content_body_delimiter))
                .append("\n")
                .append(r.getString(R.string.email_content_sent_prefix))
                .append(" ")
                .append(footer);

        return builder.toString();
    }

    private String getBodyFooter() {
        Resources r = context.getResources();
        StringBuilder builder = new StringBuilder();

        if (properties.isContentDeviceName()) {
            builder
                    .append(r.getString(R.string.email_content_from_prefix))
                    .append(" ")
                    .append(DeviceUtil.getDeviceName())
                    .append("\n");
        }

        if (properties.isContentTime()) {
            SimpleDateFormat format = new SimpleDateFormat(r.getString(R.string.email_content_time_pattern), Locale.getDefault());
            builder
                    .append(r.getString(R.string.email_content_time_prefix))
                    .append(" ")
                    .append(format.format(message.getTime()))
                    .append("\n");
        }

        if (properties.isContentLocation()) {
            builder
                    .append(r.getString(R.string.email_content_location_prefix))
                    .append(" ")
                    .append(formatLocation(message.getLocation()))
                    .append("\n");
        }

        return builder.toString();
    }

}
