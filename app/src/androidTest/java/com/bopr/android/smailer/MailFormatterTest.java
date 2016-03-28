package com.bopr.android.smailer;

import android.app.Application;
import android.content.ContentProviderOperation;
import android.test.ApplicationTestCase;

import com.bopr.android.smailer.util.Util;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import static android.provider.ContactsContract.AUTHORITY;
import static android.provider.ContactsContract.CommonDataKinds.Phone;
import static android.provider.ContactsContract.CommonDataKinds.StructuredName;
import static android.provider.ContactsContract.Data;
import static android.provider.ContactsContract.RawContacts;
import static com.bopr.android.smailer.Settings.VAL_PREF_EMAIL_CONTENT_CONTACT;
import static com.bopr.android.smailer.Settings.VAL_PREF_EMAIL_CONTENT_DEVICE_NAME;
import static com.bopr.android.smailer.Settings.VAL_PREF_EMAIL_CONTENT_LOCATION;
import static com.bopr.android.smailer.Settings.VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME;

/**
 * {@link MailFormatter} tester.
 */
public class MailFormatterTest extends ApplicationTestCase<Application> {

    public MailFormatterTest() {
        super(Application.class);
        Locale.setDefault(Locale.US);
        TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"));
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        if (Contacts.getContactName(getContext(), "+12345678901") == null) {
            ArrayList<ContentProviderOperation> ops = new ArrayList<>();

            ops.add(ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
                    .withValue(RawContacts.ACCOUNT_TYPE, null)
                    .withValue(RawContacts.ACCOUNT_NAME, null)
                    .build());

            ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                    .withValueBackReference(Data.RAW_CONTACT_ID, 0)
                    .withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(StructuredName.DISPLAY_NAME, "John Dou")
                    .build());

            ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                    .withValueBackReference(Data.RAW_CONTACT_ID, 0)
                    .withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
                    .withValue(Phone.NUMBER, "+12345678901")
                    .withValue(Phone.TYPE, Phone.TYPE_MOBILE)
                    .build());

            getContext().getContentResolver().applyBatch(AUTHORITY, ops);
        }
    }

    /**
     * Check formatting incoming sms email subject.
     *
     * @throws Exception when fails
     */
    public void testIncomingSmsSubject() throws Exception {
        MailMessage message = new MailMessage("+70123456789", true, null, null, false,
                true, "Email body text", null, false, null);

        MailFormatter formatter = new MailFormatter(message, getContext().getResources(), null, null);

        assertEquals("[SMailer] Incoming SMS from +70123456789", formatter.getSubject());
    }

    /**
     * Check formatting incoming call email subject.
     *
     * @throws Exception when fails
     */
    public void testIncomingCallSubject() throws Exception {
        MailMessage message = new MailMessage("+70123456789", true, null, null, false,
                false, null, null, false, null);

        MailFormatter formatter = new MailFormatter(message, getContext().getResources(), null, null);

        assertEquals("[SMailer] Incoming call from +70123456789", formatter.getSubject());
    }

    /**
     * Check formatting outgoing call email subject.
     *
     * @throws Exception when fails
     */
    public void testOutgoingCallSubject() throws Exception {
        MailMessage message = new MailMessage("+70123456789", false, null, null, false,
                false, null, null, false, null);

        MailFormatter formatter = new MailFormatter(message, getContext().getResources(), null, null);

        assertEquals("[SMailer] Outgoing call to +70123456789", formatter.getSubject());
    }

    /**
     * Check formatting outgoing call email subject.
     *
     * @throws Exception when fails
     */
    public void testMissedCallSubject() throws Exception {
        MailMessage message = new MailMessage("+70123456789", false, null, null, true,
                false, null, null, false, null);

        MailFormatter formatter = new MailFormatter(message, getContext().getResources(), null, null);

        assertEquals("[SMailer] Missed call from +70123456789", formatter.getSubject());
    }

    /**
     * Check that email body does not contain any footer when no options have been chosen.
     *
     * @throws Exception when fails
     */
    public void testNoBodyFooter() throws Exception {
        MailMessage message = new MailMessage("+70123456789", true, null, null, false,
                true, "Email body text", null, false, null);

        MailFormatter formatter = new MailFormatter(message, getContext().getResources(), null, null);

        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body>" +
                "Email body text" +
                "</body></html>", formatter.getBody());
    }

    /**
     * Check email body footer with {@link Settings#VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME} option switched on.
     *
     * @throws Exception when fails
     */
    public void testFooterTimeOption() throws Exception {
        long time = new GregorianCalendar(2016, 1, 2, 3, 4, 5).getTime().getTime();
        MailMessage message = new MailMessage("+70123456789", true, time, null, false,
                true, "Email body text", null, false, null);

        MailFormatter formatter = new MailFormatter(message, getContext().getResources(), null, null);
        formatter.setContentOptions(Util.asSet(VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME));

        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body>" +
                "Email body text" +
                "<hr style=\"border: none; background-color: #cccccc; height: 1px;\">" +
                "Sent at February 2, 2016 3:04:05 AM EST" +
                "</body></html>", formatter.getBody());
    }

    /**
     * Check email body footer with {@link Settings#VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME} option
     * switched on and no time specified in message.
     *
     * @throws Exception when fails
     */
    public void testFooterNoTime() throws Exception {
        MailMessage message = new MailMessage("+70123456789", true, null, null, false,
                true, "Email body text", null, false, null);

        MailFormatter formatter = new MailFormatter(message, getContext().getResources(), null, null);
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
    public void testFooterDeviceNameOption() throws Exception {
        String deviceName = Settings.getDeviceName();
        MailMessage message = new MailMessage("+70123456789", true, null, null, false,
                true, "Email body text", null, false, null);

        MailFormatter formatter = new MailFormatter(message, getContext().getResources(), null, deviceName);
        formatter.setContentOptions(Util.asSet(VAL_PREF_EMAIL_CONTENT_DEVICE_NAME));

        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body>" +
                "Email body text" +
                "<hr style=\"border: none; background-color: #cccccc; height: 1px;\">" +
                "Sent from " + deviceName +
                "</body></html>", formatter.getBody());
    }

    /**
     * Check email body footer with {@link Settings#VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME} and
     * {@link Settings#VAL_PREF_EMAIL_CONTENT_DEVICE_NAME} options switched on.
     *
     * @throws Exception when fails
     */
    public void testFooterDeviceNameTimeOption() throws Exception {
        String deviceName = Settings.getDeviceName();
        long time = new GregorianCalendar(2016, 1, 2, 3, 4, 5).getTime().getTime();
        MailMessage message = new MailMessage("+70123456789", true, time, null, false,
                true, "Email body text", null, false, null);

        MailFormatter formatter = new MailFormatter(message, getContext().getResources(), null, deviceName);
        formatter.setContentOptions(Util.asSet(VAL_PREF_EMAIL_CONTENT_DEVICE_NAME, VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME));


        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body>" +
                "Email body text" +
                "<hr style=\"border: none; background-color: #cccccc; height: 1px;\">" +
                "Sent from " + deviceName + " at February 2, 2016 3:04:05 AM EST" +
                "</body></html>", formatter.getBody());
    }

    /**
     * Check email body footer with {@link Settings#VAL_PREF_EMAIL_CONTENT_LOCATION} option switched on.
     *
     * @throws Exception when fails
     */
    public void testFooterLocation() throws Exception {
        MailMessage message = new MailMessage("+70123456789", true, null, null, false, true,
                "Email body text", new GeoCoordinates(60.555, 30.555), false, null);

        MailFormatter formatter = new MailFormatter(message, getContext().getResources(), null, null);
        formatter.setContentOptions(Util.asSet(VAL_PREF_EMAIL_CONTENT_LOCATION));

        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body>" +
                "Email body text" +
                "<hr style=\"border: none; background-color: #cccccc; height: 1px;\">" +
                "Last known device location: <a href=\"http://maps.google.com/maps/place/60.555,30.555\">" +
                "60&#176;33'17\"N, 30&#176;33'17\"W</a>" +
                "</body></html>", formatter.getBody());
    }

    /**
     * Check email body footer with {@link Settings#VAL_PREF_EMAIL_CONTENT_DEVICE_NAME} option
     * switched on and ni location specified in message.
     *
     * @throws Exception when fails
     */
    public void testFooterNoLocation() throws Exception {
        MailMessage message = new MailMessage("+70123456789", true, null, null, false,
                true, "Email body text", null, false, null);

        MailFormatter formatter = new MailFormatter(message, getContext().getResources(), null, null);
        formatter.setContentOptions(Util.asSet(VAL_PREF_EMAIL_CONTENT_LOCATION));

        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body>" +
                "Email body text" +
                "<hr style=\"border: none; background-color: #cccccc; height: 1px;\">" +
                "Last known device location: unknown" +
                "</body></html>", formatter.getBody());
    }

    /**
     * Check email body footer with {@link Settings#VAL_PREF_EMAIL_CONTENT_CONTACT} option switched on.
     *
     * @throws Exception when fails
     */
    public void testContactName() throws Exception {
        MailMessage message = new MailMessage("+12345678901", true, null, null, false,
                true, "Email body text", null, false, null);

        MailFormatter formatter = new MailFormatter(message, getContext().getResources(),
                Contacts.getContactName(getContext(), "+12345678901"), null);
        formatter.setContentOptions(Util.asSet(VAL_PREF_EMAIL_CONTENT_CONTACT));

        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body>" +
                "Email body text" +
                "<hr style=\"border: none; background-color: #cccccc; height: 1px;\">" +
                "Sender: <a href=\"tel:+12345678901\">+12345678901</a> (John Dou)" +
                "</body></html>", formatter.getBody());
    }

    /**
     * Check email body footer with {@link Settings#VAL_PREF_EMAIL_CONTENT_CONTACT} option switched on
     * and unknown contact name.
     *
     * @throws Exception when fails
     */
    public void testUnknownContactName() throws Exception {
        MailMessage message = new MailMessage("+12345678901", true, null, null, false,
                true, "Email body text", null, false, null);

        MailFormatter formatter = new MailFormatter(message, getContext().getResources(),
                null, null);
        formatter.setContentOptions(Util.asSet(VAL_PREF_EMAIL_CONTENT_CONTACT));

        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body>" +
                "Email body text" +
                "<hr style=\"border: none; background-color: #cccccc; height: 1px;\">" +
                "Sender: <a href=\"tel:+12345678901\">+12345678901</a> (Unknown contact)" +
                "</body></html>", formatter.getBody());
    }

    /**
     * Check incoming call email body.
     *
     * @throws Exception when fails
     */
    public void testIncomingCallBody() throws Exception {
        long start = new GregorianCalendar(2016, 1, 2, 3, 4, 5).getTime().getTime();
        long end = new GregorianCalendar(2016, 1, 2, 4, 5, 10).getTime().getTime();
        MailMessage message = new MailMessage("+70123456789", true, start, end, false,
                false, null, null, false, null);

        MailFormatter formatter = new MailFormatter(message, getContext().getResources(), null, null);

        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body>" +
                "You had an incoming call of 1:01:05 duration." +
                "</body></html>", formatter.getBody());
    }

    /**
     * Check outgoing call email body.
     *
     * @throws Exception when fails
     */
    public void testOutgoingCallBody() throws Exception {
        long start = new GregorianCalendar(2016, 1, 2, 3, 4, 5).getTime().getTime();
        long end = new GregorianCalendar(2016, 1, 2, 4, 5, 15).getTime().getTime();
        MailMessage message = new MailMessage("+70123456789", false, start, end, false,
                false, null, null, false, null);

        MailFormatter formatter = new MailFormatter(message, getContext().getResources(), null, null);

        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body>" +
                "You had an outgoing call of 1:01:10 duration." +
                "</body></html>", formatter.getBody());
    }

    /**
     * Check missed call email body.
     *
     * @throws Exception when fails
     */
    public void testMissedCallBody() throws Exception {
        long start = new GregorianCalendar(2016, 1, 2, 3, 4, 5).getTime().getTime();
        MailMessage message = new MailMessage("+70123456789", false, start, null, true,
                false, null, null, false, null);

        MailFormatter formatter = new MailFormatter(message, getContext().getResources(), null, null);

        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body>" +
                "You had a missed call." +
                "</body></html>", formatter.getBody());
    }

    /**
     * Check incoming call email body when all required information is present.
     *
     * @throws Exception when fails
     */
    public void testAllIncomingContentCall() throws Exception {
        String deviceName = Settings.getDeviceName();
        long start = new GregorianCalendar(2016, 1, 2, 3, 4, 5).getTime().getTime();
        long end = new GregorianCalendar(2016, 1, 2, 4, 5, 10).getTime().getTime();

        MailMessage message = new MailMessage("+12345678901", true, start, end, false,
                false, null, new GeoCoordinates(60.555, 30.555), true, null);

        MailFormatter formatter = new MailFormatter(message, getContext().getResources(),
                Contacts.getContactName(getContext(), "+12345678901"), deviceName);
        formatter.setContentOptions(Util.asSet(VAL_PREF_EMAIL_CONTENT_CONTACT, VAL_PREF_EMAIL_CONTENT_LOCATION,
                VAL_PREF_EMAIL_CONTENT_DEVICE_NAME, VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME));

        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head>" +
                "<body>" +
                "You had an incoming call of 1:01:05 duration." +
                "<hr style=\"border: none; background-color: #cccccc; height: 1px;\">" +
                "Caller: <a href=\"tel:+12345678901\">+12345678901</a> (John Dou)" +
                "<br>" +
                "Last known device location: <a href=\"http://maps.google.com/maps/place/60.555,30.555\">60&#176;33'17\"N, 30&#176;33'17\"W</a>" +
                "<br>" +
                "Sent from " + deviceName + " at February 2, 2016 3:04:05 AM EST" +
                "</body></html>", formatter.getBody());
    }

    /**
     * Check outgoing call email body when all required information is present.
     *
     * @throws Exception when fails
     */
    public void testAllOutgoingContentCall() throws Exception {
        String deviceName = Settings.getDeviceName();
        long start = new GregorianCalendar(2016, 1, 2, 3, 4, 5).getTime().getTime();
        long end = new GregorianCalendar(2016, 1, 2, 4, 5, 10).getTime().getTime();

        MailMessage message = new MailMessage("+12345678901", false, start, end, false,
                false, null, new GeoCoordinates(60.555, 30.555), true, null);

        MailFormatter formatter = new MailFormatter(message, getContext().getResources(),
                Contacts.getContactName(getContext(), "+12345678901"), deviceName);
        formatter.setContentOptions(Util.asSet(VAL_PREF_EMAIL_CONTENT_CONTACT, VAL_PREF_EMAIL_CONTENT_LOCATION,
                VAL_PREF_EMAIL_CONTENT_DEVICE_NAME, VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME));

        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head>" +
                "<body>" +
                "You had an outgoing call of 1:01:05 duration." +
                "<hr style=\"border: none; background-color: #cccccc; height: 1px;\">" +
                "Called: <a href=\"tel:+12345678901\">+12345678901</a> (John Dou)" +
                "<br>" +
                "Last known device location: <a href=\"http://maps.google.com/maps/place/60.555,30.555\">60&#176;33'17\"N, 30&#176;33'17\"W</a>" +
                "<br>" +
                "Sent from " + deviceName + " at February 2, 2016 3:04:05 AM EST" +
                "</body></html>", formatter.getBody());
    }

    /**
     * Check missed call email body when all required information is present.
     *
     * @throws Exception when fails
     */
    public void testAllContentMissedCall() throws Exception {
        String deviceName = Settings.getDeviceName();
        long start = new GregorianCalendar(2016, 1, 2, 3, 4, 5).getTime().getTime();
        long end = new GregorianCalendar(2016, 1, 2, 4, 5, 10).getTime().getTime();

        MailMessage message = new MailMessage("+12345678901", true, start, end, true,
                false, null, new GeoCoordinates(60.555, 30.555), true, null);

        MailFormatter formatter = new MailFormatter(message, getContext().getResources(),
                null, deviceName);
        formatter.setContentOptions(Util.asSet(VAL_PREF_EMAIL_CONTENT_CONTACT, VAL_PREF_EMAIL_CONTENT_LOCATION,
                VAL_PREF_EMAIL_CONTENT_DEVICE_NAME, VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME));

        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head>" +
                "<body>" +
                "You had a missed call." +
                "<hr style=\"border: none; background-color: #cccccc; height: 1px;\">" +
                "Caller: <a href=\"tel:+12345678901\">+12345678901</a> (Unknown contact)" +
                "<br>" +
                "Last known device location: <a href=\"http://maps.google.com/maps/place/60.555,30.555\">60&#176;33'17\"N, 30&#176;33'17\"W</a>" +
                "<br>" +
                "Sent from " + deviceName + " at February 2, 2016 3:04:05 AM EST" +
                "</body></html>", formatter.getBody());
    }

    /**
     * Check email body with valid non-default locale specified.
     *
     * @throws Exception when fails
     */
    public void testNonDefaultLocale() throws Exception {
        String deviceName = Settings.getDeviceName();

        TimeZone timeZone = TimeZone.getTimeZone("EST");
        GregorianCalendar calendar = new GregorianCalendar(timeZone);
        calendar.set(2016, 1, 2, 3, 4, 5);
        long start = calendar.getTime().getTime();
        calendar.set(2016, 1, 2, 3, 4, 10);
        long end = calendar.getTime().getTime();

        MailMessage message = new MailMessage("+12345678901", true, start, end, true,
                false, null, new GeoCoordinates(60.555, 30.555), true,
                null);

        MailFormatter formatter = new MailFormatter(message, getContext().getResources(), null,
                deviceName);
        formatter.setLocale("ru_RU");
        formatter.setContentOptions(Util.asSet(VAL_PREF_EMAIL_CONTENT_CONTACT, VAL_PREF_EMAIL_CONTENT_LOCATION,
                VAL_PREF_EMAIL_CONTENT_DEVICE_NAME, VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME));

        assertEquals("[SMailer] Пропущенный звонок от +12345678901", formatter.getSubject());
        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\">" +
                "</head><body>" +
                "Пропущенный звонок." +
                "<hr style=\"border: none; background-color: #cccccc; height: 1px;\">" +
                "Вам звонил: <a href=\"tel:+12345678901\">+12345678901</a> (Неизвестный контакт)<br>" +
                "Последнее известное местоположение: <a href=\"http://maps.google.com/maps/place/60.555,30.555\">" +
                "60&#176;33'17\"N, 30&#176;33'17\"W</a><br>" +
                "Отправлено с " + deviceName + " 2 февраля 2016 г. 3:04:05 GMT-05:00" +
                "</body></html>", formatter.getBody());

        formatter.setLocale(null); /* should set default locale */
        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\">" +
                "</head><body>" +
                "You had a missed call." +
                "<hr style=\"border: none; background-color: #cccccc; height: 1px;\">" +
                "Caller: <a href=\"tel:+12345678901\">+12345678901</a> (Unknown contact)<br>" +
                "Last known device location: <a href=\"http://maps.google.com/maps/place/60.555,30.555\">" +
                "60&#176;33'17\"N, 30&#176;33'17\"W</a><br>" +
                "Sent from " + deviceName + " at February 2, 2016 3:04:05 AM EST" +
                "</body></html>", formatter.getBody());
    }

    /**
     * Check email body with invalid non-default locale specified.
     *
     * @throws Exception when fails
     */
    public void testInvalidLocale() throws Exception {
        String deviceName = Settings.getDeviceName();
        long start = new GregorianCalendar(2016, 1, 2, 3, 4, 5).getTime().getTime();
        long end = new GregorianCalendar(2016, 1, 2, 4, 5, 10).getTime().getTime();

        MailMessage message = new MailMessage("+12345678901", true, start, end, true,
                false, null, new GeoCoordinates(60.555, 30.555), true, null);

        MailFormatter formatter = new MailFormatter(message, getContext().getResources(), null,
                deviceName);
        formatter.setLocale("blah-blah"); /* should set default locale */

        formatter.setContentOptions(Util.asSet(VAL_PREF_EMAIL_CONTENT_CONTACT, VAL_PREF_EMAIL_CONTENT_LOCATION,
                VAL_PREF_EMAIL_CONTENT_DEVICE_NAME, VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME));

        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\">" +
                "</head><body>" +
                "You had a missed call." +
                "<hr style=\"border: none; background-color: #cccccc; height: 1px;\">" +
                "Caller: <a href=\"tel:+12345678901\">+12345678901</a> (Unknown contact)<br>" +
                "Last known device location: <a href=\"http://maps.google.com/maps/place/60.555,30.555\">" +
                "60&#176;33'17\"N, 30&#176;33'17\"W</a><br>" +
                "Sent from " + deviceName + " at February 2, 2016 3:04:05 AM EST" +
                "</body></html>", formatter.getBody());
    }

}