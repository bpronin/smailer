package com.bopr.android.smailer.sync;

import com.bopr.android.smailer.PhoneEvent;
import com.google.api.client.util.Key;

import java.util.List;
import java.util.Set;

/**
 * Data transfer object for synchronization.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class SyncData {

    @Key("phone_black_list")
    public Set<String> phoneBlacklist;
    @Key("text_black_list")
    public Set<String> textBlacklist;
    @Key("phone_white_list")
    public Set<String> phoneWhitelist;
    @Key("text_white_list")
    public Set<String> textWhitelist;
    @Key("phone_events")
    public List<Event> events;

    public static class Event {

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
        public Double latitude;
        @Key("longitude")
        public Double longitude;
        @PhoneEvent.EventState
        @Key("state")
        public int state;
    }

}
