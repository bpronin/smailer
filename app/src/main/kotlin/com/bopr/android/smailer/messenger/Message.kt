package com.bopr.android.smailer.messenger

import android.os.Parcelable
import com.bopr.android.smailer.messenger.MessageState.Companion.STATE_PENDING
import com.bopr.android.smailer.util.DEVICE_NAME
import com.bopr.android.smailer.util.GeoLocation
import kotlinx.parcelize.Parcelize
import java.lang.System.*

@Parcelize
data class Message(
    val creationTime: Long = currentTimeMillis(),
    val device: String = DEVICE_NAME,
    @MessageState var state: Int = STATE_PENDING,
    var processTime: Long? = null,
    var location: GeoLocation? = null,
    var isRead: Boolean = false,
    val payload: Parcelable
) : Parcelable