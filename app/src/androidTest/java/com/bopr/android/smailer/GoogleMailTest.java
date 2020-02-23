package com.bopr.android.smailer;

import org.junit.Test;

import static com.google.api.services.gmail.GmailScopes.GMAIL_SEND;

public class GoogleMailTest  extends BaseTest{

    @Test
    public void send() throws Exception {
        MailMessage message = new MailMessage();
        message.setSubject("test");
        message.setBody("test");
        message.setRecipients("boprsoftdev@gmail.com");

        GoogleMail transport = new GoogleMail(getContext());
        transport.login("bo.smailer.service@gmail.com", GMAIL_SEND);
        transport.send(message);
    }
}