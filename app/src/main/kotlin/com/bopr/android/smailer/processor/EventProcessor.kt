package com.bopr.android.smailer.processor

import android.content.Context
import com.bopr.android.smailer.provider.Event

abstract class EventProcessor(val context: Context) {

    abstract fun isEnabled(): Boolean

    abstract fun process(event: Event)

}
