package com.bopr.android.smailer;

import com.bopr.android.smailer.mail.JavaMailTransport;

import org.junit.Test;

import java.io.InputStream;
import java.util.Properties;

/**
 * {@link JavaMailTransport} tester.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class MailTransportTest extends BaseTest {

//    public void testSend() throws Exception {
//        JavaMailTransport transport = new JavaMailTransport();
//        transport.init("boris.i.pronin@gmail.com", "blue88cofe", "smtp.gmail.com", "465");
//        transport.send("test_subject", "test_body", "boris.i.pronin@gmail.com", "boprsoft.dev@gmail.com");
//    }

    /* NOTE: disable antivirus for this test */
    @Test
    public void testCheckConnection() throws Exception {
        Properties properties = new Properties();
        InputStream stream = getContext().getAssets().open("debug.properties");
        properties.load(stream);
        stream.close();

        String user = properties.getProperty("default_sender");
        String password = properties.getProperty("default_password");

        JavaMailTransport transport = new JavaMailTransport();

        transport.startSession(user, password, "smtp.gmail.com", "465");
        assertEquals(JavaMailTransport.CHECK_RESULT_OK, transport.checkSession());

        transport.startSession(user, "bad_password", "smtp.gmail.com", "465");
        assertEquals(JavaMailTransport.CHECK_RESULT_AUTHENTICATION, transport.checkSession());

        transport.startSession("bad_user", password, "smtp.gmail.com", "465");
        assertEquals(JavaMailTransport.CHECK_RESULT_AUTHENTICATION, transport.checkSession());

        transport.startSession(user, password, "smtp.gmail.com", "111");
        assertEquals(JavaMailTransport.CHECK_RESULT_NOT_CONNECTED, transport.checkSession());

        transport.startSession(user, password, "smtp.ggg.com", "465");
        assertEquals(JavaMailTransport.CHECK_RESULT_NOT_CONNECTED, transport.checkSession());
    }

}