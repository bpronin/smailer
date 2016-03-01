package com.bopr.android.smailer;

/**
 * Class MailMessage.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class MailMessage {

    private final String phone;
    private final String body;

    public MailMessage(String phone, String body) {
        this.phone = phone;
        this.body = body;
    }

    public String getPhone() {
        return phone;
    }

    public String getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "MailMessage{" +
                "phone='" + phone + '\'' +
                ", body='" + body + '\'' +
                '}';
    }
}
