package com.bopr.android.smailer;

import android.content.res.Resources;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.GregorianCalendar;
import java.util.Locale;

import static com.bopr.android.smailer.settings.Settings.VAL_PREF_EMAIL_CONTENT_CALLER;
import static com.bopr.android.smailer.settings.Settings.VAL_PREF_EMAIL_CONTENT_DEVICE_NAME;
import static com.bopr.android.smailer.settings.Settings.VAL_PREF_EMAIL_CONTENT_LOCATION;
import static com.bopr.android.smailer.settings.Settings.VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;


/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class MailFormatterUnitTest {

    @Mock
    private static Resources resources;

    @BeforeClass
    public static void startUpClass() throws Exception {
        Locale.setDefault(Locale.US);
    }

    @Before
    public void startUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(resources.getString(R.string.app_name)).thenReturn("SMailer");
        when(resources.getString(R.string.email_subject_incoming_sms)).thenReturn("Incoming SMS from");
        when(resources.getString(R.string.email_subject_outgoing_sms)).thenReturn("Outgoing SMS to");
        when(resources.getString(R.string.email_subject_incoming_call)).thenReturn("Incoming call from");
        when(resources.getString(R.string.email_subject_outgoing_call)).thenReturn("Outgoing call to");
        when(resources.getString(R.string.email_subject_missed_call)).thenReturn("Missed call from");
        when(resources.getString(R.string.email_body_missed_call)).thenReturn("You had a missed call.");
        when(resources.getString(R.string.email_body_incoming_call)).thenReturn("You had an incoming call of {duration} duration.");
        when(resources.getString(R.string.email_body_outgoing_call)).thenReturn("You had an outgoing call of {duration} duration.");
        when(resources.getString(R.string.email_body_sent)).thenReturn("Sent{device_name}{time}");
        when(resources.getString(R.string.email_body_from)).thenReturn("from {device_name}");
        when(resources.getString(R.string.email_body_time)).thenReturn("at {time}");
        when(resources.getString(R.string.email_body_sender)).thenReturn("Sender: {phone}{name}");
        when(resources.getString(R.string.email_body_caller)).thenReturn("Caller: {phone}{name}");
        when(resources.getString(R.string.email_body_called)).thenReturn("Called: {phone}{name}");
        when(resources.getString(R.string.email_body_location)).thenReturn("Last known device location: {location}");
    }

    /**
     * Check formatting incoming sms email subject.
     *
     * @throws Exception when fails
     */
    @Test
    public void testIncomingSmsSubject() throws Exception {
        MailFormatter formatter = new MailFormatter(
                new MailMessage("+70123456789", true, 0, 0, false, true, "Email body text", null),
                resources, new MailerProperties(), "John Dou", "The Device");

        String text = formatter.getSubject();
        assertEquals("[SMailer] Incoming SMS from +70123456789", text);
    }

    /**
     * Check formatting incoming call email subject.
     *
     * @throws Exception when fails
     */
    @Test
    public void testIncomingCallSubject() throws Exception {
        MailMessage message = new MailMessage("+70123456789", true, 0, 0, false, false, null, null);
        MailerProperties properties = new MailerProperties();

        MailFormatter formatter = new MailFormatter(message, resources, properties, "John Dou", "The Device");

        String text = formatter.getSubject();
        assertEquals("[SMailer] Incoming call from +70123456789", text);
    }

    /**
     * Check formatting outgoing call email subject.
     *
     * @throws Exception when fails
     */
    @Test
    public void testOutgoingCallSubject() throws Exception {
        MailMessage message = new MailMessage("+70123456789", false, 0, 0, false, false, null, null);
        MailerProperties properties = new MailerProperties();

        MailFormatter formatter = new MailFormatter(message, resources, properties, "John Dou", "The Device");

        String text = formatter.getSubject();
        assertEquals("[SMailer] Outgoing call to +70123456789", text);
    }

    /**
     * Check formatting outgoing call email subject.
     *
     * @throws Exception when fails
     */
    @Test
    public void testMissedCallSubject() throws Exception {
        MailMessage message = new MailMessage("+70123456789", false, 0, 0, true, false, null, null);
        MailerProperties properties = new MailerProperties();

        MailFormatter formatter = new MailFormatter(message, resources, properties, "John Dou", "The Device");

        String text = formatter.getSubject();
        assertEquals("[SMailer] Missed call from +70123456789", text);
    }

    /**
     * Check that email body does not contain any footer when no options have chosen.
     *
     * @throws Exception when fails
     */
    @Test
    public void testNoBodyFooter() throws Exception {
        MailMessage message = new MailMessage("+70123456789", true, 0, 0, false, true,
                "Email body text", null);

        MailerProperties properties = new MailerProperties();

        MailFormatter formatter = new MailFormatter(message, resources, properties, "John Dou", "The Device");

        String text = formatter.getBody();
        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; " +
                "charset=utf-8\"></head><body>Email body text</body></html>", text);
    }

    /**
     * Check email body footer with different options.
     *
     * @throws Exception when fails
     */
    @Test
    public void testFooterTimeOption() throws Exception {
        long time = new GregorianCalendar(2016, 1, 2, 3, 4, 5).getTime().getTime();
        MailMessage message = new MailMessage("+70123456789", true, time, 0, false, true,
                "Email body text", null);

        MailerProperties properties = new MailerProperties();
        properties.setContentOptions(VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME);

        MailFormatter formatter = new MailFormatter(message, resources, properties, "John Dou", "The Device");

        String text = formatter.getBody();
        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; " +
                "charset=utf-8\"></head><body>Email body text<hr style=\"border: none; " +
                "background-color: #cccccc; height: 1px;\">Sent " +
                "at Feb 2, 2016 3:04:05 AM</body></html>", text);
    }

    /**
     * Check email body footer with different options.
     *
     * @throws Exception when fails
     */

    @Test
    public void testFooterTimeNoSenderNoLocationOption() throws Exception {
        long time = new GregorianCalendar(2016, 1, 2, 3, 4, 5).getTime().getTime();
        MailMessage message = new MailMessage("+12345678901", true, time, 0, false, true,
                "Email body text", null);

        MailerProperties properties = new MailerProperties();
        properties.setContentOptions(VAL_PREF_EMAIL_CONTENT_CALLER, VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME);

        MailFormatter formatter = new MailFormatter(message, resources, properties, null, "The Device");

        String text = formatter.getBody();
        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body>" +
                        "Email body text" +
                        "<hr style=\"border: none; background-color: #cccccc; height: 1px;\">" +
                        "Sender: <a href=\"tel:+12345678901\">+12345678901</a>" +
                        "<br>" +
                        "Sent at Feb 2, 2016 3:04:05 AM" +
                        "</body></html>",
                text);
    }

    /**
     * Check email body footer with different options.
     *
     * @throws Exception when fails
     */
    @Test
    public void testFooterDeviceNameOption() throws Exception {
        MailMessage message = new MailMessage("+70123456789", true, 0, 0, false, true,
                "Email body text", null);

        MailerProperties properties = new MailerProperties();
        properties.setContentOptions(VAL_PREF_EMAIL_CONTENT_DEVICE_NAME);

        MailFormatter formatter = new MailFormatter(message, resources, properties, "John Dou", "The Device");

        String text = formatter.getBody();
        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; " +
                "charset=utf-8\"></head><body>Email body text<hr style=\"border: none; " +
                "background-color: #cccccc; height: 1px;\">Sent from " +
                "The Device</body></html>", text);
    }

    /**
     * Check email body footer with different options.
     *
     * @throws Exception when fails
     */
    @Test
    public void testFooterDeviceNameTimeOption() throws Exception {
        long time = new GregorianCalendar(2016, 1, 2, 3, 4, 5).getTime().getTime();
        MailMessage message = new MailMessage("+70123456789", true, time, 0, false, true,
                "Email body text", null);

        MailerProperties properties = new MailerProperties();
        properties.setContentOptions(VAL_PREF_EMAIL_CONTENT_DEVICE_NAME, VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME);

        MailFormatter formatter = new MailFormatter(message, resources, properties, "John Dou", "The Device");

        String text = formatter.getBody();
        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; " +
                "charset=utf-8\"></head><body>Email body text<hr style=\"border: none; " +
                "background-color: #cccccc; height: 1px;\">Sent from " +
                "The Device at Feb 2, 2016 3:04:05 AM</body></html>", text);
    }

    /**
     * Check email body footer with different options.
     *
     * @throws Exception when fails
     */
    @Test
    public void testFooterLocation() throws Exception {
        MailMessage message = new MailMessage("+70123456789", true, 0, 0, false, true,
                "Email body text", 60.555, 30.555);

        MailerProperties properties = new MailerProperties();
        properties.setContentOptions(VAL_PREF_EMAIL_CONTENT_LOCATION);

        MailFormatter formatter = new MailFormatter(message, resources, properties, "John Dou", "The Device");

        String text = formatter.getBody();
        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; " +
                "charset=utf-8\"></head><body>Email body text<hr style=\"border: none; " +
                "background-color: #cccccc; height: 1px;\">Last known device location: " +
                "<a href=\"http://maps.google.com/maps/place/60.555,30.555\">60&#176;33'17\"N, 30&#176;33'17\"W</a>" +
                "</body></html>", text);
    }

    /**
     * Check email body footer with different options.
     *
     * @throws Exception when fails
     */
    @Test
    public void testFooterNoLocation() throws Exception {
        MailMessage message = new MailMessage("+70123456789", true, 0, 0, false, true,
                "Email body text", null);

        MailerProperties properties = new MailerProperties();
        properties.setContentOptions(VAL_PREF_EMAIL_CONTENT_LOCATION);

        MailFormatter formatter = new MailFormatter(message, resources, properties, "John Dou", "The Device");

        String text = formatter.getBody();
        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; " +
                "charset=utf-8\"></head><body>Email body text</body></html>", text);
    }

    /**
     * Check email body footer with different options.
     *
     * @throws Exception when fails
     */
    @Test
    public void testContactName() throws Exception {
        MailMessage message = new MailMessage("+12345678901", true, 0, 0, false, true,
                "Email body text", null);

        MailerProperties properties = new MailerProperties();
        properties.setContentOptions(VAL_PREF_EMAIL_CONTENT_CALLER);

        MailFormatter formatter = new MailFormatter(message, resources, properties, "John Dou", "The Device");

        String text = formatter.getBody();
        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body>" +
                "Email body text" +
                "<hr style=\"border: none; background-color: #cccccc; height: 1px;\">" +
                "Sender: <a href=\"tel:+12345678901\">+12345678901 (John Dou)</a>" +
                "</body></html>", text);
    }

    /**
     * Check formatting incoming call email body.
     *
     * @throws Exception when fails
     */
    @Test
    public void testIncomingCallBody() throws Exception {
        long start = new GregorianCalendar(2016, 1, 2, 3, 4, 5).getTime().getTime();
        long end = new GregorianCalendar(2016, 1, 2, 4, 5, 10).getTime().getTime();
        MailMessage message = new MailMessage("+70123456789", true, start, end, false, false, null, null);

        MailerProperties properties = new MailerProperties();

        MailFormatter formatter = new MailFormatter(message, resources, properties, "John Dou", "The Device");

        String text = formatter.getBody();
        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; " +
                "charset=utf-8\"></head><body>You had an incoming call of 1:01:05 duration." +
                "</body></html>", text);
    }

    /**
     * Check formatting outgoing call email body.
     *
     * @throws Exception when fails
     */
    @Test
    public void testOutgoingCallBody() throws Exception {
        long start = new GregorianCalendar(2016, 1, 2, 3, 4, 5).getTime().getTime();
        long end = new GregorianCalendar(2016, 1, 2, 4, 5, 15).getTime().getTime();
        MailMessage message = new MailMessage("+70123456789", false, start, end, false, false, null, null);

        MailerProperties properties = new MailerProperties();

        MailFormatter formatter = new MailFormatter(message, resources, properties, "John Dou", "The Device");

        String text = formatter.getBody();
        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; " +
                "charset=utf-8\"></head><body>You had an outgoing call of 1:01:10 duration." +
                "</body></html>", text);
    }

    /**
     * Check formatting missed call email body.
     *
     * @throws Exception when fails
     */
    @Test
    public void testMissedCallBody() throws Exception {
        long start = new GregorianCalendar(2016, 1, 2, 3, 4, 5).getTime().getTime();
        MailMessage message = new MailMessage("+70123456789", false, start, 0, true, false, null, null);

        MailerProperties properties = new MailerProperties();

        MailFormatter formatter = new MailFormatter(message, resources, properties, "John Dou", "The Device");

        String text = formatter.getBody();
        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; " +
                "charset=utf-8\"></head><body>You had a missed call." +
                "</body></html>", text);
    }

    /**
     * Check email body footer with different options.
     *
     * @throws Exception when fails
     */
    @Test
    public void testAllContentSms() throws Exception {
        long time = new GregorianCalendar(2016, 1, 2, 3, 4, 5).getTime().getTime();
        MailMessage message = new MailMessage("+12345678901", true, time, 0, false, true,
                "Email body text.", 60.555, 30.555);

        MailerProperties properties = new MailerProperties();
        properties.setContentOptions(VAL_PREF_EMAIL_CONTENT_CALLER, VAL_PREF_EMAIL_CONTENT_LOCATION,
                VAL_PREF_EMAIL_CONTENT_DEVICE_NAME, VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME);

        MailFormatter formatter = new MailFormatter(message, resources, properties, "John Dou", "The Device");

        String text = formatter.getBody();
        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head>" +
                "<body>" +
                "Email body text." +
                "<hr style=\"border: none; background-color: #cccccc; height: 1px;\">" +
                "Sender: <a href=\"tel:+12345678901\">+12345678901 (John Dou)</a>" +
                "<br>" +
                "Last known device location: <a href=\"http://maps.google.com/maps/place/60.555,30.555\">60&#176;33'17\"N, 30&#176;33'17\"W</a>" +
                "<br>" +
                "Sent from The Device at Feb 2, 2016 3:04:05 AM" +
                "</body></html>", text);
    }

    /**
     * Check email body footer with different options.
     *
     * @throws Exception when fails
     */
    @Test
    public void testAllIncomingContentCall() throws Exception {
        long start = new GregorianCalendar(2016, 1, 2, 3, 4, 5).getTime().getTime();
        long end = new GregorianCalendar(2016, 1, 2, 4, 5, 10).getTime().getTime();

        MailMessage message = new MailMessage("+12345678901", true, start, end, false, false, null, 60.555, 30.555);

        MailerProperties properties = new MailerProperties();
        properties.setContentOptions(VAL_PREF_EMAIL_CONTENT_CALLER, VAL_PREF_EMAIL_CONTENT_LOCATION,
                VAL_PREF_EMAIL_CONTENT_DEVICE_NAME, VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME);

        MailFormatter formatter = new MailFormatter(message, resources, properties, "John Dou", "The Device");

        String text = formatter.getBody();
        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head>" +
                "<body>" +
                "You had an incoming call of 1:01:05 duration." +
                "<hr style=\"border: none; background-color: #cccccc; height: 1px;\">" +
                "Caller: <a href=\"tel:+12345678901\">+12345678901 (John Dou)</a>" +
                "<br>" +
                "Last known device location: <a href=\"http://maps.google.com/maps/place/60.555,30.555\">60&#176;33'17\"N, 30&#176;33'17\"W</a>" +
                "<br>" +
                "Sent from The Device at Feb 2, 2016 3:04:05 AM" +
                "</body></html>", text);
    }

    /**
     * Check email body footer with different options.
     *
     * @throws Exception when fails
     */
    @Test
    public void testAllOutgoingContentCall() throws Exception {
        long start = new GregorianCalendar(2016, 1, 2, 3, 4, 5).getTime().getTime();
        long end = new GregorianCalendar(2016, 1, 2, 4, 5, 10).getTime().getTime();

        MailMessage message = new MailMessage("+12345678901", false, start, end, false, false, null, 60.555, 30.555);

        MailerProperties properties = new MailerProperties();
        properties.setContentOptions(VAL_PREF_EMAIL_CONTENT_CALLER, VAL_PREF_EMAIL_CONTENT_LOCATION,
                VAL_PREF_EMAIL_CONTENT_DEVICE_NAME, VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME);

        MailFormatter formatter = new MailFormatter(message, resources, properties, "John Dou", "The Device");

        String text = formatter.getBody();
        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head>" +
                "<body>" +
                "You had an outgoing call of 1:01:05 duration." +
                "<hr style=\"border: none; background-color: #cccccc; height: 1px;\">" +
                "Called: <a href=\"tel:+12345678901\">+12345678901 (John Dou)</a>" +
                "<br>" +
                "Last known device location: <a href=\"http://maps.google.com/maps/place/60.555,30.555\">60&#176;33'17\"N, 30&#176;33'17\"W</a>" +
                "<br>" +
                "Sent from The Device at Feb 2, 2016 3:04:05 AM" +
                "</body></html>", text);
    }

    /**
     * Check email body footer with different options.
     *
     * @throws Exception when fails
     */
    @Test
    public void testAllContentMissedCall() throws Exception {
        long start = new GregorianCalendar(2016, 1, 2, 3, 4, 5).getTime().getTime();
        long end = new GregorianCalendar(2016, 1, 2, 4, 5, 10).getTime().getTime();

        MailMessage message = new MailMessage("+12345678901", true, start, end, true, false, null, 60.555, 30.555);

        MailerProperties properties = new MailerProperties();
        properties.setContentOptions(VAL_PREF_EMAIL_CONTENT_CALLER, VAL_PREF_EMAIL_CONTENT_LOCATION,
                VAL_PREF_EMAIL_CONTENT_DEVICE_NAME, VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME);

        MailFormatter formatter = new MailFormatter(message, resources, properties, null, null);

        String text = formatter.getBody();
        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head>" +
                "<body>" +
                "You had a missed call." +
                "<hr style=\"border: none; background-color: #cccccc; height: 1px;\">" +
                "Caller: <a href=\"tel:+12345678901\">+12345678901</a>" +
                "<br>" +
                "Last known device location: <a href=\"http://maps.google.com/maps/place/60.555,30.555\">60&#176;33'17\"N, 30&#176;33'17\"W</a>" +
                "<br>" +
                "Sent at Feb 2, 2016 3:04:05 AM" +
                "</body></html>", text);
    }

}