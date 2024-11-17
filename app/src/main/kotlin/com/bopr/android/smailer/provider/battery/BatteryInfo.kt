package com.bopr.android.smailer.provider.battery

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BatteryInfo(
    val text: String
) : Parcelable
