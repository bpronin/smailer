package com.bopr.android.smailer.sender

import android.content.Context

abstract class MessengerTransport(val context: Context) {

    abstract fun sendMessages(vararg messages: EventMessage)
}
