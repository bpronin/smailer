package com.bopr.android.smailer;

import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;

import com.bopr.android.smailer.util.AndroidUtil;
import com.bopr.android.smailer.util.SharedPreferencesWrapper;
import com.bopr.android.smailer.util.TextUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

import static android.content.Context.MODE_PRIVATE;
import static com.bopr.android.smailer.util.TextUtil.commaJoin;
import static com.bopr.android.smailer.util.TextUtil.commaSplit;
import static com.bopr.android.smailer.util.Util.asSet;
import static com.bopr.android.smailer.util.Util.toSet;

/**
 * Settings.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
@SuppressWarnings("WeakerAccess")
public class Settings extends SharedPreferencesWrapper {

    private static final int SETTINGS_VERSION = 2;
    public static final String PREFERENCES_STORAGE_NAME = "com.bopr.android.smailer_preferences";

    public static final String PREF_SYNC_TIME = "sync_time"; /* hidden */
    public static final String PREF_SETTINGS_VERSION = "settings_version";
    public static final String PREF_SENDER_ACCOUNT = "sender_account";
    public static final String PREF_RECIPIENTS_ADDRESS = "recipients_address";
    public static final String PREF_EMAIL_CONTENT = "email_content";
    public static final String PREF_EMAIL_TRIGGERS = "email_triggers";
    public static final String PREF_EMAIL_LOCALE = "email_locale";
    public static final String PREF_NOTIFY_SEND_SUCCESS = "notify_send_success";
    public static final String PREF_RULES = "rules";
    public static final String PREF_HISTORY = "history";
    public static final String PREF_MARK_SMS_AS_READ = "mark_processed_sms_as_read";
    public static final String PREF_RESEND_UNSENT = "resend_unsent"; /* hidden */
    public static final String PREF_FILTER_PHONE_BLACKLIST = "message_filter_blacklist";
    public static final String PREF_FILTER_PHONE_WHITELIST = "message_filter_whitelist";
    public static final String PREF_FILTER_TEXT_BLACKLIST = "message_filter_text_blacklist";
    public static final String PREF_FILTER_TEXT_WHITELIST = "message_filter_text_whitelist";
    public static final String PREF_DEVICE_ALIAS = "device_alias";
    public static final String PREF_REMOTE_CONTROL_ENABLED = "remote_control_enabled";
    public static final String PREF_REMOTE_CONTROL_ACCOUNT = "remote_control_account";
    public static final String PREF_REMOTE_CONTROL_NOTIFICATIONS = "remote_control_notifications";
    public static final String PREF_REMOTE_CONTROL_FILTER_RECIPIENTS = "remote_control_filter_recipients";

    public static final String VAL_PREF_DEFAULT = "default";
    public static final String VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME = "time";
    public static final String VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME_SENT = "time_sent";
    public static final String VAL_PREF_EMAIL_CONTENT_DEVICE_NAME = "device_name";
    public static final String VAL_PREF_EMAIL_CONTENT_LOCATION = "location";
    public static final String VAL_PREF_EMAIL_CONTENT_CONTACT = "contact_name";
    public static final String VAL_PREF_EMAIL_CONTENT_HEADER = "header";
    public static final String VAL_PREF_EMAIL_CONTENT_REMOTE_COMMAND_LINKS = "remote_control_links";
    public static final String VAL_PREF_TRIGGER_IN_SMS = "in_sms";
    public static final String VAL_PREF_TRIGGER_OUT_SMS = "out_sms";
    public static final String VAL_PREF_TRIGGER_IN_CALLS = "in_calls";
    public static final String VAL_PREF_TRIGGER_OUT_CALLS = "out_calls";
    public static final String VAL_PREF_TRIGGER_MISSED_CALLS = "missed_calls";

    public static final Set<String> DEFAULT_CONTENT = asSet(
            VAL_PREF_EMAIL_CONTENT_HEADER,
            VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME,
            VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME_SENT,
            VAL_PREF_EMAIL_CONTENT_DEVICE_NAME,
            VAL_PREF_EMAIL_CONTENT_LOCATION,
            VAL_PREF_EMAIL_CONTENT_CONTACT,
            VAL_PREF_EMAIL_CONTENT_REMOTE_COMMAND_LINKS);
    public static final Set<String> DEFAULT_TRIGGERS = asSet(
            VAL_PREF_TRIGGER_IN_SMS,
            VAL_PREF_TRIGGER_MISSED_CALLS);

    private Context context;

    public Settings(@NonNull Context context) {
        super(context.getSharedPreferences(PREFERENCES_STORAGE_NAME, MODE_PRIVATE));
        this.context = context;
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

        Set<String> content = getStringSet(PREF_EMAIL_CONTENT, null);
        if (content == null) {
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
    public String getReleaseVersion() {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException x) {
            throw new RuntimeException(x);
        }
    }

    @NonNull
    public BuildInfo getReleaseInfo() {
        Properties properties = new Properties();
        try {
            InputStream stream = context.getAssets().open("release.properties");
            properties.load(stream);
            stream.close();
            return new BuildInfo(
                    properties.getProperty("build_number"),
                    properties.getProperty("build_time")
            );
        } catch (IOException x) {
            throw new RuntimeException("Cannot read release properties", x);
        }
    }

    @NonNull
    public PhoneEventFilter getFilter() {
        PhoneEventFilter filter = new PhoneEventFilter();

        filter.setTriggers(getStringSet(PREF_EMAIL_TRIGGERS, Collections.<String>emptySet()));
        filter.setPhoneBlacklist(toSet(commaSplit(getString(PREF_FILTER_PHONE_BLACKLIST, ""))));
        filter.setPhoneWhitelist(toSet(commaSplit(getString(PREF_FILTER_PHONE_WHITELIST, ""))));
        filter.setTextBlacklist(toSet(commaSplit(getString(PREF_FILTER_TEXT_BLACKLIST, ""))));
        filter.setTextWhitelist(toSet(commaSplit(getString(PREF_FILTER_TEXT_WHITELIST, ""))));

        return filter;
    }

    @Override
    public Editor edit() {
        return new Editor(super.edit());
    }

    public class Editor extends EditorWrapper {

        private Editor(EditorWrapper edit) {
            super(edit);
        }

        public Editor putFilter(PhoneEventFilter filter) {
            putString(PREF_FILTER_PHONE_BLACKLIST, commaJoin(filter.getPhoneBlacklist()));
            putString(PREF_FILTER_PHONE_WHITELIST, commaJoin(filter.getPhoneWhitelist()));
            putString(PREF_FILTER_TEXT_BLACKLIST, commaJoin(filter.getTextBlacklist()));
            putString(PREF_FILTER_TEXT_WHITELIST, commaJoin(filter.getTextWhitelist()));
            return this;
        }

    }

    public static class BuildInfo {

        public final String number;
        public final String time;

        public BuildInfo(String number, String time) {
            this.number = number;
            this.time = time;
        }

    }

}
