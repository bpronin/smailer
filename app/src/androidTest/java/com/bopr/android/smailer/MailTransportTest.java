package com.bopr.android.smailer;

import java.util.Properties;

/**
 * {@link MailTransport} tester.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class MailTransportTest extends BaseTest {

//    public void testSend() throws Exception {
//        MailTransport transport = new MailTransport();
//        transport.init("boris.i.pronin@gmail.com", "blue88cofe", "smtp.gmail.com", "465");
//        transport.send("test_subject", "test_body", "boris.i.pronin@gmail.com", "boprsoft.dev@gmail.com");
//    }

    public void testCheckConnection() throws Exception {
        Properties properties = new Properties();
        properties.load(getContext().getAssets().open("debug.properties"));

        String user = properties.getProperty("default_sender");
        String password = properties.getProperty("default_password");

        MailTransport transport = new MailTransport();

        transport.init(user, password, "smtp.gmail.com", "465");
        assertEquals(MailTransport.CHECK_RESULT_OK, transport.checkConnection());

        transport.init(user, "bad_password", "smtp.gmail.com", "465");
        assertEquals(MailTransport.CHECK_RESULT_AUTHENTICATION, transport.checkConnection());

        transport.init("bad_user", password, "smtp.gmail.com", "465");
        assertEquals(MailTransport.CHECK_RESULT_AUTHENTICATION, transport.checkConnection());

        transport.init(user, password, "smtp.gmail.com", "111");
        assertEquals(MailTransport.CHECK_RESULT_NOT_CONNECTED, transport.checkConnection());

        transport.init(user, password, "smtp.ggg.com", "465");
        assertEquals(MailTransport.CHECK_RESULT_NOT_CONNECTED, transport.checkConnection());
    }

}