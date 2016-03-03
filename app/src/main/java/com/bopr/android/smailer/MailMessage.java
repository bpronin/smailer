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
    private final Date time;

    public MailMessage(String phone, String body, long timeInMillis) {
        this.phone = phone;
        this.body = body;
        this.time = new Date(timeInMillis);
    }

    public String getPhone() {
        return phone;
    }

    public String getBody() {
        return body;
    }

    public Date getTime() {
        return time;
    }

    @Override
    public String toString() {
        return "MailMessage{" +
                "phone='" + phone + '\'' +
                ", body='" + body + '\'' +
                ", time=" + time +
                '}';
    }
}
