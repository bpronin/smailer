package com.bopr.android.smailer.provider

import androidx.annotation.IntDef
import com.bopr.android.smailer.provider.EventState.Companion.STATE_IGNORED
import com.bopr.android.smailer.provider.EventState.Companion.STATE_PENDING
import com.bopr.android.smailer.provider.EventState.Companion.STATE_PROCESSED
import com.bopr.android.smailer.provider.telephony.PhoneEventData

@Retention(AnnotationRetention.SOURCE)
@IntDef(STATE_PENDING, STATE_PROCESSED, STATE_IGNORED)
annotation class EventState {

    companion object {
        const val STATE_PENDING = 0
        const val STATE_PROCESSED = 1
        const val STATE_IGNORED = 2
    }
}