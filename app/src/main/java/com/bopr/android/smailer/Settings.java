package com.bopr.android.smailer;

import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;

import com.bopr.android.smailer.util.AndroidUtil;
import com.bopr.android.smailer.util.SharedPreferencesWrapper;
import com.bopr.android.smailer.util.Util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;

import static android.content.Context.MODE_PRIVATE;
import static com.bopr.android.smailer.util.Util.asSet;
import static com.bopr.android.smailer.util.Util.commaJoin;
import static com.bopr.android.smailer.util.Util.commaSplit;
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

    public static final String KEY_SYNC_TIME = "sync_time"; /* hidden */
    public static final String KEY_SETTINGS_VERSION = "settings_version";
    public static final String KEY_PREF_SENDER_ACCOUNT = "sender_account";
    public static final String KEY_PREF_RECIPIENTS_ADDRESS = "recipients_address";
    public static final String KEY_PREF_OUTGOING_SERVER = "outgoing_server";
    public static final String KEY_PREF_EMAIL_CONTENT = "email_content";
    public static final String KEY_PREF_EMAIL_TRIGGERS = "email_triggers";
    public static final String KEY_PREF_EMAIL_LOCALE = "email_locale";
    public static final String KEY_PREF_NOTIFY_SEND_SUCCESS = "notify_send_success";
    public static final String KEY_PREF_OPTIONS = "options";
    public static final String KEY_PREF_RULES = "rules";
    public static final String KEY_PREF_HISTORY = "history";
    public static final String KEY_PREF_MARK_SMS_AS_READ = "mark_processed_sms_as_read";
    public static final String KEY_PREF_RESEND_UNSENT = "resend_unsent"; /* hidden */
    public static final String KEY_PREF_FILTER_BLACKLIST = "message_filter_blacklist";
    public static final String KEY_PREF_FILTER_WHITELIST = "message_filter_whitelist";
    public static final String KEY_PREF_FILTER_TEXT_BLACKLIST = "message_filter_text_blacklist";
    public static final String KEY_PREF_FILTER_TEXT_WHITELIST = "message_filter_text_whitelist";
    public static final String KEY_PREF_DEVICE_ALIAS = "device_alias";
    public static final String KEY_PREF_REMOTE_CONTROL = "remote_control";
    public static final String KEY_PREF_REMOTE_CONTROL_ENABLED = "remote_control_enabled";
    public static final String KEY_PREF_REMOTE_CONTROL_ACCOUNT = "remote_control_account";
    public static final String KEY_PREF_REMOTE_CONTROL_NOTIFICATIONS = "remote_control_notifications";
    public static final String KEY_PREF_REMOTE_CONTROL_FILTER_RECIPIENTS = "remote_control_filter_recipients";
    public static final String KEY_PREF_SYNC_ITEMS = "sync_items";

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
    public static final String VAL_PREF_SYNC_EVENTS = "sync_events";
    public static final String VAL_PREF_SYNC_FILTER_LISTS = "sync_filter_lists";

    public static final String DEFAULT_LOCALE = "default";
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
    public static void init(Context context) {
        Settings settings = new Settings(context);
        EditorWrapper edit = settings.edit();

        edit.putStringSetOptional(KEY_PREF_EMAIL_TRIGGERS, DEFAULT_TRIGGERS);
        edit.putStringOptional(KEY_PREF_EMAIL_LOCALE, DEFAULT_LOCALE);
        edit.putBooleanOptional(KEY_PREF_RESEND_UNSENT, true);
        edit.putBooleanOptional(KEY_PREF_MARK_SMS_AS_READ, false);
        edit.putBooleanOptional(KEY_PREF_REMOTE_CONTROL_ENABLED, false);
        edit.putBooleanOptional(KEY_PREF_REMOTE_CONTROL_NOTIFICATIONS, true);
        edit.putBooleanOptional(KEY_PREF_REMOTE_CONTROL_FILTER_RECIPIENTS, true);

        Set<String> content = settings.getStringSet(KEY_PREF_EMAIL_CONTENT, null);
        if (content == null) {
            edit.putStringSet(KEY_PREF_EMAIL_CONTENT, DEFAULT_CONTENT);
        } else if (settings.getInt(Settings.KEY_SETTINGS_VERSION, 1) == 1) {
            content.add(VAL_PREF_EMAIL_CONTENT_HEADER);
            content.add(VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME_SENT);
            edit.putStringSet(KEY_PREF_EMAIL_CONTENT, content);
        }
        edit.putStringSetOptional(KEY_PREF_SYNC_ITEMS, asSet(VAL_PREF_SYNC_EVENTS,
                VAL_PREF_SYNC_FILTER_LISTS));

        edit.putInt(KEY_SETTINGS_VERSION, SETTINGS_VERSION);
        edit.apply();
    }

    /**
     * Returns device name.
     */
    public String getDeviceName() {
        String name = getString(KEY_PREF_DEVICE_ALIAS, "");
        if (!Util.isEmpty(name)) {
            return name;
        }
        return AndroidUtil.getDeviceName();
    }

    public String getReleaseVersion() {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException x) {
            throw new RuntimeException(x);
        }
    }

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

        filter.setTriggers(getStringSet(KEY_PREF_EMAIL_TRIGGERS, Collections.<String>emptySet()));
        filter.setPhoneBlacklist(toSet(commaSplit(getString(KEY_PREF_FILTER_BLACKLIST, ""))));
        filter.setPhoneWhitelist(toSet(commaSplit(getString(KEY_PREF_FILTER_WHITELIST, ""))));
        filter.setTextBlacklist(toSet(commaSplit(getString(KEY_PREF_FILTER_TEXT_BLACKLIST, ""))));
        filter.setTextWhitelist(toSet(commaSplit(getString(KEY_PREF_FILTER_TEXT_WHITELIST, ""))));

        return filter;
    }

    public void putFilter(PhoneEventFilter filter) {
        edit().putString(KEY_PREF_FILTER_BLACKLIST, commaJoin(filter.getPhoneBlacklist()))
                .putString(KEY_PREF_FILTER_WHITELIST, commaJoin(filter.getPhoneWhitelist()))
                .putString(KEY_PREF_FILTER_TEXT_BLACKLIST, commaJoin(filter.getTextBlacklist()))
                .putString(KEY_PREF_FILTER_TEXT_WHITELIST, commaJoin(filter.getTextWhitelist()))
                .apply();
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
