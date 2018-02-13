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

import static com.bopr.android.smailer.Settings.*;
import static com.bopr.android.smailer.util.TagFormatter.formatFrom;
import static com.bopr.android.smailer.util.Util.*;

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
    private static final String PHONE_LINK_PATTERN = "<a href=\"tel:{phone}\">{phone}</a>";

    private final PhoneEvent event;
    private Context baseContext;
    private String contactName;
    private String deviceName;
    private Set<String> contentOptions;
    private Locale locale = Locale.getDefault();

    MailFormatter(Context context, PhoneEvent event) {
        this.event = event;
        this.baseContext = context;
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
    String getSubject() {
        Context context = setupContext();

        return formatFrom(SUBJECT_PATTERN, context)
                .putResource("app_name", R.string.app_name)
                .put("source", getTriggerText(context))
                .put("phone", event.getPhone())
                .format();
    }

    /**
     * Returns formatted email body.
     *
     * @return email body
     */
    @NonNull
    String getBody() {
        Context context = setupContext();

        String footerText = getFooterText(context);
        TagFormatter formatter = formatFrom(BODY_PATTERN)
                .put("message", getMessageText(context))
                .put("footer", footerText);

        if (!isEmpty(footerText)) {
            formatter.put("line", LINE);
        }

        return formatter.format();
    }

    @NonNull
    private String getTriggerText(Context context) {
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

        return context.getString(resourceId);
    }

    @NonNull
    private String getMessageText(Context context) {
        if (event.isMissed()) {
            return context.getString(R.string.email_body_missed_call);
        } else if (event.isSms()) {
            return event.getText();
        } else {
            int pattern;
            if (event.isIncoming()) {
                pattern = R.string.email_body_incoming_call;
            } else {
                pattern = R.string.email_body_outgoing_call;
            }
            return formatFrom(pattern, context)
                    .put("duration", formatDuration(event.getCallDuration()))
                    .format();
        }
    }

    @Nullable
    private String getFooterText(Context context) {
        if (contentOptions != null) {
            String callerText = contentOptions.contains(VAL_PREF_EMAIL_CONTENT_CONTACT) ? getCallerText(context) : null;
            String deviceNameText = contentOptions.contains(VAL_PREF_EMAIL_CONTENT_DEVICE_NAME) ? getDeviceNameText(context) : null;
            String timeText = contentOptions.contains(VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME) ? getTimeText(context) : null;
            String locationText = contentOptions.contains(VAL_PREF_EMAIL_CONTENT_LOCATION) ? getLocationText(context) : null;

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
                text.append(formatFrom(R.string.email_body_sent, context)
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
    private String getCallerText(Context context) {
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
            if (Contacts.isPermissionsDenied(baseContext)) { /* base context here */
                name = context.getString(R.string.email_body_unknown_contact_no_permission);
            } else {
                name = context.getString(R.string.email_body_unknown_contact);
            }
        }

        return formatFrom(resourceId, context)
                .put("phone", formatFrom(PHONE_LINK_PATTERN)
                        .put("phone", event.getPhone()))
                .put("name", name)
                .format();
    }

    @Nullable
    private String getDeviceNameText(Context context) {
        if (!isEmpty(deviceName)) {
            return " " + formatFrom(R.string.email_body_from, context)
                    .put("device_name", deviceName)
                    .format();
        }
        return null;
    }

    @Nullable
    private String getTimeText(Context context) {
        if (event.getStartTime() != null) {
            DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);
            return " " + formatFrom(R.string.email_body_time, context)
                    .put("time", df.format(new Date(event.getStartTime())))
                    .format();
        }
        return null;
    }

    @NonNull
    private String getLocationText(Context context) {
        GeoCoordinates location = event.getLocation();
        if (location != null) {
            return formatFrom(R.string.email_body_location, context)
                    .put("location", formatFrom(GOOGLE_MAP_LINK_PATTERN)
                            .put("latitude", location.getLatitude())
                            .put("longitude", location.getLongitude())
                            .put("location", formatLocation(location, "&#176;", "\'", "\"", "N", "S", "W", "E"))
                            .format())
                    .format();
        } else {
            return formatFrom(R.string.email_body_location, context)
                    .putResource("location", Locator.isPermissionsDenied(baseContext) /* base context here */
                            ? R.string.email_body_unknown_location_no_permission
                            : R.string.email_body_unknown_location)
                    .format();
        }
    }

    private Context setupContext() {
        Configuration configuration = baseContext.getResources().getConfiguration();
        configuration.setLocale(locale);
        return baseContext.createConfigurationContext(configuration);
    }

//    private Locale setupLocale() {
//        Configuration configuration = context.getResources().getConfiguration();
//        Locale locale = configuration.locale;
//        restoreLocale(this.locale);
//        return locale;
//    }
//
//    private void restoreLocale(Locale locale) {
//        Configuration configuration = context.getResources().getConfiguration();
//        configuration.locale = locale;
//        // TODO: 05.12.2017 https://stackoverflow.com/questions/40221711/android-context-getresources-updateconfiguration-deprecated
//        context.getResources().updateConfiguration(configuration, null);
//    }

}
