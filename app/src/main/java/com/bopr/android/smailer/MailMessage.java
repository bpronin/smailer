package com.bopr.android.smailer;

import java.util.Date;

/**
 * Class MailMessage.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class MailMessage {

    private final String phone;
    private final String body;
    private final Date date;

    public MailMessage(String phone, String body, Date date) {
        this.phone = phone;
        this.body = body;
        this.date = date;
    }

    public String getPhone() {
        return phone;
    }

    public String getBody() {
        return body;
    }

    public Date getDate() {
        return date;
    }

    @Override
    public String toString() {
        return "MailMessage{" +
                "phone='" + phone + '\'' +
                ", body='" + body + '\'' +
                ", date=" + date +
                '}';
    }
}
