package com.bopr.android.smailer;

import android.os.Parcel;
import android.os.Parcelable;

import com.bopr.android.smailer.util.Util;

import androidx.annotation.NonNull;

/**
 * Represents phone call or SMS event.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
@SuppressWarnings("WeakerAccess")
public class PhoneEvent implements Parcelable {

    public enum State {
        PENDING,
        PROCESSED,
        IGNORED
    }

    private Long id;
    private boolean incoming;
    private boolean missed;
    private String phone;
    private Long startTime;
    private Long endTime;
    private String text;
    private String details;
    private GeoCoordinates location;
    private State state = State.PENDING;

    public PhoneEvent() {
    }

    public PhoneEvent(String phone, boolean incoming, Long startTime, Long endTime, boolean missed,
                      String text, GeoCoordinates location, String details, State state) {
        this.text = text;
        this.endTime = endTime;
        this.startTime = startTime;
        this.phone = phone;
        this.missed = missed;
        this.incoming = incoming;
        this.location = location;
        this.details = details;
        this.state = state;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isSms() {
        return !Util.isEmpty(text);
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

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    @NonNull
    @Override
    public String toString() {
        return "PhoneEvent{" +
                "id=" + id +
                ", incoming=" + incoming +
                ", missed=" + missed +
                ", phone='" + phone + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", text='" + text + '\'' +
                ", details='" + details + '\'' +
                ", location=" + location +
                ", state=" + state +
                '}';
    }

    /* Generated Parcelable stuff implementation */

    protected PhoneEvent(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readLong();
        }
        incoming = in.readByte() != 0;
        missed = in.readByte() != 0;
        phone = in.readString();
        if (in.readByte() == 0) {
            startTime = null;
        } else {
            startTime = in.readLong();
        }
        if (in.readByte() == 0) {
            endTime = null;
        } else {
            endTime = in.readLong();
        }
        text = in.readString();
        details = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(id);
        }
        dest.writeByte((byte) (incoming ? 1 : 0));
        dest.writeByte((byte) (missed ? 1 : 0));
        dest.writeString(phone);
        if (startTime == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(startTime);
        }
        if (endTime == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(endTime);
        }
        dest.writeString(text);
        dest.writeString(details);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<PhoneEvent> CREATOR = new Creator<PhoneEvent>() {

        @Override
        public PhoneEvent createFromParcel(Parcel in) {
            return new PhoneEvent(in);
        }

        @Override
        public PhoneEvent[] newArray(int size) {
            return new PhoneEvent[size];
        }
    };
}
