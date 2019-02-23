package com.bopr.android.smailer;

import androidx.annotation.NonNull;

/**
 * Email message.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
class MailMessage {

    final String id;
    final String subject;
    final String body;

    MailMessage(String id, String subject, String body) {
        this.id = id;
        this.subject = subject;
        this.body = body;
    }

    @Override
    @NonNull
    public String toString() {
        return "MailMessage{" +
                "id='" + id + '\'' +
                ", subject='" + subject + '\'' +
                ", body='" + body + '\'' +
                '}';
    }
}
