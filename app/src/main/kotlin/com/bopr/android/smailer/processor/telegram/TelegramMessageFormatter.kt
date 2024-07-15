package com.bopr.android.smailer.processor.telegram

import android.content.Context

abstract class TelegramMessageFormatter(private val context: Context) {

    abstract fun formatMessage(): String

}
