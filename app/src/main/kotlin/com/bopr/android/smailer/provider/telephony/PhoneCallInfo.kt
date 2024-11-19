package com.bopr.android.smailer.provider.telephony

import android.os.Parcelable
import com.bopr.android.smailer.messenger.ProcessingState
import com.bopr.android.smailer.messenger.ProcessingState.Companion.STATE_PENDING
import com.bopr.android.smailer.util.Bits
import com.bopr.android.smailer.util.Bits.Companion.bit
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
    var location: GeoLocation? = null,
    var details: String? = null,
    @ProcessingState var processState: Int = STATE_PENDING,
    val acceptor: String,
    var bypassFlags: Bits = FLAG_BYPASS_NONE,
    var processTime: Long? = null,
    var isRead: Boolean = false
) : Parcelable {

    val isSms get() = text != null
    val callDuration get() = endTime?.minus(startTime)

    companion object {

        /* informative flags */

        val FLAG_BYPASS_NONE = Bits()
        val FLAG_BYPASS_NO_CONSUMERS = bit(0)
        val FLAG_BYPASS_NUMBER_BLACKLISTED = bit(1)
        val FLAG_BYPASS_TEXT_BLACKLISTED = bit(2)
        val FLAG_BYPASS_TRIGGER_OFF = bit(3)
    }
}