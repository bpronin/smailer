package com.bopr.android.smailer.messenger.mail

interface MailFormatter {

    fun formatSubject(): String?

    fun formatBody(): String?

}
