package com.bopr.android.smailer.provider

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Event(
    val payload: Parcelable,
) : Parcelable