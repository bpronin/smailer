package com.bopr.android.smailer;

import java.io.File;
import java.util.Collection;

import androidx.annotation.NonNull;

/**
 * Email message.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
@SuppressWarnings("WeakerAccess")
public class MailMessage {

    private String id;
    private String subject;
    private String body;
    private Collection<File> attachment;
    private String recipients;
    private String replyTo;

    public MailMessage() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Collection<File> getAttachment() {
        return attachment;
    }

    public void setAttachment(Collection<File> attachment) {
        this.attachment = attachment;
    }

    public String getRecipients() {
        return recipients;
    }

    public void setRecipients(String recipients) {
        this.recipients = recipients;
    }

    public String getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }

    @Override
    @NonNull
    public String toString() {
        return "MailMessage{" +
                "id='" + id + '\'' +
                ", subject='" + subject + '\'' +
                ", body='" + body + '\'' +
                ", attachment=" + attachment +
                ", recipients='" + recipients + '\'' +
                ", replyTo='" + replyTo + '\'' +
                '}';
    }
}
