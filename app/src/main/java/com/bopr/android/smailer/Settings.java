package com.bopr.android.smailer;

import android.content.Context;
import android.content.pm.PackageManager;

import com.bopr.android.smailer.util.AndroidUtil;
import com.bopr.android.smailer.util.SharedPreferencesWrapper;
import com.bopr.android.smailer.util.Util;

import java.io.IOException;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;

import androidx.annotation.NonNull;

import static android.content.Context.MODE_PRIVATE;
import static com.bopr.android.smailer.util.Util.asSet;
import static com.bopr.android.smailer.util.Util.commaSeparated;
import static com.bopr.android.smailer.util.Util.parseCommaSeparated;
import static com.bopr.android.smailer.util.Util.toSet;

/**
 * Settings.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
@SuppressWarnings("WeakerAccess")
public class Settings extends SharedPreferencesWrapper {

    public static final String PREFERENCES_STORAGE_NAME = "com.bopr.android.smailer_preferences";
    public static final String DB_NAME = "smailer.sqlite";

    public static final String KEY_PREF_SENDER_ACCOUNT = "sender_account";
    public static final String KEY_PREF_SENDER_PASSWORD = "sender_password";
    public static final String KEY_PREF_EMAIL_HOST = "sender_host";
    public static final String KEY_PREF_EMAIL_PORT = "sender_port";
    public static final String KEY_PREF_RECIPIENTS_ADDRESS = "recipients_address";
    public static final String KEY_PREF_OUTGOING_SERVER = "outgoing_server";
    public static final String KEY_PREF_EMAIL_CONTENT = "email_content";
    public static final String KEY_PREF_EMAIL_TRIGGERS = "email_triggers";
    public static final String KEY_PREF_EMAIL_LOCALE = "email_locale";
    public static final String KEY_PREF_NOTIFY_SEND_SUCCESS = "notify_send_success";
    public static final String KEY_PREF_MORE = "more";
    public static final String KEY_PREF_RULES = "rules";
    public static final String KEY_PREF_LOG = "log";
    public static final String KEY_PREF_MARK_SMS_AS_READ = "mark_processed_sms_as_read";
    public static final String KEY_PREF_RESEND_UNSENT = "resend_unsent";
    public static final String KEY_PREF_FILTER_BLACKLIST = "message_filter_blacklist";
    public static final String KEY_PREF_FILTER_WHITELIST = "message_filter_whitelist";
    public static final String KEY_PREF_FILTER_TEXT_BLACKLIST = "message_filter_text_blacklist";
    public static final String KEY_PREF_FILTER_TEXT_WHITELIST = "message_filter_text_whitelist";
    public static final String KEY_PREF_DEVICE_ALIAS = "device_alias";
    public static final String KEY_PREF_FIREBASE_TOKEN = "firebase_token";

    public static final String VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME = "time";
    public static final String VAL_PREF_EMAIL_CONTENT_DEVICE_NAME = "device_name";
    public static final String VAL_PREF_EMAIL_CONTENT_LOCATION = "location";
    public static final String VAL_PREF_EMAIL_CONTENT_CONTACT = "contact_name";
    public static final String VAL_PREF_TRIGGER_IN_SMS = "in_sms";
    public static final String VAL_PREF_TRIGGER_OUT_SMS = "out_sms";
    public static final String VAL_PREF_TRIGGER_IN_CALLS = "in_calls";
    public static final String VAL_PREF_TRIGGER_OUT_CALLS = "out_calls";
    public static final String VAL_PREF_TRIGGER_MISSED_CALLS = "missed_calls";

    public static final String DEFAULT_HOST = "smtp.gmail.com";
    public static final String DEFAULT_PORT = "465";
    public static final String DEFAULT_LOCALE = "default";
    public static final Set<String> DEFAULT_CONTENT = asSet(
            VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME,
            VAL_PREF_EMAIL_CONTENT_DEVICE_NAME,
            VAL_PREF_EMAIL_CONTENT_LOCATION,
            VAL_PREF_EMAIL_CONTENT_CONTACT);
    public static final Set<String> DEFAULT_TRIGGERS = asSet(
            VAL_PREF_TRIGGER_IN_SMS,
            VAL_PREF_TRIGGER_MISSED_CALLS);

    private Context context;

    public Settings(Context context) {
        super(context.getSharedPreferences(PREFERENCES_STORAGE_NAME, MODE_PRIVATE));
        this.context = context;
    }

    /**
     * Loads default settings values.
     */
    public static void putDefaults(Context context) {
        new Settings(context).edit()
                .putStringOptional(KEY_PREF_EMAIL_HOST, DEFAULT_HOST)
                .putStringOptional(KEY_PREF_EMAIL_PORT, DEFAULT_PORT)
                .putStringSetOptional(KEY_PREF_EMAIL_TRIGGERS, DEFAULT_TRIGGERS)
                .putStringSetOptional(KEY_PREF_EMAIL_CONTENT, DEFAULT_CONTENT)
                .putStringOptional(KEY_PREF_EMAIL_LOCALE, DEFAULT_LOCALE)
                .putBooleanOptional(KEY_PREF_RESEND_UNSENT, true)
                .putBooleanOptional(KEY_PREF_MARK_SMS_AS_READ, false)
                .apply();
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
            properties.load(context.getAssets().open("release.properties"));
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
        filter.setPhoneBlacklist(toSet(parseCommaSeparated(getString(KEY_PREF_FILTER_BLACKLIST, ""))));
        filter.setPhoneWhitelist(toSet(parseCommaSeparated(getString(KEY_PREF_FILTER_WHITELIST, ""))));
        filter.setTextBlacklist(toSet(parseCommaSeparated(getString(KEY_PREF_FILTER_TEXT_BLACKLIST, ""))));
        filter.setTextWhitelist(toSet(parseCommaSeparated(getString(KEY_PREF_FILTER_TEXT_WHITELIST, ""))));

        return filter;
    }

    public void putFilter(PhoneEventFilter filter) {
        edit().putString(KEY_PREF_FILTER_BLACKLIST, commaSeparated(filter.getPhoneBlacklist()))
                .putString(KEY_PREF_FILTER_WHITELIST, commaSeparated(filter.getPhoneWhitelist()))
                .putString(KEY_PREF_FILTER_TEXT_BLACKLIST, commaSeparated(filter.getTextBlacklist()))
                .putString(KEY_PREF_FILTER_TEXT_WHITELIST, commaSeparated(filter.getTextWhitelist()))
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
