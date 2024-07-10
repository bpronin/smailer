package com.bopr.android.smailer.sender

import android.content.Context

abstract class Transport(val context: Context) {

    abstract fun sendMessage(
        message: EventMessage,
        onSuccess: () -> Unit,
        onError: (error: Exception) -> Unit
    )
}
