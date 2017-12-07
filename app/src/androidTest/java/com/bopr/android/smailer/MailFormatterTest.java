package com.bopr.android.smailer;

import android.content.Context;
import com.bopr.android.smailer.util.Util;
import org.junit.Test;

import java.util.GregorianCalendar;
import java.util.TimeZone;

import static android.Manifest.permission.*;
import static android.content.pm.PackageManager.PERMISSION_DENIED;
import static com.bopr.android.smailer.Settings.*;
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

    @Override
    public void setUp() throws Exception {
        super.setUp();

        context = mock(Context.class);
        when(context.getResources()).thenReturn(getContext().getResources());
//        when(context.getContentResolver()).thenReturn(getContext().getContentResolver());
//        when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(mock(SharedPreferences.class));
    }

    /**
     * Check formatting incoming sms email subject.
     *
     * @throws Exception when fails
     */
    @Test
    public void testIncomingSmsSubject() throws Exception {
        PhoneEvent event = new PhoneEvent("+70123456789", true, null, null, false,
                "Email body text", null, false, null, PhoneEvent.State.PENDING);

        MailFormatter formatter = new MailFormatter(context, event);

        assertEquals("[SMailer] Incoming SMS from +70123456789", formatter.getSubject());
    }

    /**
     * Check formatting outgoing sms email subject.
     *
     * @throws Exception when fails
     */
    @Test
    public void testOutgoingSmsSubject() throws Exception {
        PhoneEvent message = new PhoneEvent("+70123456789", false, null, null, false,
                "Email body text", null, false, null, PhoneEvent.State.PENDING);

        MailFormatter formatter = new MailFormatter(context, message);

        assertEquals("[SMailer] Outgoing SMS to +70123456789", formatter.getSubject());
    }

    /**
     * Check formatting incoming call email subject.
     *
     * @throws Exception when fails
     */
    @Test
    public void testIncomingCallSubject() throws Exception {
        PhoneEvent message = new PhoneEvent("+70123456789", true, null, null, false,
                null, null, false, null, PhoneEvent.State.PENDING);

        MailFormatter formatter = new MailFormatter(context, message);

        assertEquals("[SMailer] Incoming call from +70123456789", formatter.getSubject());
    }

    /**
     * Check formatting outgoing call email subject.
     *
     * @throws Exception when fails
     */
    @Test
    public void testOutgoingCallSubject() throws Exception {
        PhoneEvent message = new PhoneEvent("+70123456789", false, null, null, false,
                null, null, false, null, PhoneEvent.State.PENDING);

        MailFormatter formatter = new MailFormatter(context, message);

        assertEquals("[SMailer] Outgoing call to +70123456789", formatter.getSubject());
    }

    /**
     * Check formatting outgoing call email subject.
     *
     * @throws Exception when fails
     */
    @Test
    public void testMissedCallSubject() throws Exception {
        PhoneEvent message = new PhoneEvent("+70123456789", false, null, null, true,
                null, null, false, null, PhoneEvent.State.PENDING);

        MailFormatter formatter = new MailFormatter(context, message);

        assertEquals("[SMailer] Missed call from +70123456789", formatter.getSubject());
    }

    /**
     * Check that email body does not contain any footer when no options have been chosen.
     *
     * @throws Exception when fails
     */
    @Test
    public void testNoBodyFooter() throws Exception {
        PhoneEvent message = new PhoneEvent("+70123456789", true, null, null, false,
                "Email body text", null, false, null, PhoneEvent.State.PENDING);

        MailFormatter formatter = new MailFormatter(context, message);

        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body>" +
                "Email body text" +
                "</body></html>", formatter.getBody());
    }

    /**
     * Check email body footer with {@link Settings#VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME} option switched on.
     *
     * @throws Exception when fails
     */
    @Test
    public void testFooterTimeOption() throws Exception {
        long time = new GregorianCalendar(2016, 1, 2, 3, 4, 5).getTime().getTime();
        PhoneEvent message = new PhoneEvent("+70123456789", true, time, null, false,
                "Email body text", null, false, null, PhoneEvent.State.PENDING);

        MailFormatter formatter = new MailFormatter(context, message);
        formatter.setContentOptions(Util.asSet(VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME));

        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body>" +
                "Email body text" +
                "<hr style=\"border: none; background-color: #cccccc; height: 1px;\"><small>" +
                "Sent at February 2, 2016 3:04:05 AM EST" +
                "</small></body></html>", formatter.getBody());
    }

    /**
     * Check email body footer with {@link Settings#VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME} option
     * switched on and no time specified in message.
     *
     * @throws Exception when fails
     */
    @Test
    public void testFooterNoTime() throws Exception {
        PhoneEvent message = new PhoneEvent("+70123456789", true, null, null, false,
                "Email body text", null, false, null, PhoneEvent.State.PENDING);

        MailFormatter formatter = new MailFormatter(context, message);
        formatter.setContentOptions(Util.asSet(VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME));

        String body = formatter.getBody();
        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body>" +
                "Email body text" +
                "</body></html>", body);
    }

    /**
     * Check email body footer with {@link Settings#VAL_PREF_EMAIL_CONTENT_DEVICE_NAME} option switched on.
     *
     * @throws Exception when fails
     */
    @Test
    public void testFooterDeviceNameOption() throws Exception {
        String deviceName = Settings.getDeviceName(getContext());
        PhoneEvent message = new PhoneEvent("+70123456789", true, null, null, false,
                "Email body text", null, false, null, PhoneEvent.State.PENDING);

        MailFormatter formatter = new MailFormatter(context, message);
        formatter.setDeviceName(deviceName);
        formatter.setContentOptions(Util.asSet(VAL_PREF_EMAIL_CONTENT_DEVICE_NAME));

        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body>" +
                "Email body text" +
                "<hr style=\"border: none; background-color: #cccccc; height: 1px;\"><small>" +
                "Sent from " + deviceName +
                "</small></body></html>", formatter.getBody());
    }

    /**
     * Check email body footer with {@link Settings#VAL_PREF_EMAIL_CONTENT_DEVICE_NAME} option
     * switched on and no devise name specified.
     *
     * @throws Exception when fails
     */
    @Test
    public void testFooterDeviceNameOptionNoValue() throws Exception {
        PhoneEvent message = new PhoneEvent("+70123456789", true, null, null, false,
                "Email body text", null, false, null, PhoneEvent.State.PENDING);

        MailFormatter formatter = new MailFormatter(context, message);
        formatter.setContentOptions(Util.asSet(VAL_PREF_EMAIL_CONTENT_DEVICE_NAME));

        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body>" +
                "Email body text" +
                "</body></html>", formatter.getBody());
    }

    /**
     * Check email body footer with {@link Settings#VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME} and
     * {@link Settings#VAL_PREF_EMAIL_CONTENT_DEVICE_NAME} options switched on.
     *
     * @throws Exception when fails
     */
    @Test
    public void testFooterDeviceNameTimeOption() throws Exception {
        String deviceName = Settings.getDeviceName(getContext());
        long time = new GregorianCalendar(2016, 1, 2, 3, 4, 5).getTime().getTime();
        PhoneEvent message = new PhoneEvent("+70123456789", true, time, null, false,
                "Email body text", null, false, null, PhoneEvent.State.PENDING);

        MailFormatter formatter = new MailFormatter(context, message);
        formatter.setDeviceName(deviceName);
        formatter.setContentOptions(Util.asSet(VAL_PREF_EMAIL_CONTENT_DEVICE_NAME, VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME));


        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body>" +
                "Email body text" +
                "<hr style=\"border: none; background-color: #cccccc; height: 1px;\"><small>" +
                "Sent from " + deviceName + " at February 2, 2016 3:04:05 AM EST" +
                "</small></body></html>", formatter.getBody());
    }

    /**
     * Check email body footer with {@link Settings#VAL_PREF_EMAIL_CONTENT_LOCATION} option switched on.
     *
     * @throws Exception when fails
     */
    @Test
    public void testFooterLocation() throws Exception {
        PhoneEvent message = new PhoneEvent("+70123456789", true, null, null, false,
                "Email body text", new GeoCoordinates(60.555, 30.555), false, null, PhoneEvent.State.PENDING);

        MailFormatter formatter = new MailFormatter(context, message);
        formatter.setContentOptions(Util.asSet(VAL_PREF_EMAIL_CONTENT_LOCATION));

        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body>" +
                "Email body text" +
                "<hr style=\"border: none; background-color: #cccccc; height: 1px;\"><small>" +
                "Last known device location: <a href=\"https://www.google.com/maps/place/60.555+30.555/@60.555,30.555\">" +
                "60&#176;33'17\"N, 30&#176;33'17\"W</a>" +
                "</small></body></html>", formatter.getBody());
    }

    /**
     * Check email body footer with {@link Settings#VAL_PREF_EMAIL_CONTENT_DEVICE_NAME} option
     * switched on and no location specified in message.
     *
     * @throws Exception when fails
     */
    @Test
    public void testFooterNoLocation() throws Exception {
        PhoneEvent message = new PhoneEvent("+70123456789", true, null, null, false,
                "Email body text", null, false, null, PhoneEvent.State.PENDING);

        MailFormatter formatter = new MailFormatter(context, message);
        formatter.setContentOptions(Util.asSet(VAL_PREF_EMAIL_CONTENT_LOCATION));

        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body>" +
                "Email body text" +
                "<hr style=\"border: none; background-color: #cccccc; height: 1px;\"><small>" +
                "Last known device location: (location service disabled)" +
                "</small></body></html>", formatter.getBody());
    }

    /**
     * Check email body footer with {@link Settings#VAL_PREF_EMAIL_CONTENT_DEVICE_NAME} option
     * switched on and no location permissions.
     *
     * @throws Exception when fails
     */
    @Test
    public void testFooterNoLocationPermissions() throws Exception {
        PhoneEvent message = new PhoneEvent("+70123456789", true, null, null, false,
                "Email body text", null, false, null, PhoneEvent.State.PENDING);

        MailFormatter formatter = new MailFormatter(context, message);
        formatter.setContentOptions(Util.asSet(VAL_PREF_EMAIL_CONTENT_LOCATION));
        when(context.checkPermission(eq(ACCESS_COARSE_LOCATION), anyInt(), anyInt())).thenReturn(PERMISSION_DENIED);
        when(context.checkPermission(eq(ACCESS_FINE_LOCATION), anyInt(), anyInt())).thenReturn(PERMISSION_DENIED);

        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body>" +
                "Email body text" +
                "<hr style=\"border: none; background-color: #cccccc; height: 1px;\"><small>" +
                "Last known device location: (no permission to read location)" +
                "</small></body></html>", formatter.getBody());
    }

    /**
     * Check email body footer with {@link Settings#VAL_PREF_EMAIL_CONTENT_CONTACT} option switched on.
     *
     * @throws Exception when fails
     */
    @Test
    public void testContactName() throws Exception {
        PhoneEvent message = new PhoneEvent("+12345678901", true, null, null, false,
                "Email body text", null, false, null, PhoneEvent.State.PENDING);

        MailFormatter formatter = new MailFormatter(context, message);
        formatter.setContactName("John Dou");
        formatter.setContentOptions(Util.asSet(VAL_PREF_EMAIL_CONTENT_CONTACT));

        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body>" +
                "Email body text" +
                "<hr style=\"border: none; background-color: #cccccc; height: 1px;\"><small>" +
                "Sender: <a href=\"tel:+12345678901\">+12345678901</a> (John Dou)" +
                "</small></body></html>", formatter.getBody());
    }

    /**
     * Check email body footer with {@link Settings#VAL_PREF_EMAIL_CONTENT_CONTACT} option switched on
     * and no permission to read contacts.
     *
     * @throws Exception when fails
     */
    @Test
    public void testContactNameNoPermission() throws Exception {
        PhoneEvent message = new PhoneEvent("+12345678901", true, null, null, false,
                "Email body text", null, false, null, PhoneEvent.State.PENDING);

        MailFormatter formatter = new MailFormatter(context, message);
        formatter.setContentOptions(Util.asSet(VAL_PREF_EMAIL_CONTENT_CONTACT));
        when(context.checkPermission(eq(READ_CONTACTS), anyInt(), anyInt())).thenReturn(PERMISSION_DENIED);

        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body>" +
                "Email body text" +
                "<hr style=\"border: none; background-color: #cccccc; height: 1px;\"><small>" +
                "Sender: <a href=\"tel:+12345678901\">+12345678901</a> (no permission to read contacts)" +
                "</small></body></html>", formatter.getBody());
    }

    /**
     * Check email body footer with {@link Settings#VAL_PREF_EMAIL_CONTENT_CONTACT} option switched on
     * and unknown contact name.
     *
     * @throws Exception when fails
     */
    @Test
    public void testUnknownContactName() throws Exception {
        PhoneEvent message = new PhoneEvent("+12345678901", true, null, null, false,
                "Email body text", null, false, null, PhoneEvent.State.PENDING);

        MailFormatter formatter = new MailFormatter(context, message);
        formatter.setContentOptions(Util.asSet(VAL_PREF_EMAIL_CONTENT_CONTACT));

        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body>" +
                "Email body text" +
                "<hr style=\"border: none; background-color: #cccccc; height: 1px;\"><small>" +
                "Sender: <a href=\"tel:+12345678901\">+12345678901</a> (Unknown contact)" +
                "</small></body></html>", formatter.getBody());
    }

    /**
     * Check incoming call email body.
     *
     * @throws Exception when fails
     */
    @Test
    public void testIncomingCallBody() throws Exception {
        long start = new GregorianCalendar(2016, 1, 2, 3, 4, 5).getTime().getTime();
        long end = new GregorianCalendar(2016, 1, 2, 4, 5, 10).getTime().getTime();
        PhoneEvent message = new PhoneEvent("+70123456789", true, start, end, false,
                null, null, false, null, PhoneEvent.State.PENDING);

        MailFormatter formatter = new MailFormatter(context, message);

        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body>" +
                "You had an incoming call of 1:01:05 duration." +
                "</body></html>", formatter.getBody());
    }

    /**
     * Check outgoing call email body.
     *
     * @throws Exception when fails
     */
    @Test
    public void testOutgoingCallBody() throws Exception {
        long start = new GregorianCalendar(2016, 1, 2, 3, 4, 5).getTime().getTime();
        long end = new GregorianCalendar(2016, 1, 2, 4, 5, 15).getTime().getTime();
        PhoneEvent message = new PhoneEvent("+70123456789", false, start, end, false,
                null, null, false, null, PhoneEvent.State.PENDING);

        MailFormatter formatter = new MailFormatter(context, message);

        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body>" +
                "You had an outgoing call of 1:01:10 duration." +
                "</body></html>", formatter.getBody());
    }

    /**
     * Check missed call email body.
     *
     * @throws Exception when fails
     */
    @Test
    public void testMissedCallBody() throws Exception {
        long start = new GregorianCalendar(2016, 1, 2, 3, 4, 5).getTime().getTime();
        PhoneEvent message = new PhoneEvent("+70123456789", false, start, null, true,
                null, null, false, null, PhoneEvent.State.PENDING);

        MailFormatter formatter = new MailFormatter(context, message);

        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body>" +
                "You had a missed call." +
                "</body></html>", formatter.getBody());
    }

    /**
     * Check incoming call email body when all required information is present.
     *
     * @throws Exception when fails
     */
    @Test
    public void testAllIncomingContentCall() throws Exception {
        String deviceName = Settings.getDeviceName(getContext());
        long start = new GregorianCalendar(2016, 1, 2, 3, 4, 5).getTime().getTime();
        long end = new GregorianCalendar(2016, 1, 2, 4, 5, 10).getTime().getTime();

        PhoneEvent message = new PhoneEvent("+12345678901", true, start, end, false,
                null, new GeoCoordinates(60.555, 30.555), true, null, PhoneEvent.State.PENDING);

        MailFormatter formatter = new MailFormatter(context, message);
        formatter.setContactName("John Dou");
        formatter.setDeviceName(deviceName);
        formatter.setContentOptions(Util.asSet(VAL_PREF_EMAIL_CONTENT_CONTACT, VAL_PREF_EMAIL_CONTENT_LOCATION,
                VAL_PREF_EMAIL_CONTENT_DEVICE_NAME, VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME));

        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head>" +
                "<body>" +
                "You had an incoming call of 1:01:05 duration." +
                "<hr style=\"border: none; background-color: #cccccc; height: 1px;\"><small>" +
                "Caller: <a href=\"tel:+12345678901\">+12345678901</a> (John Dou)" +
                "<br>" +
                "Last known device location: <a href=\"https://www.google.com/maps/place/60.555+30.555/@60.555,30.555\">60&#176;33'17\"N, 30&#176;33'17\"W</a>" +
                "<br>" +
                "Sent from " + deviceName + " at February 2, 2016 3:04:05 AM EST" +
                "</small></body></html>", formatter.getBody());
    }

    /**
     * Check outgoing call email body when all required information is present.
     *
     * @throws Exception when fails
     */
    @Test
    public void testAllOutgoingContentCall() throws Exception {
        String deviceName = Settings.getDeviceName(getContext());
        long start = new GregorianCalendar(2016, 1, 2, 3, 4, 5).getTime().getTime();
        long end = new GregorianCalendar(2016, 1, 2, 4, 5, 10).getTime().getTime();

        PhoneEvent message = new PhoneEvent("+12345678901", false, start, end, false,
                null, new GeoCoordinates(60.555, 30.555), true, null, PhoneEvent.State.PENDING);

        MailFormatter formatter = new MailFormatter(context, message);
        formatter.setContactName("John Dou");
        formatter.setDeviceName(deviceName);
        formatter.setContentOptions(Util.asSet(VAL_PREF_EMAIL_CONTENT_CONTACT, VAL_PREF_EMAIL_CONTENT_LOCATION,
                VAL_PREF_EMAIL_CONTENT_DEVICE_NAME, VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME));

        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head>" +
                "<body>" +
                "You had an outgoing call of 1:01:05 duration." +
                "<hr style=\"border: none; background-color: #cccccc; height: 1px;\"><small>" +
                "Called: <a href=\"tel:+12345678901\">+12345678901</a> (John Dou)" +
                "<br>" +
                "Last known device location: <a href=\"https://www.google.com/maps/place/60.555+30.555/@60.555,30.555\">60&#176;33'17\"N, 30&#176;33'17\"W</a>" +
                "<br>" +
                "Sent from " + deviceName + " at February 2, 2016 3:04:05 AM EST" +
                "</small></body></html>", formatter.getBody());
    }

    /**
     * Check missed call email body when all required information is present.
     *
     * @throws Exception when fails
     */
    @Test
    public void testAllContentMissedCall() throws Exception {
        String deviceName = Settings.getDeviceName(getContext());
        long start = new GregorianCalendar(2016, 1, 2, 3, 4, 5).getTime().getTime();
        long end = new GregorianCalendar(2016, 1, 2, 4, 5, 10).getTime().getTime();

        PhoneEvent message = new PhoneEvent("+12345678901", true, start, end, true,
                null, new GeoCoordinates(60.555, 30.555), true, null, PhoneEvent.State.PENDING);

        MailFormatter formatter = new MailFormatter(context, message);
        formatter.setDeviceName(deviceName);
        formatter.setContentOptions(Util.asSet(VAL_PREF_EMAIL_CONTENT_CONTACT, VAL_PREF_EMAIL_CONTENT_LOCATION,
                VAL_PREF_EMAIL_CONTENT_DEVICE_NAME, VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME));

        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head>" +
                "<body>" +
                "You had a missed call." +
                "<hr style=\"border: none; background-color: #cccccc; height: 1px;\"><small>" +
                "Caller: <a href=\"tel:+12345678901\">+12345678901</a> (Unknown contact)" +
                "<br>" +
                "Last known device location: <a href=\"https://www.google.com/maps/place/60.555+30.555/@60.555,30.555\">60&#176;33'17\"N, 30&#176;33'17\"W</a>" +
                "<br>" +
                "Sent from " + deviceName + " at February 2, 2016 3:04:05 AM EST" +
                "</small></body></html>", formatter.getBody());
    }

    /**
     * Check email body with valid non-default locale specified.
     *
     * @throws Exception when fails
     */
    @Test
    public void testNonDefaultLocale() throws Exception {
        String deviceName = Settings.getDeviceName(getContext());

        TimeZone timeZone = TimeZone.getTimeZone("EST");
        GregorianCalendar calendar = new GregorianCalendar(timeZone);
        calendar.set(2016, 1, 2, 3, 4, 5);
        long start = calendar.getTime().getTime();
        calendar.set(2016, 1, 2, 3, 4, 10);
        long end = calendar.getTime().getTime();

        PhoneEvent message = new PhoneEvent("+12345678901", true, start, end, true,
                null, new GeoCoordinates(60.555, 30.555), true,
                null, PhoneEvent.State.PENDING);

        MailFormatter formatter = new MailFormatter(context, message);
        formatter.setDeviceName(deviceName);
        formatter.setLocale("ru_RU");
        formatter.setContentOptions(Util.asSet(VAL_PREF_EMAIL_CONTENT_CONTACT, VAL_PREF_EMAIL_CONTENT_LOCATION,
                VAL_PREF_EMAIL_CONTENT_DEVICE_NAME, VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME));

        assertEquals("[SMailer] Пропущенный звонок от +12345678901", formatter.getSubject());
        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\">" +
                "</head><body>" +
                "Пропущенный звонок." +
                "<hr style=\"border: none; background-color: #cccccc; height: 1px;\"><small>" +
                "Вам звонил: <a href=\"tel:+12345678901\">+12345678901</a> (Неизвестный контакт)<br>" +
                "Последнее известное местоположение: <a href=\"https://www.google.com/maps/place/60.555+30.555/@60.555,30.555\">" +
                "60&#176;33'17\"N, 30&#176;33'17\"W</a><br>" +
                "Отправлено с устройства \"" + deviceName + "\" 2 февраля 2016 г. 3:04:05 GMT-05:00" +
                "</small></body></html>", formatter.getBody());

        formatter.setLocale(null); /* should set default locale */
        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\">" +
                "</head><body>" +
                "You had a missed call." +
                "<hr style=\"border: none; background-color: #cccccc; height: 1px;\"><small>" +
                "Caller: <a href=\"tel:+12345678901\">+12345678901</a> (Unknown contact)<br>" +
                "Last known device location: <a href=\"https://www.google.com/maps/place/60.555+30.555/@60.555,30.555\">" +
                "60&#176;33'17\"N, 30&#176;33'17\"W</a><br>" +
                "Sent from " + deviceName + " at February 2, 2016 3:04:05 AM EST" +
                "</small></body></html>", formatter.getBody());
    }

    /**
     * Check email body with invalid non-default locale specified.
     *
     * @throws Exception when fails
     */
    @Test
    public void testInvalidLocale() throws Exception {
        String deviceName = Settings.getDeviceName(getContext());
        long start = new GregorianCalendar(2016, 1, 2, 3, 4, 5).getTime().getTime();
        long end = new GregorianCalendar(2016, 1, 2, 4, 5, 10).getTime().getTime();

        PhoneEvent message = new PhoneEvent("+12345678901", true, start, end, true,
                null, new GeoCoordinates(60.555, 30.555), true, null, PhoneEvent.State.PENDING);

        MailFormatter formatter = new MailFormatter(context, message);
        formatter.setDeviceName(deviceName);
        formatter.setLocale("blah-blah"); /* should set default locale */

        formatter.setContentOptions(Util.asSet(VAL_PREF_EMAIL_CONTENT_CONTACT, VAL_PREF_EMAIL_CONTENT_LOCATION,
                VAL_PREF_EMAIL_CONTENT_DEVICE_NAME, VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME));

        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\">" +
                "</head><body>" +
                "You had a missed call." +
                "<hr style=\"border: none; background-color: #cccccc; height: 1px;\"><small>" +
                "Caller: <a href=\"tel:+12345678901\">+12345678901</a> (Unknown contact)<br>" +
                "Last known device location: <a href=\"https://www.google.com/maps/place/60.555+30.555/@60.555,30.555\">" +
                "60&#176;33'17\"N, 30&#176;33'17\"W</a><br>" +
                "Sent from " + deviceName + " at February 2, 2016 3:04:05 AM EST" +
                "</small></body></html>", formatter.getBody());
    }

}