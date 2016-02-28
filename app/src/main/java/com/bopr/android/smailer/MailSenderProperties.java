package com.bopr.android.smailer;

import com.bopr.android.smailer.settings.Settings;

/**
 * Class MailSenderProperties.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class MailSenderProperties {

    private String user;
    private String password;
    private String recipients;
    private String sender;
    private String subject = Settings.DEFAULT_EMAIL_SUBJECT;

    public MailSenderProperties() {
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

    public String getRecipients() {
        return recipients;
    }

    public void setRecipients(String recipients) {
        this.recipients = recipients;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    @Override
    public String toString() {
        return "MailSenderProperties{" +
                "user='" + user + '\'' +
                ", password='" + password + '\'' +
                ", recipients='" + recipients + '\'' +
                ", subject='" + subject + '\'' +
                ", sender='" + sender + '\'' +
                '}';
    }
}
