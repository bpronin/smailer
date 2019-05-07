package com.bopr.android.smailer;

import android.content.Context;
import android.content.res.Configuration;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_CONTACTS;
import static android.content.pm.PackageManager.PERMISSION_DENIED;
import static com.bopr.android.smailer.Settings.VAL_PREF_EMAIL_CONTENT_CONTACT;
import static com.bopr.android.smailer.Settings.VAL_PREF_EMAIL_CONTENT_DEVICE_NAME;
import static com.bopr.android.smailer.Settings.VAL_PREF_EMAIL_CONTENT_LOCATION;
import static com.bopr.android.smailer.Settings.VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME;
import static com.bopr.android.smailer.Settings.VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME_SENT;
import static com.bopr.android.smailer.util.Util.asSet;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link MailFormatter} tester.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class MailFormatterTest extends BaseTest {

    private Context context;
    private final Date defaultTime = new GregorianCalendar(2016, 1, 2, 3, 4, 5).getTime();

    @Override
    public void setUp() throws Exception {
        super.setUp();

        context = mock(Context.class);
        when(context.getResources()).thenReturn(getContext().getResources());
        when(context.createConfigurationContext(any(Configuration.class))).thenAnswer(new Answer<Context>() {

            @Override
            public Context answer(InvocationOnMock invocation) {
                Configuration parameter = (Configuration) invocation.getArguments()[0];
                return getContext().createConfigurationContext(parameter);
            }
        });
//        when(context.getContentResolver()).thenReturn(getContext().getContentResolver());
//        when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(mock(SharedPreferences.class));
    }

    /**
     * Check formatting incoming sms email subject.
     */
    @Test
    public void testIncomingSmsSubject() {
        PhoneEvent event = new PhoneEvent("+70123456789", true, 0, null, false,
                "Email body text", null, null, PhoneEvent.STATE_PENDING);

        MailFormatter formatter = new MailFormatter(context, event);

        assertEquals("[SMailer] Incoming SMS from +70123456789", formatter.formatSubject());
    }

    /**
     * Check formatting outgoing sms email subject.
     */
    @Test
    public void testOutgoingSmsSubject() {
        PhoneEvent event = new PhoneEvent("+70123456789", false, 0, null, false,
                "Email body text", null, null, PhoneEvent.STATE_PENDING);

        MailFormatter formatter = new MailFormatter(context, event);

        assertEquals("[SMailer] Outgoing SMS to +70123456789", formatter.formatSubject());
    }

    /**
     * Check formatting incoming call email subject.
     */
    @Test
    public void testIncomingCallSubject() {
        PhoneEvent event = new PhoneEvent("+70123456789", true, 0, null, false,
                null, null, null, PhoneEvent.STATE_PENDING);

        MailFormatter formatter = new MailFormatter(context, event);

        assertEquals("[SMailer] Incoming call from +70123456789", formatter.formatSubject());
    }

    /**
     * Check formatting outgoing call email subject.
     */
    @Test
    public void testOutgoingCallSubject() {
        PhoneEvent event = new PhoneEvent("+70123456789", false, 0, null, false,
                null, null, null, PhoneEvent.STATE_PENDING);

        MailFormatter formatter = new MailFormatter(context, event);

        assertEquals("[SMailer] Outgoing call to +70123456789", formatter.formatSubject());
    }

    /**
     * Check formatting outgoing call email subject.
     */
    @Test
    public void testMissedCallSubject() {
        PhoneEvent event = new PhoneEvent("+70123456789", false, 0, null, true,
                null, null, null, PhoneEvent.STATE_PENDING);

        MailFormatter formatter = new MailFormatter(context, event);

        assertEquals("[SMailer] Missed call from +70123456789", formatter.formatSubject());
    }

    /**
     * Check that email body does not contain any footer when no options have been chosen.
     */
    @Test
    public void testNoBodyFooter() {
        PhoneEvent event = new PhoneEvent("+70123456789", true, 0, null, false,
                "Email body text", null, null, PhoneEvent.STATE_PENDING);

        MailFormatter formatter = new MailFormatter(context, event);

        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body>" +
                "Email body text" +
                "</body></html>", formatter.formatBody());
    }

    /**
     * Check email body footer with {@link Settings#VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME} option switched on.
     */
    @Test
    public void testFooterTimeOption() {
        PhoneEvent event = new PhoneEvent("+70123456789", true, defaultTime.getTime(), null, false,
                "Email body text", null, null, PhoneEvent.STATE_PENDING);

        MailFormatter formatter = new MailFormatter(context, event);
        formatter.setSendTime(defaultTime);
        formatter.setContentOptions(asSet(VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME));

        String actual = formatter.formatBody();
        assertEquals("<html>" +
                "<head>" +
                "<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\">" +
                "</head>" +
                "<body>" +
                "Email body text" +
                "<hr style=\"border: none; background-color: #cccccc; height: 1px;\">" +
                "<small>" +
                "Time: February 2, 2016 3:04:05 AM EST" +
                "</small>" +
                "</body>" +
                "</html>", actual);
    }

    /**
     * Check email body footer with {@link Settings#VAL_PREF_EMAIL_CONTENT_DEVICE_NAME} option switched on.
     */
    @Test
    public void testFooterDeviceNameOption() {
        PhoneEvent event = new PhoneEvent("+70123456789", true, 0, null, false,
                "Email body text", null, null, PhoneEvent.STATE_PENDING);

        MailFormatter formatter = new MailFormatter(context, event);
        formatter.setSendTime(defaultTime);
        formatter.setDeviceName("The Device");
        formatter.setContentOptions(asSet(VAL_PREF_EMAIL_CONTENT_DEVICE_NAME));

        assertEquals("<html>" +
                "<head>" +
                "<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\">" +
                "</head>" +
                "<body>" +
                "Email body text" +
                "<hr style=\"border: none; background-color: #cccccc; height: 1px;\">" +
                "<small>" +
                "Sent from The Device" +
                "</small>" +
                "</body>" +
                "</html>", formatter.formatBody());
    }

    /**
     * Check email body footer with {@link Settings#VAL_PREF_EMAIL_CONTENT_DEVICE_NAME} option
     * switched on and no devise name specified.
     */
    @Test
    public void testFooterDeviceNameOptionNoValue() {
        PhoneEvent event = new PhoneEvent("+70123456789", true, 0, null, false,
                "Email body text", null, null, PhoneEvent.STATE_PENDING);

        MailFormatter formatter = new MailFormatter(context, event);
        formatter.setSendTime(defaultTime);
        formatter.setContentOptions(asSet(VAL_PREF_EMAIL_CONTENT_DEVICE_NAME));

        assertEquals("<html>" +
                "<head>" +
                "<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\">" +
                "</head>" +
                "<body>" +
                "Email body text" +
                "</body>" +
                "</html>", formatter.formatBody());
    }

    /**
     * Check email body footer with {@link Settings#VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME} and
     * {@link Settings#VAL_PREF_EMAIL_CONTENT_DEVICE_NAME} options switched on.
     */
    @Test
    public void testFooterDeviceNameTimeOption() {
        PhoneEvent event = new PhoneEvent("+70123456789", true, defaultTime.getTime(), null, false,
                "Email body text", null, null, PhoneEvent.STATE_PENDING);

        MailFormatter formatter = new MailFormatter(context, event);
        formatter.setSendTime(defaultTime);
        formatter.setDeviceName("The Device");
        formatter.setContentOptions(asSet(VAL_PREF_EMAIL_CONTENT_DEVICE_NAME, VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME));

        assertEquals("<html>" +
                "<head>" +
                "<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\">" +
                "</head>" +
                "<body>" +
                "Email body text" +
                "<hr style=\"border: none; background-color: #cccccc; height: 1px;\">" +
                "<small>" +
                "Time: February 2, 2016 3:04:05 AM EST" +
                "<br>" +
                "Sent from The Device" +
                "</small>" +
                "</body>" +
                "</html>", formatter.formatBody());
    }

    /**
     * Check email body footer with {@link Settings#VAL_PREF_EMAIL_CONTENT_LOCATION} option switched on.
     */
    @Test
    public void testFooterLocation() {
        PhoneEvent event = new PhoneEvent("+70123456789", true, 0, null, false,
                "Email body text", new GeoCoordinates(60.555, 30.555), null, PhoneEvent.STATE_PENDING);

        MailFormatter formatter = new MailFormatter(context, event);
        formatter.setSendTime(defaultTime);
        formatter.setContentOptions(asSet(VAL_PREF_EMAIL_CONTENT_LOCATION));

        assertEquals("<html>" +
                "<head>" +
                "<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\">" +
                "</head>" +
                "<body>" +
                "Email body text<hr style=\"border: none; background-color: #cccccc; height: 1px;\">" +
                "<small>" +
                "Last known device location: <a href=\"https://www.google.com/maps/place/60.555+30.555/@60.555,30.555\">60&#176;33'17\"N, 30&#176;33'17\"W</a>" +
                "</small>" +
                "</body>" +
                "</html>", formatter.formatBody());
    }

    /**
     * Check email body footer with {@link Settings#VAL_PREF_EMAIL_CONTENT_DEVICE_NAME} option
     * switched on and no location specified in event.
     */
    @Test
    public void testFooterNoLocation() {
        PhoneEvent event = new PhoneEvent("+70123456789", true, 0, null, false,
                "Email body text", null, null, PhoneEvent.STATE_PENDING);

        MailFormatter formatter = new MailFormatter(context, event);
        formatter.setSendTime(defaultTime);
        formatter.setContentOptions(asSet(VAL_PREF_EMAIL_CONTENT_LOCATION));

        String body = formatter.formatBody();
        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head>" +
                "<body>" +
                "Email body text<hr style=\"border: none; background-color: #cccccc; height: 1px;\">" +
                "<small>" +
                "Last known device location: (location service disabled)" +
                "</small>" +
                "</body>" +
                "</html>", body);
    }

    /**
     * Check email body footer with {@link Settings#VAL_PREF_EMAIL_CONTENT_DEVICE_NAME} option
     * switched on and no location permissions.
     */
    @Test
    public void testFooterNoLocationPermissions() {
        PhoneEvent event = new PhoneEvent("+70123456789", true, 0, null, false,
                "Email body text", null, null, PhoneEvent.STATE_PENDING);

        MailFormatter formatter = new MailFormatter(context, event);
        formatter.setSendTime(defaultTime);
        formatter.setContentOptions(asSet(VAL_PREF_EMAIL_CONTENT_LOCATION));

        when(context.checkPermission(eq(ACCESS_COARSE_LOCATION), anyInt(), anyInt())).thenReturn(PERMISSION_DENIED);
        when(context.checkPermission(eq(ACCESS_FINE_LOCATION), anyInt(), anyInt())).thenReturn(PERMISSION_DENIED);

        assertEquals("<html>" +
                "<head>" +
                "<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\">" +
                "</head>" +
                "<body>" +
                "Email body text<hr style=\"border: none; background-color: #cccccc; height: 1px;\">" +
                "<small>Last known device location: (no permission to read location)" +
                "</small>" +
                "</body>" +
                "</html>", formatter.formatBody());
    }

    /**
     * Check email body footer with {@link Settings#VAL_PREF_EMAIL_CONTENT_CONTACT} option switched on.
     */
    @Test
    public void testContactName() {
        PhoneEvent event = new PhoneEvent("+12345678901", true, 0, null, false,
                "Email body text", null, null, PhoneEvent.STATE_PENDING);

        MailFormatter formatter = new MailFormatter(context, event);
        formatter.setSendTime(defaultTime);
        formatter.setContactName("John Dou");
        formatter.setContentOptions(asSet(VAL_PREF_EMAIL_CONTENT_CONTACT));

        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\">" +
                "</head><body>" +
                "Email body text<hr style=\"border: none; background-color: #cccccc; height: 1px;\">" +
                "<small>" +
                "Sender: <a href=\"tel:+12345678901\" style=\"text-decoration: none\">&#9742;</a>+12345678901 (John Dou)" +
                "</small>" +
                "</body>" +
                "</html>", formatter.formatBody());
    }

    /**
     * Check email body footer with {@link Settings#VAL_PREF_EMAIL_CONTENT_CONTACT} option switched on
     * and no permission to read contacts.
     */
    @Test
    public void testContactNameNoPermission() {
        PhoneEvent event = new PhoneEvent("+12345678901", true, 0, null, false,
                "Email body text", null, null, PhoneEvent.STATE_PENDING);

        MailFormatter formatter = new MailFormatter(context, event);
        formatter.setSendTime(defaultTime);
        formatter.setContentOptions(asSet(VAL_PREF_EMAIL_CONTENT_CONTACT));
        when(context.checkPermission(eq(READ_CONTACTS), anyInt(), anyInt())).thenReturn(PERMISSION_DENIED);

        String body = formatter.formatBody();
        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head>" +
                "<body>Email body text<hr style=\"border: none; background-color: #cccccc; height: 1px;\">" +
                "<small>Sender: <a href=\"tel:+12345678901\" style=\"text-decoration: none\">&#9742;</a>+12345678901 (no permission to read contacts)" +
                "</small></body></html>", body);
    }

    /**
     * Check email body footer with {@link Settings#VAL_PREF_EMAIL_CONTENT_CONTACT} option switched on
     * and unknown contact name.
     */
    @Test
    public void testUnknownContactName() {
        PhoneEvent event = new PhoneEvent("+12345678901", true, 0, null, false,
                "Email body text", null, null, PhoneEvent.STATE_PENDING);

        MailFormatter formatter = new MailFormatter(context, event);
        formatter.setSendTime(defaultTime);
        formatter.setContentOptions(asSet(VAL_PREF_EMAIL_CONTENT_CONTACT));

        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head>" +
                "<body>Email body text<hr style=\"border: none; background-color: #cccccc; height: 1px;\">" +
                "<small>Sender: <a href=\"tel:+12345678901\" style=\"text-decoration: none\">&#9742;</a>+12345678901 (Unknown contact)" +
                "</small></body></html>", formatter.formatBody());
    }

    /**
     * Check incoming call email body.
     */
    @Test
    public void testIncomingCallBody() {
        long start = defaultTime.getTime();
        long end = new GregorianCalendar(2016, 1, 2, 4, 5, 10).getTime().getTime();
        PhoneEvent event = new PhoneEvent("+70123456789", true, start, end, false,
                null, null, null, PhoneEvent.STATE_PENDING);

        MailFormatter formatter = new MailFormatter(context, event);

        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body>" +
                "You had an incoming call of 1:01:05 duration." +
                "</body></html>", formatter.formatBody());
    }

    /**
     * Check outgoing call email body.
     */
    @Test
    public void testOutgoingCallBody() {
        long start = defaultTime.getTime();
        long end = new GregorianCalendar(2016, 1, 2, 4, 5, 15).getTime().getTime();
        PhoneEvent event = new PhoneEvent("+70123456789", false, start, end, false,
                null, null, null, PhoneEvent.STATE_PENDING);

        MailFormatter formatter = new MailFormatter(context, event);

        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body>" +
                "You had an outgoing call of 1:01:10 duration." +
                "</body></html>", formatter.formatBody());
    }

    /**
     * Check missed call email body.
     */
    @Test
    public void testMissedCallBody() {
        long start = defaultTime.getTime();
        PhoneEvent event = new PhoneEvent("+70123456789", false, start, null, true,
                null, null, null, PhoneEvent.STATE_PENDING);

        MailFormatter formatter = new MailFormatter(context, event);

        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body>" +
                "You had a missed call." +
                "</body></html>", formatter.formatBody());
    }

    /**
     * Check incoming call email body when all required information is present.
     */
    @Test
    public void testAllIncomingContentCall() {
        long start = defaultTime.getTime();
        long end = new GregorianCalendar(2016, 1, 2, 4, 5, 10).getTime().getTime();

        PhoneEvent event = new PhoneEvent("+12345678901", true, start, end, false,
                null, new GeoCoordinates(60.555, 30.555), null, PhoneEvent.STATE_PENDING);

        MailFormatter formatter = new MailFormatter(context, event);
        formatter.setSendTime(defaultTime);
        formatter.setContactName("John Dou");
        formatter.setDeviceName("Device");
        formatter.setContentOptions(asSet(VAL_PREF_EMAIL_CONTENT_CONTACT, VAL_PREF_EMAIL_CONTENT_LOCATION,
                VAL_PREF_EMAIL_CONTENT_DEVICE_NAME, VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME, VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME_SENT));

        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head>" +
                "<body>" +
                "You had an incoming call of 1:01:05 duration.<hr style=\"border: none; background-color: #cccccc; height: 1px;\">" +
                "<small>" +
                "Caller: <a href=\"tel:+12345678901\" style=\"text-decoration: none\">&#9742;</a>+12345678901 (John Dou)" +
                "<br>" +
                "Time: February 2, 2016 3:04:05 AM EST" +
                "<br>" +
                "Last known device location: <a href=\"https://www.google.com/maps/place/60.555+30.555/@60.555,30.555\">60&#176;33'17\"N, 30&#176;33'17\"W</a>" +
                "<br>" +
                "Sent from Device at February 2, 2016 3:04:05 AM EST" +
                "</small></body></html>", formatter.formatBody());
    }

    /**
     * Check outgoing call email body when all required information is present.
     */
    @Test
    public void testAllOutgoingContentCall() {
        long start = defaultTime.getTime();
        long end = new GregorianCalendar(2016, 1, 2, 4, 5, 10).getTime().getTime();

        PhoneEvent event = new PhoneEvent("+12345678901", false, start, end, false,
                null, new GeoCoordinates(60.555, 30.555), null, PhoneEvent.STATE_PENDING);

        MailFormatter formatter = new MailFormatter(context, event);
        formatter.setSendTime(defaultTime);
        formatter.setContactName("John Dou");
        formatter.setDeviceName("Device");
        formatter.setContentOptions(asSet(VAL_PREF_EMAIL_CONTENT_CONTACT, VAL_PREF_EMAIL_CONTENT_LOCATION,
                VAL_PREF_EMAIL_CONTENT_DEVICE_NAME, VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME, VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME_SENT));

        String body = formatter.formatBody();
        assertEquals("<html><head>" +
                "<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\">" +
                "</head>" +
                "<body>" +
                "You had an outgoing call of 1:01:05 duration.<hr style=\"border: none; background-color: #cccccc; height: 1px;\">" +
                "<small>" +
                "Called: <a href=\"tel:+12345678901\" style=\"text-decoration: none\">&#9742;</a>+12345678901 (John Dou)" +
                "<br>Time: February 2, 2016 3:04:05 AM EST" +
                "<br>Last known device location: <a href=\"https://www.google.com/maps/place/60.555+30.555/@60.555,30.555\">60&#176;33'17\"N, 30&#176;33'17\"W</a>" +
                "<br>Sent from Device at February 2, 2016 3:04:05 AM EST" +
                "</small></body></html>", body);
    }

    /**
     * Check missed call email body when all required information is present.
     */
    @Test
    public void testAllContentMissedCall() {
        long start = defaultTime.getTime();
        long end = new GregorianCalendar(2016, 1, 2, 4, 5, 10).getTime().getTime();

        PhoneEvent event = new PhoneEvent("+12345678901", true, start, end, true,
                null, new GeoCoordinates(60.555, 30.555), null, PhoneEvent.STATE_PENDING);

        MailFormatter formatter = new MailFormatter(context, event);
        formatter.setSendTime(defaultTime);
        formatter.setDeviceName("Device");
        formatter.setContentOptions(asSet(VAL_PREF_EMAIL_CONTENT_CONTACT, VAL_PREF_EMAIL_CONTENT_LOCATION,
                VAL_PREF_EMAIL_CONTENT_DEVICE_NAME, VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME, VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME_SENT));

        String body = formatter.formatBody();
        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head>" +
                "<body>You had a missed call.<hr style=\"border: none; background-color: #cccccc; height: 1px;\">" +
                "<small>Caller: <a href=\"tel:+12345678901\" style=\"text-decoration: none\">&#9742;</a>+12345678901 (Unknown contact)" +
                "<br>Time: February 2, 2016 3:04:05 AM EST" +
                "<br>Last known device location: <a href=\"https://www.google.com/maps/place/60.555+30.555/@60.555,30.555\">60&#176;33'17\"N, 30&#176;33'17\"W</a>" +
                "<br>Sent from Device at February 2, 2016 3:04:05 AM EST" +
                "</small></body></html>", body);
    }

    /**
     * Check email body with valid non-default locale specified.
     */
    @Test
    public void testNonDefaultLocale() {
        TimeZone timeZone = TimeZone.getTimeZone("EST");
        GregorianCalendar calendar = new GregorianCalendar(timeZone);
        calendar.set(2016, 1, 2, 3, 4, 5);
        long start = calendar.getTime().getTime();
        calendar.set(2016, 1, 2, 3, 4, 10);
        long end = calendar.getTime().getTime();

        PhoneEvent event = new PhoneEvent("+12345678901", true, start, end, true,
                null, new GeoCoordinates(60.555, 30.555),
                null, PhoneEvent.STATE_PENDING);

        MailFormatter formatter = new MailFormatter(context, event);
        formatter.setSendTime(defaultTime);
        formatter.setDeviceName("Device");
        formatter.setLocale("ru_RU");
        formatter.setContentOptions(asSet(VAL_PREF_EMAIL_CONTENT_CONTACT,
                VAL_PREF_EMAIL_CONTENT_LOCATION,
                VAL_PREF_EMAIL_CONTENT_DEVICE_NAME,
                VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME,
                VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME_SENT));

        assertEquals("[SMailer] Пропущенный звонок от +12345678901", formatter.formatSubject());
        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head>" +
                "<body>Пропущенный звонок.<hr style=\"border: none; background-color: #cccccc; height: 1px;\">" +
                "<small>Вам звонил: <a href=\"tel:+12345678901\" style=\"text-decoration: none\">&#9742;</a>+12345678901 (Неизвестный контакт)" +
                "<br>Время: 2 февраля 2016 г. 3:04:05 GMT-05:00" +
                "<br>Последнее известное местоположение: <a href=\"https://www.google.com/maps/place/60.555+30.555/@60.555,30.555\">60&#176;33'17\"N, 30&#176;33'17\"W</a>" +
                "<br>Отправлено с устройства \"Device\" 2 февраля 2016 г. 3:04:05 GMT-05:00" +
                "</small></body></html>", formatter.formatBody());

        formatter.setLocale(null); /* set default locale */

        assertEquals("[SMailer] Missed call from +12345678901", formatter.formatSubject());
        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head>" +
                "<body>You had a missed call.<hr style=\"border: none; background-color: #cccccc; height: 1px;\">" +
                "<small>Caller: <a href=\"tel:+12345678901\" style=\"text-decoration: none\">&#9742;</a>+12345678901 (Unknown contact)" +
                "<br>Time: February 2, 2016 3:04:05 AM EST" +
                "<br>Last known device location: <a href=\"https://www.google.com/maps/place/60.555+30.555/@60.555,30.555\">60&#176;33'17\"N, 30&#176;33'17\"W</a>" +
                "<br>Sent from Device at February 2, 2016 3:04:05 AM EST" +
                "</small></body></html>", formatter.formatBody());
    }

    /**
     * Check email body with invalid non-default locale specified.
     */
    @Test
    public void testInvalidLocale() {
        long start = defaultTime.getTime();
        long end = new GregorianCalendar(2016, 1, 2, 4, 5, 10).getTime().getTime();

        PhoneEvent event = new PhoneEvent("+12345678901", true, start, end, true,
                null, new GeoCoordinates(60.555, 30.555), null, PhoneEvent.STATE_PENDING);

        MailFormatter formatter = new MailFormatter(context, event);
        formatter.setSendTime(defaultTime);
        formatter.setDeviceName("Device");
        formatter.setLocale("blah-blah"); /* should set default locale */
        formatter.setContentOptions(asSet(VAL_PREF_EMAIL_CONTENT_CONTACT, VAL_PREF_EMAIL_CONTENT_LOCATION,
                VAL_PREF_EMAIL_CONTENT_DEVICE_NAME, VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME, VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME_SENT));

        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head>" +
                "<body>" +
                "You had a missed call.<hr style=\"border: none; background-color: #cccccc; height: 1px;\">" +
                "<small>Caller: <a href=\"tel:+12345678901\" style=\"text-decoration: none\">&#9742;</a>+12345678901 (Unknown contact)" +
                "<br>Time: February 2, 2016 3:04:05 AM EST" +
                "<br>Last known device location: <a href=\"https://www.google.com/maps/place/60.555+30.555/@60.555,30.555\">60&#176;33'17\"N, 30&#176;33'17\"W</a>" +
                "<br>Sent from Device at February 2, 2016 3:04:05 AM EST" +
                "</small>" +
                "</body>" +
                "</html>", formatter.formatBody());
    }

    /**
     * Check URLs formatting.
     */
    @Test
    public void testFormatUrls() {
        PhoneEvent event = new PhoneEvent("+12345678901", true, 0, null, false,
                "Please visit http://google.com site", null, null, PhoneEvent.STATE_PENDING);

        MailFormatter formatter = new MailFormatter(context, event);

        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body>" +
                "Please visit <a href=\"http://google.com\">http://google.com</a> site" +
                "</body></html>", formatter.formatBody());
    }

}