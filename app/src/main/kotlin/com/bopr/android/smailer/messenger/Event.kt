package com.bopr.android.smailer.messenger

import android.os.Parcelable
import com.bopr.android.smailer.messenger.ProcessState.Companion.STATE_PENDING
import com.bopr.android.smailer.util.Bits
import com.bopr.android.smailer.util.Bits.Companion.bit
import com.bopr.android.smailer.util.DEVICE_NAME
import com.bopr.android.smailer.util.GeoLocation
import kotlinx.parcelize.Parcelize
import java.lang.System.*

@Parcelize
data class Event(
    val timestamp: Long = currentTimeMillis(),
    val target: String = DEVICE_NAME,
    var bypassFlags: Bits = Bits(),
    @ProcessState var processState: Int = STATE_PENDING,
    var processFlags: Bits = Bits(),
    var processTime: Long? = null,
    var location: GeoLocation? = null,
    var isRead: Boolean = false,
    val payload: EventPayload
) : Parcelable {

    companion object {

        /* Bypass flags. Explain why an event was not processed. */

        val FLAG_BYPASS_NO_CONSUMERS = bit(0)
        val FLAG_BYPASS_NUMBER_BLACKLISTED = bit(1)
        val FLAG_BYPASS_TEXT_BLACKLISTED = bit(2)
        val FLAG_BYPASS_TRIGGER_OFF = bit(3)

        /* Processing lags. Explains how exactly an event was processed. */

        val FLAG_SENT_BY_MAIL = bit(0)
        val FLAG_SENT_BY_TELEGRAM = bit(1)
        val FLAG_SENT_BY_SMS = bit(2)
    }
}