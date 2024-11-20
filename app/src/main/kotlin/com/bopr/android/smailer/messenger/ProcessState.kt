package com.bopr.android.smailer.messenger

import androidx.annotation.IntDef

@Retention(AnnotationRetention.SOURCE)
@IntDef(
    ProcessState.Companion.STATE_PENDING,
    ProcessState.Companion.STATE_PROCESSED,
    ProcessState.Companion.STATE_IGNORED
)
annotation class ProcessState {

    companion object {

        const val STATE_PENDING = 0
        const val STATE_PROCESSED = 1
        const val STATE_IGNORED = 2
    }
}