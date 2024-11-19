package com.bopr.android.smailer.messenger

import android.os.Parcelable
import com.bopr.android.smailer.messenger.ProcessingState.Companion.STATE_PENDING
import com.bopr.android.smailer.util.DEVICE_NAME
import com.bopr.android.smailer.util.Bits
import com.bopr.android.smailer.util.Bits.Companion.bit
import com.bopr.android.smailer.util.GeoLocation
import kotlinx.parcelize.Parcelize
import java.lang.System.*

@Parcelize
data class Message(
    @ProcessingState var state: Int = STATE_PENDING,
    val creationTime: Long = currentTimeMillis(),
    var processTime: Long? = null,
    var processedFlags: Bits = FLAG_UNPROCESSED,
    var location: GeoLocation? = null,
    val device: String = DEVICE_NAME,
    var isRead: Boolean = false,
    val payload: Parcelable
) : Parcelable {

    companion object {

        /* processing result flags */

        val FLAG_UNPROCESSED = Bits()
        val FLAG_SENT_BY_MAIL = bit(0)
        val FLAG_SENT_BY_TELEGRAM = bit(1)
        val FLAG_SENT_BY_SMS = bit(2)
    }
}