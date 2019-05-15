package com.bopr.android.smailer.sync;

import com.bopr.android.smailer.PhoneEvent;
import com.google.api.client.util.Key;

import java.util.List;
import java.util.Set;

/**
 * Data transfer object for synchronization.
 */
@SuppressWarnings("WeakerAccess")
public class SyncData {

    @Key("time")
    public long time;
    @Key("phone_black_list")
    public Set<String> phoneBlacklist;
    @Key("text_black_list")
    public Set<String> textBlacklist;
    @Key("phone_white_list")
    public Set<String> phoneWhitelist;
    @Key("text_white_list")
    public Set<String> textWhitelist;
    @Key("phone_events")
    public List<PhoneEvent> events;

/*
    public static class Event{
        
        @Key("is_incoming")
        public boolean incoming;
        @Key("is_missed")
        public boolean missed;
        @Key("phone")
        public String phone;
        @Key("recipient")
        public String recipient;
        @Key("start_time")
        public long startTime;
        @Key("end_time")
        public Long endTime;
        @Key("message_text")
        public String text;
        @Key("details")
        public String details;
        @Key("latitude")
        public double latitude;
        @Key("longitude")
        public double longitude;
        @PhoneEvent.EventState
        @Key("state")
        public int state;
    }

    private Event serializeEvent(PhoneEvent event) {
        Event data = new Event();
        data.state = event.getState();
        data.phone = event.getPhone();
        data.text = event.getText();
        data.incoming = event.isIncoming();
        data.missed = event.isMissed();
        data.details = event.getDetails();
        data.startTime = event.getStartTime();
        data.endTime = event.getEndTime();
        data.recipient = event.getRecipient();
        data.latitude = event.getLocation().getLatitude();
        data.longitude = event.getLocation().getLongitude();
        return data;
    }

    private PhoneEvent deserializeEvent(Event data) {
        PhoneEvent event = new PhoneEvent();
        event.setState(data.state);
        event.setPhone(data.phone);
        event.setText(data.text);
        event.setIncoming(data.incoming);
        event.setMissed(data.missed);
        event.setStartTime(data.startTime);
        event.setEndTime(data.endTime);
        event.setDetails(data.details);
        event.setRecipient(data.recipient);
        event.setLocation(new GeoCoordinates(data.latitude, data.longitude));
        return event;
    }
*/
}
