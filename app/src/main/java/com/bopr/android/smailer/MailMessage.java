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
    private Date startTime;
    private Date endTime;
    private String body;
    private Double latitude;
    private Double longitude;

    public MailMessage(String phone, boolean incoming, long startTime, long endTime, boolean missed, boolean sms,
                       String body, double latitude, double longitude
    ) {
        this.body = body;
        this.latitude = latitude;
        this.longitude = longitude;
        this.endTime = new Date(endTime);
        this.startTime = new Date(startTime);
        this.phone = phone;
        this.missed = missed;
        this.incoming = incoming;
        this.sms = sms;
    }

    public MailMessage(String phone, boolean incoming, long startTime, long endTime, boolean missed, boolean sms,
                       String body, Location location
    ) {
        this.body = body;
        this.endTime = new Date(endTime);
        this.startTime = new Date(startTime);
        this.phone = phone;
        this.missed = missed;
        this.incoming = incoming;
        this.sms = sms;
        setLocation(location);
    }

    public boolean isSms() {
        return sms;
    }

    public void setSms(boolean sms) {
        this.sms = sms;
    }

    public boolean isIncoming() {
        return incoming;
    }

    public void setIncoming(boolean incoming) {
        this.incoming = incoming;
    }

    public boolean isMissed() {
        return missed;
    }

    public void setMissed(boolean missed) {
        this.missed = missed;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    private void setLocation(Location location) {
        if (location != null) {
            this.latitude = location.getLatitude();
            this.longitude = location.getLongitude();
        }
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public long getCallDuration() {
        if (startTime != null && endTime != null) {
            return endTime.getTime() - startTime.getTime();
        }
        return 0;
    }

    @Override
    public String toString() {
        return "MailMessage{" +
                "sms=" + sms +
                ", incoming=" + incoming +
                ", missed=" + missed +
                ", phone='" + phone + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", body='" + body + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
