package com.bopr.android.smailer.messenger

import androidx.annotation.IntDef

@Retention(AnnotationRetention.SOURCE)
@IntDef(
    MessageState.Companion.STATE_PENDING,
    MessageState.Companion.STATE_PROCESSED,
    MessageState.Companion.STATE_IGNORED
)
annotation class MessageState {

    companion object {
        const val STATE_PENDING = 0
        const val STATE_PROCESSED = 1
        const val STATE_IGNORED = 2
    }
}