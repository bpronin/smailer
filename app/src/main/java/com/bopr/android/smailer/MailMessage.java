package com.bopr.android.smailer;

/**
 * Class MailMessage.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class MailMessage {

    private final String sender;
    private final String body;

    public MailMessage(String sender, String body) {
        this.sender = sender;
        this.body = body;
    }

    public String getSender() {
        return sender;
    }

    public String getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "MailMessage{" +
                "sender='" + sender + '\'' +
                ", body='" + body + '\'' +
                '}';
    }
}
