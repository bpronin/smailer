package com.bopr.android.smailer.sync

import com.bopr.android.smailer.PhoneEvent.Companion.STATE_PENDING
import com.bopr.android.smailer.PhoneEvent.EventState
import com.google.api.client.util.Key

/**
 * Data transfer object for synchronization.
 */
/* NOTE: JSON engine requires that all DTO constructor parameters be var and have default value */
data class SyncData(
        @Key("phone_black_list") var phoneBlacklist: MutableSet<String>? = null,
        @Key("text_black_list") var textBlacklist: MutableSet<String>? = null,
        @Key("phone_white_list") var phoneWhitelist: MutableSet<String>? = null,
        @Key("text_white_list") var textWhitelist: MutableSet<String>? = null,
        @Key("phone_events") var events: List<Event>? = null) {

    data class Event(
            @Key("is_incoming") var incoming: Boolean = false,
            @Key("is_missed") var missed: Boolean = false,
            @Key("phone") var phone: String = "",
            @Key("recipient") var recipient: String = "",
            @Key("start_time") var startTime: Long = 0,
            @Key("end_time") var endTime: Long? = null,
            @Key("message_text") var text: String? = null,
            @Key("details") var details: String? = null,
            @Key("latitude") var latitude: Double? = null,
            @Key("longitude") var longitude: Double? = null,
            @Key("state") @EventState var state: Int = STATE_PENDING
    )
}