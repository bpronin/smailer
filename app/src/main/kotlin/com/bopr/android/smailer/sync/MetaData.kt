package com.bopr.android.smailer.sync

import com.google.api.client.util.Key

/**
 * Data transfer object for synchronization metadata.
 */
/* NOTE: JSON engine requires that all DTO constructor parameters be var and have default value */
data class MetaData(
        @Key("sync_time") var time: Long = 0
)