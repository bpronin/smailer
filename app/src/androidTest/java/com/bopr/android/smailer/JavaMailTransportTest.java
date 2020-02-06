package com.bopr.android.smailer;

import org.junit.Test;

public class JavaMailTransportTest {

    @Test
    public void send() throws Exception {
        JavaMailTransport transport = new JavaMailTransport();
        transport.startSession("bo.garbage.box@gmail.com", "secretword2", "smtp.gmail.com", 465);
        transport.send("test", "test body", null, "boprsoft.dev@gmail.com");
    }
}