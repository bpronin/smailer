package com.bopr.android.smailer;

import android.content.SharedPreferences;

import java.util.Set;

import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_CONTENT;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_HOST;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_LOCALE;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_PORT;
import static com.bopr.android.smailer.Settings.KEY_PREF_RECIPIENTS_ADDRESS;
import static com.bopr.android.smailer.Settings.KEY_PREF_SENDER_ACCOUNT;
import static com.bopr.android.smailer.Settings.KEY_PREF_SENDER_PASSWORD;

/**
 * {@link Mailer} properties.
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
    private String messageLocale;

    public MailerProperties(SharedPreferences preferences) {
        setUser(preferences.getString(KEY_PREF_SENDER_ACCOUNT, ""));
        setPassword(preferences.getString(KEY_PREF_SENDER_PASSWORD, ""));
        setRecipients(preferences.getString(KEY_PREF_RECIPIENTS_ADDRESS, ""));
        setHost(preferences.getString(KEY_PREF_EMAIL_HOST, ""));
        setPort(preferences.getString(KEY_PREF_EMAIL_PORT, ""));
        setContentOptions(preferences.getStringSet(KEY_PREF_EMAIL_CONTENT, null));
        setMessageLocale(preferences.getString(KEY_PREF_EMAIL_LOCALE, null));
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

    public void setContentOptions(Set<String> contentOptions) {
        this.contentOptions = contentOptions;
    }

    public String getMessageLocale() {
        return messageLocale;
    }

    public void setMessageLocale(String messageLocale) {
        this.messageLocale = messageLocale;
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
                ", messageLocale='" + messageLocale + '\'' +
                '}';
    }
}
