package com.bopr.android.smailer.settings;

/**
 * Settings names.
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
    String KEY_PREF_EMAIL_CONTENT_MESSAGE_TIME = "email_content_message_time";
    String KEY_PREF_EMAIL_CONTENT_DEVICE_NAME = "email_content_device_name";
    String KEY_PREF_EMAIL_CONTENT_LOCATION = "email_content_location";
    String KEY_PREF_EMAIL_CONTENT_CONTACT_NAME = "email_content_contact_name";
    String KEY_PREF_OUTGOING_SERVER = "outgoing_server";
    String KEY_PREF_EMAIL_CONTENT = "email_content";

}
