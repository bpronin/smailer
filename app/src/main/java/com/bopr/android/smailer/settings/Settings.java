package com.bopr.android.smailer.settings;

/**
 * Settings.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public interface Settings {

    String PREFERENCES_STORAGE_NAME = "com.bopr.android.smailer_preferences";
    String KEY_PREF_SERVICE_ENABLED = "service_enabled";
    String KEY_PREF_SENDER_ACCOUNT = "sender_account";
    String KEY_PREF_SENDER_PASSWORD = "sender_password";
    String KEY_PREF_EMAIL_HOST = "sender_host";
    String KEY_PREF_EMAIL_PORT = "sender_port";
    String KEY_PREF_RECIPIENT_EMAIL_ADDRESS = "recipient_email_address";
    String KEY_PREF_OUTGOING_SERVER = "outgoing_server";
    String KEY_PREF_EMAIL_CONTENT = "email_content";
    String KEY_PREF_EMAIL_SOURCE = "email_source";
    String VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME = "time";
    String VAL_PREF_EMAIL_CONTENT_DEVICE_NAME = "device_name";
    String VAL_PREF_EMAIL_CONTENT_LOCATION = "location";
    String VAL_PREF_EMAIL_CONTENT_CALLER = "contact_name";
    String VAL_PREF_SOURCE_IN_SMS = "in_sms";
    String VAL_PREF_SOURCE_IN_CALLS = "in_calls";
    String VAL_PREF_SOURCE_OUT_CALLS = "out_calls";
    String VAL_PREF_SOURCE_MISSED_CALLS = "missed_calls";

}
