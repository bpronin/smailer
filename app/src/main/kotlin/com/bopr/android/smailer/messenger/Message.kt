package com.bopr.android.smailer.messenger

import android.os.Parcelable
import com.bopr.android.smailer.messenger.ProcessingState.Companion.STATE_PENDING
import com.bopr.android.smailer.util.DEVICE_NAME
import com.bopr.android.smailer.util.GeoLocation
import kotlinx.parcelize.Parcelize
import java.lang.System.*

@Parcelize
data class Message(
    @ProcessingState var state: Int = STATE_PENDING,
    val creationTime: Long = currentTimeMillis(),
    var processTime: Long? = null,
    var processResult: Int = FLAG_UNPROCESSED,
    var location: GeoLocation? = null,
    val device: String = DEVICE_NAME,
    var isRead: Boolean = false,
    val payload: Parcelable
) : Parcelable {

    companion object {

        /* processing result flags */

        const val FLAG_UNPROCESSED = 0
        const val FLAG_SENT_BY_EMAIL = 1
        const val FLAG_SENT_BY_TELEGRAM = 1 shl 1
        const val FLAG_SENT_BY_SMS = 1 shl 2
    }
}