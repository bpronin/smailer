package com.bopr.android.smailer;

import android.location.Location;

import java.util.Date;

/**
 * Class MailMessage.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class MailMessage {

    private boolean sms;
    private boolean incoming;
    private boolean missed;
    private String phone;
    private Location location;
    private Date startTime;
    private Date endTime;
    private String body;

    public MailMessage(String phone, boolean incoming, long startTime, long endTime,
                       boolean missed, boolean sms, String body, Location location) {
        this.body = body;
        this.endTime = new Date(endTime);
        this.startTime = new Date(startTime);
        this.location = location;
        this.phone = phone;
        this.missed = missed;
        this.incoming = incoming;
        this.sms = sms;
    }

    public boolean isSms() {
        return sms;
    }

    public boolean isIncoming() {
        return incoming;
    }

    public boolean isMissed() {
        return missed;
    }

    public String getPhone() {
        return phone;
    }

    public Location getLocation() {
        return location;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public long getCallDuration() {
        if (startTime != null && endTime != null) {
            return endTime.getTime() - startTime.getTime();
        } else {
            return 0;
        }
    }

    public String getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "MailMessage{" +
                "sms=" + sms +
                ", incoming=" + incoming +
                ", missed=" + missed +
                ", phone='" + phone + '\'' +
                ", location=" + location +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", body='" + body + '\'' +
                '}';
    }

}
