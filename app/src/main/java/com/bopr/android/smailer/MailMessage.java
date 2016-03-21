package com.bopr.android.smailer;

import android.location.Location;

/**
 * Class MailMessage.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class MailMessage {

    private long id = -1;
    private boolean sent;
    private boolean sms;
    private boolean incoming;
    private boolean missed;
    private String phone;
    private long startTime;
    private long endTime;
    private String text;
    private Double latitude;
    private Double longitude;

    public MailMessage() {
    }

    public MailMessage(String phone, boolean incoming, long startTime, long endTime, boolean missed,
                       boolean sms, String text, double latitude, double longitude, boolean sent) {
        this.text = text;
        this.latitude = latitude;
        this.longitude = longitude;
        this.endTime = endTime;
        this.startTime = startTime;
        this.phone = phone;
        this.missed = missed;
        this.incoming = incoming;
        this.sms = sms;
        this.sent = sent;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setLocation(Location location) {
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
        return endTime - startTime;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    public boolean isSent() {
        return sent;
    }

    @Override
    public String toString() {
        return "MailMessage{" +
                "id=" + id +
                ", sms=" + sms +
                ", incoming=" + incoming +
                ", missed=" + missed +
                ", phone='" + phone + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", text='" + text + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", sent=" + sent +
                '}';
    }
}
