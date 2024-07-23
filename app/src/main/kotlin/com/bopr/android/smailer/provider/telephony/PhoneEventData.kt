package com.bopr.android.smailer.provider.telephony

import android.os.Parcelable
import com.bopr.android.smailer.provider.EventState
import com.bopr.android.smailer.provider.EventState.Companion.STATE_PENDING
import com.bopr.android.smailer.util.GeoLocation
import kotlinx.parcelize.Parcelize

/**
 * Represents phone call or SMS event.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
@Parcelize
data class PhoneEventData(
    val phone: String,
    val isIncoming: Boolean = false,
    val startTime: Long,
    val endTime: Long? = null,
    val isMissed: Boolean = false,
    val text: String? = null,
    var location: GeoLocation? = null,
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

    companion object {

        const val STATUS_ACCEPTED = 0
        const val STATUS_NUMBER_BLACKLISTED = 1
        const val STATUS_TEXT_BLACKLISTED = 1 shl 1
        const val STATUS_TRIGGER_OFF = 1 shl 2
    }
}