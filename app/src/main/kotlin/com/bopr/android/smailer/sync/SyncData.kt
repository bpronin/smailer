package com.bopr.android.smailer.sync

import com.bopr.android.smailer.provider.telephony.PhoneEventInfo.Companion.STATE_PENDING
import com.bopr.android.smailer.provider.telephony.PhoneEventInfo.Companion.STATUS_ACCEPTED
import com.bopr.android.smailer.provider.telephony.PhoneEventInfo.EventState
import com.google.api.client.util.Key

/**
 * Data transfer object for synchronization.
 */
/* NOTE: JSON engine requires that all DTO constructor parameters be var and have default value */
data class SyncData(
        @Key("phone_black_list") var phoneBlacklist: Set<String> = emptySet(),
        @Key("text_black_list") var textBlacklist: Set<String> = emptySet(),
        @Key("phone_white_list") var phoneWhitelist: Set<String> = emptySet(),
        @Key("text_white_list") var textWhitelist: Set<String> = emptySet(),
        @Key("phone_events") var events: List<Event> = emptyList()) {

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
            @Key("state") @EventState var state: Int = STATE_PENDING,
            @Key("process_status") var processStatus: Int = STATUS_ACCEPTED,
            @Key("process_time") var processTime: Long? = null,
            @Key("is_read") var isRead: Boolean = false
    )
}