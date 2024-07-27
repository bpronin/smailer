package com.bopr.android.smailer.processor.mail

import android.content.Context

interface MailFormatter {

    fun formatSubject(): String?

    fun formatBody(): String?

}
