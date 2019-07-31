package com.bopr.android.smailer.sync;

import com.google.api.client.util.Key;

/**
 * Data transfer object for synchronization metadata.
 */
@SuppressWarnings("WeakerAccess")
public class MetaData {

    @Key("sync_time")
    public long syncTime;
}
