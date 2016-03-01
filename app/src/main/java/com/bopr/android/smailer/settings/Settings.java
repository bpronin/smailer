package com.bopr.android.smailer.settings;

/**
 * Settings names.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public interface Settings {

    String KEY_PREF_SERVICE_ENABLED = "service_enabled";
    String KEY_PREF_SENDER_ACCOUNT = "sender_account";
    String KEY_PREF_SENDER_PASSWORD = "sender_password";
    String KEY_PREF_EMAIL_PROTOCOL = "sender_protocol";
    String KEY_PREF_EMAIL_HOST = "sender_host";
    String KEY_PREF_EMAIL_PORT = "sender_port";
    String KEY_PREF_RECIPIENT_EMAIL_ADDRESS = "recipient_email_address";

    String DEFAULT_EMAIL_PROTOCOL = "smtp";
    String DEFAULT_EMAIL_HOST = "smtp.gmail.com";
    String DEFAULT_EMAIL_PORT = "465";

}
