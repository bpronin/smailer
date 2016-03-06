package com.bopr.android.smailer;

import android.location.Location;

import java.util.Date;

/**
 * Class MailMessage.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class MailMessage {

    private final String phone;
    private final String body;
    private final Location location;
    private final Date time;

    public MailMessage(String phone, String body, long timeInMillis, Location location) {
        this.phone = phone;
        this.body = body;
        this.location = location;
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

    public Location getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return "MailMessage{" +
                "phone='" + phone + '\'' +
                ", body='" + body + '\'' +
                ", location=" + location +
                ", time=" + time +
                '}';
    }
}
