package com.bopr.android.smailer;

import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bopr.android.smailer.util.TagFormatter;
import com.bopr.android.smailer.util.Util;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

import static com.bopr.android.smailer.Settings.VAL_PREF_EMAIL_CONTENT_CONTACT;
import static com.bopr.android.smailer.Settings.VAL_PREF_EMAIL_CONTENT_DEVICE_NAME;
import static com.bopr.android.smailer.Settings.VAL_PREF_EMAIL_CONTENT_LOCATION;
import static com.bopr.android.smailer.Settings.VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME;
import static com.bopr.android.smailer.util.TagFormatter.from;
import static com.bopr.android.smailer.util.Util.formatDuration;
import static com.bopr.android.smailer.util.Util.formatLocation;
import static com.bopr.android.smailer.util.Util.isEmpty;

/**
 * Formats email subject and body.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class MailFormatter {

    private static final String SUBJECT_PATTERN = "[{app_name}] {source} {phone}";
    private static final String BODY_PATTERN = "<html>" +
            "<head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head>" +
            "<body>{message}{line}{footer}</body></html>";
    private static final String LINE = "<hr style=\"border: none; background-color: #cccccc; height: 1px;\">";
    private static final String GOOGLE_MAP_LINK_PATTERN = "<a href=\"https://www.google.com/maps/" +
            "place/{latitude}+{longitude}/@{latitude},{longitude}\">{location}</a>";
    private static final String PHONE_LINK_PATTERN = "<a href=\"tel:{phone}\">{phone}</a>";

    private final MailMessage message;
    private Context context;
    private String contactName;
    private String deviceName;
    private Set<String> contentOptions;
    private Locale locale = Locale.getDefault();

    public MailFormatter(Context context, MailMessage message) {
        this.message = message;
        this.context = context;
    }

    /**
     * Sets contact name to be used in email body.
     *
     * @param contactName name
     */
    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    /**
     * Sets device name to be used in email body.
     *
     * @param deviceName name
     */
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    /**
     * Sets email content options. See {@link Settings#DEFAULT_CONTENT}.
     *
     * @param contentOptions set of options
     */
    public void setContentOptions(Set<String> contentOptions) {
        this.contentOptions = contentOptions;
    }

    /**
     * Sets custom mail locale.
     *
     * @param code locale code as "en_EN"
     */
    public void setLocale(String code) {
        Locale locale = Util.stringToLocale(code);
        if (locale != null) {
            this.locale = locale;
        } else {
            this.locale = Locale.getDefault();
        }
    }

    /**
     * Returns formatted email subject.
     *
     * @return email subject
     */
    @NonNull
    public String getSubject() {
        Locale currentLocale = setupLocale();

        String result = from(SUBJECT_PATTERN, context)
                .putResource("app_name", R.string.app_name)
                .put("source", getTriggerText())
                .put("phone", message.getPhone())
                .format();

        restoreLocale(currentLocale);
        return result;
    }

    /**
     * Returns formatted email body.
     *
     * @return email body
     */
    @NonNull
    public String getBody() {
        Locale currentLocale = setupLocale();

        String footerText = getFooterText();
        TagFormatter formatter = from(BODY_PATTERN)
                .put("message", getMessageText())
                .put("footer", footerText);
        if (!isEmpty(footerText)) {
            formatter.put("line", LINE);
        }
        String result = formatter.format();

        restoreLocale(currentLocale);
        return result;
    }

    @NonNull
    private String getTriggerText() {
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

        return context.getString(resourceId);
    }

    @NonNull
    private String getMessageText() {
        if (message.isMissed()) {
            return context.getString(R.string.email_body_missed_call);
        } else if (message.isSms()) {
            return message.getText();
        } else {
            int pattern;
            if (message.isIncoming()) {
                pattern = R.string.email_body_incoming_call;
            } else {
                pattern = R.string.email_body_outgoing_call;
            }
            return from(pattern, context)
                    .put("duration", formatDuration(message.getCallDuration()))
                    .format();
        }
    }

    @Nullable
    private String getFooterText() {
        if (contentOptions != null) {
            String callerText = contentOptions.contains(VAL_PREF_EMAIL_CONTENT_CONTACT) ? getCallerText() : null;
            String deviceNameText = contentOptions.contains(VAL_PREF_EMAIL_CONTENT_DEVICE_NAME) ? getDeviceNameText() : null;
            String timeText = contentOptions.contains(VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME) ? getTimeText() : null;
            String locationText = contentOptions.contains(VAL_PREF_EMAIL_CONTENT_LOCATION) ? getLocationText() : null;

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
                text.append(from(R.string.email_body_sent, context)
                        .put("device_name", deviceNameText)
                        .put("time", timeText));
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

        String name = this.contactName;
        if (isEmpty(name)) {
            if (Contacts.isPermissionsDenied(context)) {
                name = context.getString(R.string.email_body_unknown_contact_no_permission);
            } else {
                name = context.getString(R.string.email_body_unknown_contact);
            }
        }

        return from(resourceId, context)
                .put("phone", from(PHONE_LINK_PATTERN)
                        .put("phone", message.getPhone()))
                .put("name", name)
                .format();
    }

    @Nullable
    private String getDeviceNameText() {
        if (!isEmpty(deviceName)) {
            return " " + from(R.string.email_body_from, context)
                    .put("device_name", deviceName)
                    .format();
        }
        return null;
    }

    @Nullable
    private String getTimeText() {
        if (message.getStartTime() != null) {
            DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);
            return " " + from(R.string.email_body_time, context)
                    .put("time", df.format(new Date(message.getStartTime())))
                    .format();
        }
        return null;
    }

    private String getLocationText() {
        GeoCoordinates location = message.getLocation();
        if (location != null) {
            return from(R.string.email_body_location, context)
                    .put("location", from(GOOGLE_MAP_LINK_PATTERN)
                            .put("latitude", location.getLatitude())
                            .put("longitude", location.getLongitude())
                            .put("location", formatLocation(location, "&#176;", "\'", "\"", "N", "S", "W", "E"))
                            .format())
                    .format();
        } else {
            return from(R.string.email_body_location, context)
                    .putResource("location", Locator.isPermissionsDenied(context)
                            ? R.string.email_body_unknown_location_no_permission
                            : R.string.email_body_unknown_location)
                    .format();
        }
    }

    private Locale setupLocale() {
        Configuration configuration = context.getResources().getConfiguration();
        Locale locale = configuration.locale;
        restoreLocale(this.locale);
        return locale;
    }

    private void restoreLocale(Locale locale) {
        Configuration configuration = context.getResources().getConfiguration();
        configuration.locale = locale;
        context.getResources().updateConfiguration(configuration, null);
    }

}
