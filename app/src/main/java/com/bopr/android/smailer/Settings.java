package com.bopr.android.smailer;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bopr.android.smailer.util.AndroidUtil;
import com.bopr.android.smailer.util.SharedPreferencesWrapper;
import com.bopr.android.smailer.util.TextUtil;
import com.bopr.android.smailer.util.Util;

import java.util.Locale;
import java.util.Set;

import static android.content.Context.MODE_PRIVATE;

/**
 * Settings.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
@SuppressWarnings("WeakerAccess")
public class Settings extends SharedPreferencesWrapper {

    private static final int SETTINGS_VERSION = 2;
    public static final String PREFERENCES_STORAGE_NAME = "com.bopr.android.smailer_preferences";

    public static final String PREF_DEVICE_ALIAS = "device_alias";
    public static final String PREF_EMAIL_CONTENT = "email_content";
    public static final String PREF_EMAIL_LOCALE = "email_locale";
    public static final String PREF_EMAIL_TRIGGERS = "email_triggers";
    public static final String PREF_FILTER_PHONE_BLACKLIST = "message_filter_blacklist";
    public static final String PREF_FILTER_PHONE_WHITELIST = "message_filter_whitelist";
    public static final String PREF_FILTER_TEXT_BLACKLIST = "message_filter_text_blacklist";
    public static final String PREF_FILTER_TEXT_WHITELIST = "message_filter_text_whitelist";
    public static final String PREF_HISTORY = "history";
    public static final String PREF_MARK_SMS_AS_READ = "mark_processed_sms_as_read";
    public static final String PREF_NOTIFY_SEND_SUCCESS = "notify_send_success";
    public static final String PREF_RECIPIENTS_ADDRESS = "recipients_address";
    public static final String PREF_REMOTE_CONTROL_ACCOUNT = "remote_control_account";
    public static final String PREF_REMOTE_CONTROL_ENABLED = "remote_control_enabled";
    public static final String PREF_REMOTE_CONTROL_FILTER_RECIPIENTS = "remote_control_filter_recipients";
    public static final String PREF_REMOTE_CONTROL_NOTIFICATIONS = "remote_control_notifications";
    public static final String PREF_RESEND_UNSENT = "resend_unsent"; /* hidden */
    public static final String PREF_RULES = "rules";
    public static final String PREF_SENDER_ACCOUNT = "sender_account";
    public static final String PREF_SETTINGS_VERSION = "settings_version";
    public static final String PREF_SYNC_TIME = "sync_time"; /* hidden */

    public static final String VAL_PREF_DEFAULT = "default";
    public static final String VAL_PREF_EMAIL_CONTENT_CONTACT = "contact_name";
    public static final String VAL_PREF_EMAIL_CONTENT_DEVICE_NAME = "device_name";
    public static final String VAL_PREF_EMAIL_CONTENT_HEADER = "header";
    public static final String VAL_PREF_EMAIL_CONTENT_LOCATION = "location";
    public static final String VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME = "time";
    public static final String VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME_SENT = "time_sent";
    public static final String VAL_PREF_EMAIL_CONTENT_REMOTE_COMMAND_LINKS = "remote_control_links";
    public static final String VAL_PREF_TRIGGER_IN_CALLS = "in_calls";
    public static final String VAL_PREF_TRIGGER_IN_SMS = "in_sms";
    public static final String VAL_PREF_TRIGGER_MISSED_CALLS = "missed_calls";
    public static final String VAL_PREF_TRIGGER_OUT_CALLS = "out_calls";
    public static final String VAL_PREF_TRIGGER_OUT_SMS = "out_sms";

    public static final Set<String> DEFAULT_CONTENT = Util.setOf(
            VAL_PREF_EMAIL_CONTENT_HEADER,
            VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME,
            VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME_SENT,
            VAL_PREF_EMAIL_CONTENT_DEVICE_NAME,
            VAL_PREF_EMAIL_CONTENT_LOCATION,
            VAL_PREF_EMAIL_CONTENT_CONTACT,
            VAL_PREF_EMAIL_CONTENT_REMOTE_COMMAND_LINKS);
    public static final Set<String> DEFAULT_TRIGGERS = Util.setOf(
            VAL_PREF_TRIGGER_IN_SMS,
            VAL_PREF_TRIGGER_MISSED_CALLS);

    public Settings(@NonNull Context context) {
        super(context.getSharedPreferences(PREFERENCES_STORAGE_NAME, MODE_PRIVATE));
    }

    public static Settings settings(@NonNull Context context) {
        return new Settings(context);
    }

    /**
     * Loads default settings values.
     */
    public void loadDefaults() {
        EditorWrapper edit = edit();

        edit.putStringSetOptional(PREF_EMAIL_TRIGGERS, DEFAULT_TRIGGERS);
        edit.putStringOptional(PREF_EMAIL_LOCALE, VAL_PREF_DEFAULT);
        edit.putBooleanOptional(PREF_RESEND_UNSENT, true);
        edit.putBooleanOptional(PREF_MARK_SMS_AS_READ, false);
        edit.putBooleanOptional(PREF_REMOTE_CONTROL_ENABLED, false);
        edit.putBooleanOptional(PREF_REMOTE_CONTROL_NOTIFICATIONS, true);
        edit.putBooleanOptional(PREF_REMOTE_CONTROL_FILTER_RECIPIENTS, true);

        Set<String> content = getStringSet(PREF_EMAIL_CONTENT);
        if (content.isEmpty()) {
            edit.putStringSet(PREF_EMAIL_CONTENT, DEFAULT_CONTENT);
        } else if (getInt(Settings.PREF_SETTINGS_VERSION, 1) == 1) {
            content.add(VAL_PREF_EMAIL_CONTENT_HEADER);
            content.add(VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME_SENT);
            edit.putStringSet(PREF_EMAIL_CONTENT, content);
        }

        edit.putInt(PREF_SETTINGS_VERSION, SETTINGS_VERSION);
        edit.apply();
    }

    @NonNull
    public Locale getLocale() {
        String value = getString(PREF_EMAIL_LOCALE, VAL_PREF_DEFAULT);
        if (value.equals(VAL_PREF_DEFAULT)) {
            return Locale.getDefault();
        } else {
            String[] ss = value.split("_");
            if (ss.length == 2) {
                return new Locale(ss[0], ss[1]);
            } else {
                throw new IllegalArgumentException("Invalid locale code: " + value);
            }
        }
    }

    @NonNull
    public String getDeviceName() {
        String name = getString(PREF_DEVICE_ALIAS, "");
        if (!TextUtil.isNullOrEmpty(name)) {
            return name;
        }
        return AndroidUtil.deviceName();
    }

    @NonNull
    public PhoneEventFilter getFilter() {
        PhoneEventFilter filter = new PhoneEventFilter();

        filter.setTriggers(getStringSet(PREF_EMAIL_TRIGGERS));
        filter.setPhoneBlacklist(getCommaSet(PREF_FILTER_PHONE_BLACKLIST));
        filter.setPhoneWhitelist(getCommaSet(PREF_FILTER_PHONE_WHITELIST));
        filter.setTextBlacklist(getCommaSet(PREF_FILTER_TEXT_BLACKLIST));
        filter.setTextWhitelist(getCommaSet(PREF_FILTER_TEXT_WHITELIST));

        return filter;
    }

    @Override
    @NonNull
    public Editor edit() {
        return new Editor(super.edit());
    }

    public class Editor extends EditorWrapper {

        private Editor(@NonNull EditorWrapper edit) {
            super(edit);
        }

        @NonNull
        public Editor putFilter(@NonNull PhoneEventFilter filter) {
            putCommaSet(PREF_FILTER_PHONE_BLACKLIST, filter.getPhoneBlacklist());
            putCommaSet(PREF_FILTER_PHONE_WHITELIST, filter.getPhoneWhitelist());
            putCommaSet(PREF_FILTER_TEXT_BLACKLIST, filter.getTextBlacklist());
            putCommaSet(PREF_FILTER_TEXT_WHITELIST, filter.getTextWhitelist());
            return this;
        }

    }

}
