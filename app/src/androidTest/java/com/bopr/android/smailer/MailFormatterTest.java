package com.bopr.android.smailer;

import android.app.Application;
import android.content.Context;
import android.test.ApplicationTestCase;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class MailFormatterTest extends ApplicationTestCase<Application> {

    public MailFormatterTest() {
        super(Application.class);
    }

    /**
     * Check that email body does not contain any footer when no options have chosen.
     *
     * @throws Exception when fails
     */
    public void testNoBodyFooter() throws Exception {
        Context context = getContext();

        MailMessage message = new MailMessage("+70123456789", "Email body text", 0, null);

        MailerProperties properties = new MailerProperties();
        properties.setContentTime(false);
        properties.setContentDeviceName(false);
        properties.setContentLocation(false);
        properties.setContentContactName(false);

        MailFormatter formatter = new MailFormatter(context, properties, message);
        String text = formatter.getBody();

        assertEquals("Email body text", text);
    }

}