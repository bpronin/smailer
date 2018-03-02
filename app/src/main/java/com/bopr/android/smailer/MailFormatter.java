package com.bopr.android.smailer;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.bopr.android.smailer.util.Util;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

import static com.bopr.android.smailer.Settings.*;
import static com.bopr.android.smailer.util.TagFormatter.formatter;
import static com.bopr.android.smailer.util.Util.formatDuration;
import static com.bopr.android.smailer.util.Util.isEmpty;
import static java.lang.String.valueOf;

/**
 * Formats email subject and body.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
class MailFormatter {

    private static final String SUBJECT_PATTERN = "[{app_name}] {source} {phone}";
    private static final String BODY_PATTERN = "<html>" +
            "<head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head>" +
            "<body>{message}{line}{footer}</body></html>";
    private static final String LINE = "<hr style=\"border: none; background-color: #cccccc; height: 1px;\">";
    private static final String GOOGLE_MAP_LINK_PATTERN = "<a href=\"https://www.google.com/maps/" +
            "place/{latitude}+{longitude}/@{latitude},{longitude}\">{location}</a>";
    private static final String PHONE_LINK_PATTERN = "<a href=\"tel:{phone}\" style=\"text-decoration: none\">&#9742;</a>{phone}";

    private final PhoneEvent event;
    private final Context context;
    private Resources resources;
    private Locale locale;
    private String contactName;
    private String deviceName;
    private Set<String> contentOptions;

    MailFormatter(Context context, PhoneEvent event) {
        this.event = event;
        this.context = context;
        this.resources = context.getResources();
        this.locale = Locale.getDefault();
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
     * Sets email content options. See {@link Settings#DEFAULT_CONTENT}.
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
    void setLocale(String code) {
        Locale locale = Util.stringToLocale(code);
        this.locale = locale != null ? locale : Locale.getDefault();

        updateResources();
    }

    /**
     * Returns formatted email subject.
     *
     * @return email subject
     */
    @NonNull
    String formatSubject() {
        return formatter(SUBJECT_PATTERN, resources)
                .put("app_name", R.string.app_name)
                .put("source", formatTrigger())
                .put("phone", event.getPhone())
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

        return formatter(BODY_PATTERN)
                .put("message", formatMessage())
                .put("footer", footer)
                .put("line", !isEmpty(footer) ? LINE : null)
                .format();
    }

    @NonNull
    private String formatTrigger() {
        int resourceId;

        if (event.isMissed()) {
            resourceId = R.string.email_subject_missed_call;
        } else if (event.isSms()) {
            if (event.isIncoming()) {
                resourceId = R.string.email_subject_incoming_sms;
            } else {
                resourceId = R.string.email_subject_outgoing_sms;
            }
        } else {
            if (event.isIncoming()) {
                resourceId = R.string.email_subject_incoming_call;
            } else {
                resourceId = R.string.email_subject_outgoing_call;
            }
        }

        return resources.getString(resourceId);
    }

    @NonNull
    private String formatMessage() {
        if (event.isMissed()) {
            return resources.getString(R.string.email_body_missed_call);
        } else if (event.isSms()) {
            return event.getText();
        } else {
            int pattern;
            if (event.isIncoming()) {
                pattern = R.string.email_body_incoming_call;
            } else {
                pattern = R.string.email_body_outgoing_call;
            }
            return formatter(pattern, resources)
                    .put("duration", formatDuration(event.getCallDuration()))
                    .format();
        }
    }

    @Nullable
    private String formatFooter() {
        if (contentOptions != null) {
            String callerText = contentOptions.contains(VAL_PREF_EMAIL_CONTENT_CONTACT) ? formatCaller() : null;
            String deviceNameText = contentOptions.contains(VAL_PREF_EMAIL_CONTENT_DEVICE_NAME) ? formatDeviceName() : null;
            String timeText = contentOptions.contains(VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME) ? formatTime() : null;
            String locationText = contentOptions.contains(VAL_PREF_EMAIL_CONTENT_LOCATION) ? formatLocation() : null;

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

                text.append(formatter(R.string.email_body_sent, resources)
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
    private String formatCaller() {
        int resourceId;
        if (event.isSms()) {
            resourceId = R.string.email_body_sender;
        } else {
            if (event.isIncoming()) {
                resourceId = R.string.email_body_caller;
            } else {
                resourceId = R.string.email_body_called;
            }
        }

        String name = this.contactName;
        if (isEmpty(name)) {
            if (Contacts.isPermissionsDenied(context)) { /* base context here */
                name = resources.getString(R.string.email_body_unknown_contact_no_permission);
            } else {
                name = resources.getString(R.string.email_body_unknown_contact);
            }
        }

        return formatter(resourceId, resources)
                .put("phone", formatter(PHONE_LINK_PATTERN)
                        .put("phone", event.getPhone())
                        .format())
                .put("name", name)
                .format();
    }

    @Nullable
    private String formatDeviceName() {
        if (!isEmpty(deviceName)) {
            return " " + formatter(R.string.email_body_from, resources)
                    .put("device_name", deviceName)
                    .format();
        }
        return null;
    }

    @Nullable
    private String formatTime() {
        if (event.getStartTime() != null) {
            DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);
            return " " + formatter(R.string.email_body_time, resources)
                    .put("time", df.format(new Date(event.getStartTime())))
                    .format();
        }
        return null;
    }

    @NonNull
    private String formatLocation() {
        GeoCoordinates location = event.getLocation();
        if (location != null) {
            return formatter(R.string.email_body_location, resources)
                    .put("location", formatter(GOOGLE_MAP_LINK_PATTERN)
                            .put("latitude", valueOf(location.getLatitude()))
                            .put("longitude", valueOf(location.getLongitude()))
                            .put("location", Util.formatLocation(location, "&#176;", "\'", "\"", "N", "S", "W", "E"))
                            .format())
                    .format();
        } else {
            return formatter(R.string.email_body_location, resources)
                    .put("location", Locator.isPermissionsDenied(context) /* base context here */
                            ? R.string.email_body_unknown_location_no_permission
                            : R.string.email_body_unknown_location)
                    .format();
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
    }

}
