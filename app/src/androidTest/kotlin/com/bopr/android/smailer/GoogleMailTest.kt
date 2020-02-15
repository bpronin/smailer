package com.bopr.android.smailer

import com.google.api.services.gmail.GmailScopes
import org.junit.Test

class GoogleMailTest : BaseTest() {

    @Test
    fun testSend() {
        val message = MailMessage(
                subject = "test",
                body = "test",
                recipients = "boprsoftdev@gmail.com"
        )
        val transport = GoogleMail(targetContext)
        transport.startSession("bo.smailer.service@gmail.com", GmailScopes.GMAIL_SEND)
        transport.send(message)
    }
}