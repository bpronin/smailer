package com.bopr.android.smailer.util

import android.content.Intent
import android.os.Build
import com.bopr.android.smailer.PhoneEvent

fun Intent.getPhoneEventExtra(name: String): PhoneEvent? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableExtra(name, PhoneEvent::class.java)
    } else {
        @Suppress("DEPRECATION")
        return getParcelableExtra(name)
    }
}