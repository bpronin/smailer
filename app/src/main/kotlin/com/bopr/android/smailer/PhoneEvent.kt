package com.bopr.android.smailer

import android.os.Parcelable
import androidx.annotation.IntDef
import kotlinx.parcelize.Parcelize
import kotlin.annotation.AnnotationRetention.SOURCE

/**
 * Represents phone call or SMS event.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
@Parcelize
data class PhoneEvent(
        val phone: String,
        val isIncoming: Boolean = false,
        val startTime: Long,
        val endTime: Long? = null,
        val isMissed: Boolean = false,
        val text: String? = null,
        var location: GeoCoordinates? = null,
        var details: String? = null,
        @EventState var state: Int = STATE_PENDING,
        val acceptor: String,
        var processStatus: Int = STATUS_ACCEPTED,
        var processTime: Long? = null,
        var isRead: Boolean = false
) : Parcelable {

    val isSms: Boolean
        get() = text != null

    val callDuration: Long?
        get() = endTime?.minus(startTime)

    @Retention(SOURCE)
    @IntDef(STATE_PENDING, STATE_PROCESSED, STATE_IGNORED)
    annotation class EventState

    companion object {

        const val STATE_PENDING = 0
        const val STATE_PROCESSED = 1
        const val STATE_IGNORED = 2

        const val STATUS_ACCEPTED = 0
        const val STATUS_NUMBER_BLACKLISTED = 1
        const val STATUS_TEXT_BLACKLISTED = 1 shl 1
        const val STATUS_TRIGGER_OFF = 1 shl 2
    }

}