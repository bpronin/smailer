package com.bopr.android.smailer.provider.telephony

import com.bopr.android.smailer.messenger.EventPayload
import kotlinx.parcelize.Parcelize

/**
 * Phone call or SMS data.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
@Parcelize
data class PhoneCallInfo(
    val startTime: Long,
    val phone: String,
    val isIncoming: Boolean = false,
    val endTime: Long? = null,
    val isMissed: Boolean = false,
    val text: String? = null,
) : EventPayload {

    val isSms get() = text != null
    val callDuration get() = endTime?.minus(startTime)
}