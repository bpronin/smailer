package com.bopr.android.smailer.provider.battery

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BatteryLevelData(
    val text: String
) : Parcelable
