package com.bopr.android.smailer;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import com.bopr.android.smailer.util.Util;
import com.google.api.client.util.Key;

import java.lang.annotation.Retention;

import static com.bopr.android.smailer.Database.COLUMN_DETAILS;
import static com.bopr.android.smailer.Database.COLUMN_END_TIME;
import static com.bopr.android.smailer.Database.COLUMN_IS_INCOMING;
import static com.bopr.android.smailer.Database.COLUMN_IS_MISSED;
import static com.bopr.android.smailer.Database.COLUMN_LOCATION;
import static com.bopr.android.smailer.Database.COLUMN_PHONE;
import static com.bopr.android.smailer.Database.COLUMN_START_TIME;
import static com.bopr.android.smailer.Database.COLUMN_STATE;
import static com.bopr.android.smailer.Database.COLUMN_TEXT;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Represents phone call or SMS event.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
@SuppressWarnings("WeakerAccess")
public class PhoneEvent implements Parcelable {

    @Retention(SOURCE)
    @IntDef({STATE_PENDING, STATE_PROCESSED, STATE_IGNORED})
    @interface EventState {
    }

    public static final int STATE_PENDING = 0;
    public static final int STATE_PROCESSED = 1;
    public static final int STATE_IGNORED = 2;

    private Long id;
    @Key(COLUMN_IS_INCOMING)
    private boolean incoming;
    @Key(COLUMN_IS_MISSED)
    private boolean missed;
    @Key(COLUMN_PHONE)
    private String phone;
    @Key(COLUMN_START_TIME)
    private Long startTime;
    @Key(COLUMN_END_TIME)
    private Long endTime;
    @Key(COLUMN_TEXT)
    private String text;
    @Key(COLUMN_DETAILS)
    private String details;
    @Key(COLUMN_LOCATION)
    private GeoCoordinates location;
    @EventState
    @Key(COLUMN_STATE)
    private int state = STATE_PENDING;
    private boolean read;

    /* Required by Jackson */
    public PhoneEvent() {
    }

    public PhoneEvent(String phone, boolean incoming, Long startTime, Long endTime, boolean missed,
                      String text, GeoCoordinates location, String details, @EventState int state) {
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

    @EventState
    public int getState() {
        return state;
    }

    public void setState(@EventState int state) {
        this.state = state;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean value) {
        this.read = value;
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
        read = in.readByte() != 0;
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
        dest.writeByte((byte) (read ? 1 : 0));
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
