package com.bopr.android.smailer.provider.battery

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BatteryLevelInfo(val title: String, val text: String) : Parcelable
