package com.bopr.android.smailer.provider.telephony

import android.os.Parcelable
import com.bopr.android.smailer.messenger.Event.Companion.FLAG_ACCEPTED
import com.bopr.android.smailer.messenger.Event.Companion.FLAG_UNPROCESSED
import com.bopr.android.smailer.messenger.ProcessState
import com.bopr.android.smailer.messenger.ProcessState.Companion.STATE_PENDING
import com.bopr.android.smailer.util.Bits
import com.bopr.android.smailer.util.DEVICE_NAME
import com.bopr.android.smailer.util.GeoLocation
import kotlinx.parcelize.Parcelize

/**
 * Phone call or SMS data.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
@Parcelize
data class PhoneCallInfo(
    val phone: String,
    val isIncoming: Boolean = false,
    val startTime: Long,
    val endTime: Long? = null,
    val isMissed: Boolean = false,
    val text: String? = null,

    val acceptor: String = DEVICE_NAME,
    var location: GeoLocation? = null,
    @ProcessState var processState: Int = STATE_PENDING,
    var bypassFlags: Bits = FLAG_ACCEPTED,
    var processFlags: Bits = FLAG_UNPROCESSED,
    var processTime: Long? = null,
    var isRead: Boolean = false
) : Parcelable {

    val isSms get() = text != null
    val callDuration get() = endTime?.minus(startTime)
}