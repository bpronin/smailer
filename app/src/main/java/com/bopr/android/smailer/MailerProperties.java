package com.bopr.android.smailer;

import android.content.SharedPreferences;

import static com.bopr.android.smailer.settings.Settings.KEY_PREF_EMAIL_CONTENT_DEVICE_NAME;
import static com.bopr.android.smailer.settings.Settings.KEY_PREF_EMAIL_CONTENT_MESSAGE_TIME;
import static com.bopr.android.smailer.settings.Settings.KEY_PREF_EMAIL_HOST;
import static com.bopr.android.smailer.settings.Settings.KEY_PREF_EMAIL_PORT;
import static com.bopr.android.smailer.settings.Settings.KEY_PREF_RECIPIENT_EMAIL_ADDRESS;
import static com.bopr.android.smailer.settings.Settings.KEY_PREF_SENDER_ACCOUNT;
import static com.bopr.android.smailer.settings.Settings.KEY_PREF_SENDER_PASSWORD;

/**
 * Class MailerProperties.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class MailerProperties {

    private String user;
    private String password;
    private String recipients;
    private String host;
    private String port;
    private boolean contentTime;
    private boolean contentDeviceName;

//    private boolean contentLocation;
//    private boolean contentContactName;

    public MailerProperties() {
    }

    public MailerProperties(SharedPreferences preferences) {
        setUser(preferences.getString(KEY_PREF_SENDER_ACCOUNT, ""));
        setPassword(preferences.getString(KEY_PREF_SENDER_PASSWORD, ""));
        setRecipients(preferences.getString(KEY_PREF_RECIPIENT_EMAIL_ADDRESS, ""));
        setHost(preferences.getString(KEY_PREF_EMAIL_HOST, ""));
        setPort(preferences.getString(KEY_PREF_EMAIL_PORT, ""));
        setContentTime(preferences.getBoolean(KEY_PREF_EMAIL_CONTENT_MESSAGE_TIME, true));
        setContentDeviceName(preferences.getBoolean(KEY_PREF_EMAIL_CONTENT_DEVICE_NAME, true));
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getRecipients() {
        return recipients;
    }

    public void setRecipients(String recipients) {
        this.recipients = recipients;
    }

    public boolean isContentTime() {
        return contentTime;
    }

    public void setContentTime(boolean contentTime) {
        this.contentTime = contentTime;
    }

    public boolean isContentDeviceName() {
        return contentDeviceName;
    }

    public void setContentDeviceName(boolean contentDeviceName) {
        this.contentDeviceName = contentDeviceName;
    }

    @Override
    public String toString() {
        return "MailerProperties{" +
                "user='" + user + '\'' +
                ", password='" + password + '\'' +
                ", recipients='" + recipients + '\'' +
                ", host='" + host + '\'' +
                ", port='" + port + '\'' +
                ", contentTime=" + contentTime +
                ", contentDeviceName=" + contentDeviceName +
                '}';
    }
}
