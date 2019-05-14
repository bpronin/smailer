package com.bopr.android.smailer.sync;

import com.bopr.android.smailer.PhoneEvent;
import com.google.api.client.util.Key;

import java.util.List;
import java.util.Set;

/**
 * Synchronization data transfer object.
 */
@SuppressWarnings("WeakerAccess")
public class SyncDto {

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

}
