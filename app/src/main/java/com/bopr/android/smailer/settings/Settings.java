package com.bopr.android.smailer.settings;

/**
 * Settings names.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public interface Settings {

    String KEY_PREF_SERVICE_ENABLED = "service_enabled";
    String KEY_PREF_SENDER_EMAIL_ADDRESS = "sender_email_address";
    String KEY_PREF_SENDER_EMAIL_PASSWORD = "sender_email_password";
    String KEY_PREF_RECIPIENT_EMAIL_ADDRESS = "recipient_email_address";
    String KEY_PREF_SENDER_NAME = "sender_name";
    String KEY_PREF_EMAIL_SUBJECT = "email_subject";

    String DEFAULT_EMAIL_SUBJECT = "[SMailer]";

}
