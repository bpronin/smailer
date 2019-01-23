package com.bopr.android.smailer;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;

import com.bopr.android.smailer.util.AndroidUtil;
import com.bopr.android.smailer.util.Util;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static android.content.Context.MODE_PRIVATE;
import static com.bopr.android.smailer.util.Util.commaSeparated;
import static com.bopr.android.smailer.util.Util.parseCommaSeparatedSet;

/**
 * Settings.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
@SuppressWarnings("WeakerAccess")
public class Settings {

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
    public static final String KEY_PREF_FILTER_USE_WHITE_LIST = "message_filter_use_white_list";
    public static final String KEY_PREF_FILTER_BLACKLIST = "message_filter_blacklist";
    public static final String KEY_PREF_FILTER_WHITELIST = "message_filter_whitelist";
    public static final String KEY_PREF_FILTER_TEXT_USE_WHITE_LIST = "message_filter_text_use_white_list";
    public static final String KEY_PREF_FILTER_TEXT_BLACKLIST = "message_filter_text_blacklist";
    public static final String KEY_PREF_FILTER_TEXT_WHITELIST = "message_filter_text_whitelist";
    public static final String KEY_PREF_DEVICE_ALIAS = "device_alias";

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
    public static final Set<String> DEFAULT_CONTENT = Util.asSet(
            VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME,
            VAL_PREF_EMAIL_CONTENT_DEVICE_NAME,
            VAL_PREF_EMAIL_CONTENT_LOCATION,
            VAL_PREF_EMAIL_CONTENT_CONTACT);
    public static final Set<String> DEFAULT_TRIGGERS = Util.asSet(
            VAL_PREF_TRIGGER_IN_SMS,
            VAL_PREF_TRIGGER_MISSED_CALLS);

    public static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREFERENCES_STORAGE_NAME, MODE_PRIVATE);
    }

    /**
     * Loads default preferences values.
     */
    public static void loadDefaultPreferences(Context context) {
        Map<String, Object> data = new HashMap<>();
        data.put(KEY_PREF_EMAIL_HOST, DEFAULT_HOST);
        data.put(KEY_PREF_EMAIL_PORT, DEFAULT_PORT);
        data.put(KEY_PREF_EMAIL_TRIGGERS, DEFAULT_TRIGGERS);
        data.put(KEY_PREF_EMAIL_CONTENT, DEFAULT_CONTENT);
        data.put(KEY_PREF_EMAIL_LOCALE, DEFAULT_LOCALE);
        data.put(KEY_PREF_RESEND_UNSENT, true);
        data.put(KEY_PREF_FILTER_USE_WHITE_LIST, false);
        data.put(KEY_PREF_FILTER_TEXT_USE_WHITE_LIST, false);
        data.put(KEY_PREF_MARK_SMS_AS_READ, false);

        AndroidUtil.putPreferencesOptional(getPreferences(context), data);
    }

    /**
     * Returns device name.
     */
    public static String getDeviceName(Context context) {
        String name = getPreferences(context).getString(KEY_PREF_DEVICE_ALIAS, "");
        if (!Util.isEmpty(name)) {
            return name;
        }
        return AndroidUtil.getDeviceName();
    }

    public static String getReleaseVersion(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException x) {
            throw new Error(x);
        }
    }

    public static BuildInfo getReleaseInfo(Context context) {
        Properties properties = new Properties();
        try {
            properties.load(context.getAssets().open("release.properties"));
            return new BuildInfo(
                    properties.getProperty("build_number"),
                    properties.getProperty("build_time")
            );
        } catch (IOException x) {
            throw new Error("Cannot read release properties", x);
        }
    }

    public static void saveFilter(Context context, PhoneEventFilter filter) {
        SharedPreferences.Editor editor = getPreferences(context).edit();

        editor.putBoolean(KEY_PREF_FILTER_USE_WHITE_LIST, filter.isUsePhoneWhitelist());
        editor.putString(KEY_PREF_FILTER_BLACKLIST, commaSeparated(filter.getPhoneBlacklist()));
        editor.putString(KEY_PREF_FILTER_WHITELIST, commaSeparated(filter.getPhoneWhitelist()));
        editor.putBoolean(KEY_PREF_FILTER_TEXT_USE_WHITE_LIST, filter.isUseTextWhitelist());
        editor.putString(KEY_PREF_FILTER_TEXT_BLACKLIST, commaSeparated(filter.getTextBlacklist()));
        editor.putString(KEY_PREF_FILTER_TEXT_WHITELIST, commaSeparated(filter.getTextWhitelist()));

        editor.apply();
    }

    @NonNull
    public static PhoneEventFilter loadFilter(Context context) {
        SharedPreferences preferences = getPreferences(context);
        PhoneEventFilter filter = new PhoneEventFilter();

        filter.setTriggers(preferences.getStringSet(KEY_PREF_EMAIL_TRIGGERS, Collections.<String>emptySet()));
        filter.setUsePhoneWhitelist(preferences.getBoolean(KEY_PREF_FILTER_USE_WHITE_LIST, true));
        filter.setPhoneBlacklist(parseCommaSeparatedSet(preferences.getString(KEY_PREF_FILTER_BLACKLIST, "")));
        filter.setPhoneWhitelist(parseCommaSeparatedSet(preferences.getString(KEY_PREF_FILTER_WHITELIST, "")));
        filter.setUseTextWhitelist(preferences.getBoolean(KEY_PREF_FILTER_TEXT_USE_WHITE_LIST, true));
        filter.setTextBlacklist(parseCommaSeparatedSet(preferences.getString(KEY_PREF_FILTER_TEXT_BLACKLIST, "")));
        filter.setTextWhitelist(parseCommaSeparatedSet(preferences.getString(KEY_PREF_FILTER_TEXT_WHITELIST, "")));

        return filter;
    }

/*
    public static String getAndroidId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }
*/

/*
    public static String getDeviceUuid(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        String deviceId = tm.getDeviceId();
        String serialNumber = tm.getSimSerialNumber();
        String androidId = getAndroidId(context);

        UUID uuid = new UUID(androidId.hashCode(), (long) deviceId.hashCode() << 32 | serialNumber.hashCode());
        return uuid.toString();
    }
*/

    public static class BuildInfo {

        public final String number;
        public final String time;

        public BuildInfo(String number, String time) {
            this.number = number;
            this.time = time;
        }

    }

}
