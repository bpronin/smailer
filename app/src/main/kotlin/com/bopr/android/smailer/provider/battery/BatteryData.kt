package com.bopr.android.smailer.provider.battery

import com.bopr.android.smailer.messenger.EventPayload
import kotlinx.parcelize.Parcelize

@Parcelize
data class BatteryData(
    val text: String
) : EventPayload
