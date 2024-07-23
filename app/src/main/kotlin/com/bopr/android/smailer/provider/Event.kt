package com.bopr.android.smailer.provider

import android.os.Parcelable
import com.bopr.android.smailer.provider.EventState.Companion.STATE_PENDING
import com.bopr.android.smailer.util.DEVICE_NAME
import com.bopr.android.smailer.util.GeoLocation
import kotlinx.parcelize.Parcelize

@Parcelize
data class Event(
    val time: Long = System.currentTimeMillis(),
    val device: String = DEVICE_NAME,
    @EventState var state: Int = STATE_PENDING,
    var processTime: Long? = null,
    var location: GeoLocation? = null,
    var isRead: Boolean = false,
    val payload: Parcelable,
) : Parcelable