package com.bopr.android.smailer;

/**
 * Email message.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
@SuppressWarnings("WeakerAccess")
public class PhoneEvent {

    private Long id;
    private boolean processed;
    private boolean incoming;
    private boolean missed;
    private String phone;
    private Long startTime;
    private Long endTime;
    private String text;
    private String details;
    private GeoCoordinates location;

    public PhoneEvent() {
    }

    public PhoneEvent(String phone, boolean incoming, Long startTime, Long endTime, boolean missed,
                      String text, GeoCoordinates location, boolean processed,
                      String details) {
        this.text = text;
        this.endTime = endTime;
        this.startTime = startTime;
        this.phone = phone;
        this.missed = missed;
        this.incoming = incoming;
        this.location = location;
        this.processed = processed;
        this.details = details;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isSms() {
        return text != null && !text.isEmpty();
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

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getCallDuration() {
        if (startTime != null && endTime != null) {
            return endTime - startTime;
        }
        return 0;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getDetails() {
        return details;
    }

    public GeoCoordinates getLocation() {
        return location;
    }

    public void setLocation(GeoCoordinates location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "PhoneEvent{" +
                "id=" + id +
                ", sent=" + processed +
                ", incoming=" + incoming +
                ", missed=" + missed +
                ", phone='" + phone + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", text='" + text + '\'' +
                ", details='" + details + '\'' +
                ", location=" + location +
                '}';
    }

}
