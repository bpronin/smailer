package com.bopr.android.smailer.sync

import com.bopr.android.smailer.messenger.ProcessState
import com.google.api.client.util.Key

/* NOTE: JSON engine requires all DTO constructor parameters to be var and have a default value. */

/**
 * Data transfer object for synchronization.
 */
data class SyncData(
    @Key("events") var events: List<Event> = emptyList(),
    @Key("phone_calls") var phoneCalls: List<PhoneCall> = emptyList(),
    @Key("phone_black_list") var phoneBlacklist: Set<String> = emptySet(),
    @Key("text_black_list") var textBlacklist: Set<String> = emptySet(),
    @Key("phone_white_list") var phoneWhitelist: Set<String> = emptySet(),
    @Key("text_white_list") var textWhitelist: Set<String> = emptySet()
) {

    data class Event(
        @Key("timestamp") var timestamp: Long = 0,
        @Key("target") var target: String = "",
        @Key("bypass_flags") var bypassFlags: Int = 0,
        @Key("process_state") @ProcessState var processState: Int = 0,
        @Key("process_time") var processTime: Long? = null,
        @Key("process_flags") var processFlags: Int = 0,
        @Key("latitude") var latitude: Double? = null,
        @Key("longitude") var longitude: Double? = null,
        @Key("is_read") var isRead: Boolean = false,
        @Key("payload_type") var payloadType: Int = 0
    )

    data class PhoneCall(
        @Key("timestamp") var timestamp: Long = 0,
        @Key("target") var target: String = "",
        @Key("start_time") var startTime: Long = 0,
        @Key("end_time") var endTime: Long? = null,
        @Key("phone") var phone: String = "",
        @Key("is_incoming") var incoming: Boolean = false,
        @Key("is_missed") var missed: Boolean = false,
        @Key("message_text") var text: String? = null
    )
}