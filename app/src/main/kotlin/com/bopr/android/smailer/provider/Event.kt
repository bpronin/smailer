package com.bopr.android.smailer.provider

import android.os.Parcelable
import androidx.annotation.IntDef
import kotlinx.parcelize.Parcelize
import kotlin.annotation.AnnotationRetention.SOURCE

@Parcelize
data class Event2(
    val payload: Parcelable,
) : Parcelable {


}