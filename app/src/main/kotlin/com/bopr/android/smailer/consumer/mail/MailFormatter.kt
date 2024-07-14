package com.bopr.android.smailer.consumer.mail

import android.content.Context

abstract class MailFormatter(private val context: Context) {

    abstract fun formatSubject(): String?

    abstract fun formatBody(): String?

}
