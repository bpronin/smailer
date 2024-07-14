package com.bopr.android.smailer.consumer.telegram

import android.content.Context

abstract class MessageFormatter(private val context: Context) {

    abstract fun formatMessage(): String

}
