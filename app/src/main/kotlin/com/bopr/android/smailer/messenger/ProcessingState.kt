package com.bopr.android.smailer.messenger

import androidx.annotation.IntDef

@Retention(AnnotationRetention.SOURCE)
@IntDef(
    ProcessingState.Companion.STATE_PENDING,
    ProcessingState.Companion.STATE_PROCESSED,
    ProcessingState.Companion.STATE_IGNORED
)
annotation class ProcessingState {

    companion object {

        const val STATE_PENDING = 0
        const val STATE_PROCESSED = 1
        const val STATE_IGNORED = 2
    }
}