package com.bopr.android.smailer;

import android.content.SharedPreferences;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_HOST;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_PORT;
import static com.bopr.android.smailer.Settings.KEY_PREF_RECIPIENT_EMAIL_ADDRESS;
import static com.bopr.android.smailer.Settings.KEY_PREF_SENDER_ACCOUNT;
import static com.bopr.android.smailer.Settings.KEY_PREF_SENDER_PASSWORD;

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
    private Set<String> contentOptions;

    public MailerProperties() {
    }

    public MailerProperties(SharedPreferences preferences) {
        setUser(preferences.getString(KEY_PREF_SENDER_ACCOUNT, ""));
        setPassword(preferences.getString(KEY_PREF_SENDER_PASSWORD, ""));
        setRecipients(preferences.getString(KEY_PREF_RECIPIENT_EMAIL_ADDRESS, ""));
        setHost(preferences.getString(KEY_PREF_EMAIL_HOST, ""));
        setPort(preferences.getString(KEY_PREF_EMAIL_PORT, ""));
        setContentOptions(preferences.getStringSet(Settings.KEY_PREF_EMAIL_CONTENT, null));
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

    public Set<String> getContentOptions() {
        return contentOptions;
    }

    public void setContentOptions(String... options) {
        setContentOptions(new HashSet<>(Arrays.asList(options)));
    }

    public void setContentOptions(Set<String> contentOptions) {
        this.contentOptions = contentOptions;
    }

    @Override
    public String toString() {
        return "MailerProperties{" +
                "user='" + user + '\'' +
                ", password='" + password + '\'' +
                ", recipients='" + recipients + '\'' +
                ", host='" + host + '\'' +
                ", port='" + port + '\'' +
                ", contentOptions=" + contentOptions +
                '}';
    }
}
