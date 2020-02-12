package com.bopr.android.smailer

import android.os.Parcelable
import androidx.annotation.IntDef
import kotlinx.android.parcel.Parcelize
import kotlin.annotation.AnnotationRetention.SOURCE

/**
 * Represents phone call or SMS event.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
@Parcelize
data class PhoneEvent(
        var phone: String = "",
        var isIncoming: Boolean = false,
        var startTime: Long = 0,
        var endTime: Long? = null,
        var isMissed: Boolean = false,
        var text: String? = null,
        var location: GeoCoordinates? = null,
        var details: String? = null,
        @EventState var state: Int = STATE_PENDING,
        var recipient: String? = null,
        var stateReason: Int = REASON_ACCEPTED,
        var isRead: Boolean = false
) : Parcelable {

    val isSms: Boolean
        get() = text?.isNotEmpty() ?: false

    val callDuration: Long
        get() = endTime?.minus(startTime) ?: 0

    @Retention(SOURCE)
    @IntDef(STATE_PENDING, STATE_PROCESSED, STATE_IGNORED)
    annotation class EventState

    companion object {
        const val STATE_PENDING = 0
        const val STATE_PROCESSED = 1
        const val STATE_IGNORED = 2
        const val REASON_ACCEPTED = 0
        const val REASON_NUMBER_BLACKLISTED = 1
        const val REASON_TEXT_BLACKLISTED = 1 shl 1
        const val REASON_TRIGGER_OFF = 1 shl 2
    }

}