package com.bopr.android.smailer;

import android.app.Application;
import android.content.ContentProviderOperation;
import android.location.Location;
import android.test.ApplicationTestCase;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Locale;

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
        MailMessage message = new MailMessage("+70123456789", true, 0, 0, false,
                true, "Email body text", 0, 0, false);
        MailerProperties properties = new MailerProperties();

        MailFormatter formatter = new MailFormatter(message, getContext().getResources(), properties, null, null);

        String text = formatter.getSubject();
        assertEquals("[SMailer] Incoming SMS from +70123456789", text);
    }

    /**
     * Check formatting incoming call email subject.
     *
     * @throws Exception when fails
     */
    public void testIncomingCallSubject() throws Exception {
        MailMessage message = new MailMessage("+70123456789", true, 0, 0, false,
                false, null, 0, 0, false);
        MailerProperties properties = new MailerProperties();

        MailFormatter formatter = new MailFormatter(message, getContext().getResources(), properties, null, null);

        String text = formatter.getSubject();
        assertEquals("[SMailer] Incoming call from +70123456789", text);
    }

    /**
     * Check formatting outgoing call email subject.
     *
     * @throws Exception when fails
     */
    public void testOutgoingCallSubject() throws Exception {
        MailMessage message = new MailMessage("+70123456789", false, 0, 0, false,
                false, null, 0, 0, false);
        MailerProperties properties = new MailerProperties();

        MailFormatter formatter = new MailFormatter(message, getContext().getResources(), properties, null, null);

        String text = formatter.getSubject();
        assertEquals("[SMailer] Outgoing call to +70123456789", text);
    }

    /**
     * Check formatting outgoing call email subject.
     *
     * @throws Exception when fails
     */
    public void testMissedCallSubject() throws Exception {
        MailMessage message = new MailMessage("+70123456789", false, 0, 0, true,
                false, null, 0, 0, false);
        MailerProperties properties = new MailerProperties();

        MailFormatter formatter = new MailFormatter(message, getContext().getResources(), properties, null, null);

        String text = formatter.getSubject();
        assertEquals("[SMailer] Missed call from +70123456789", text);
    }

    /**
     * Check that email body does not contain any footer when no options have been chosen.
     *
     * @throws Exception when fails
     */
    public void testNoBodyFooter() throws Exception {
        MailMessage message = new MailMessage("+70123456789", true, 0, 0, false,
                true, "Email body text", 0, 0, false);

        MailerProperties properties = new MailerProperties();

        MailFormatter formatter = new MailFormatter(message, getContext().getResources(), properties, null, null);

        String text = formatter.getBody();
        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body>" +
                "Email body text" +
                "  </body></html>", text);
    }

    /**
     * Check email body footer with different options.
     *
     * @throws Exception when fails
     */
    public void testFooterTimeOption() throws Exception {
        long time = new GregorianCalendar(2016, 1, 2, 3, 4, 5).getTime().getTime();
        MailMessage message = new MailMessage("+70123456789", true, time, 0, false,
                true, "Email body text", 0, 0, false);

        MailerProperties properties = new MailerProperties();
        properties.setContentOptions(VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME);

        MailFormatter formatter = new MailFormatter(message, getContext().getResources(), properties, null, null);

        String text = formatter.getBody();
        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body>" +
                "Email body text" +
                " <hr style=\"border: none; background-color: #cccccc; height: 1px;\">" +
                " Sent at Feb 2, 2016 3:04:05 AM" +
                "</body></html>", text);
    }

    /**
     * Check email body footer with different options.
     *
     * @throws Exception when fails
     */
    public void testFooterDeviceNameOption() throws Exception {
        String deviceName = Settings.getDeviceName();
        MailMessage message = new MailMessage("+70123456789", true, 0, 0, false,
                true, "Email body text", 0, 0, false);

        MailerProperties properties = new MailerProperties();
        properties.setContentOptions(VAL_PREF_EMAIL_CONTENT_DEVICE_NAME);

        MailFormatter formatter = new MailFormatter(message, getContext().getResources(), properties, null, deviceName);

        String text = formatter.getBody();
        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body>" +
                "Email body text" +
                " <hr style=\"border: none; background-color: #cccccc; height: 1px;\">" +
                " Sent from " + deviceName +
                "</body></html>", text);
    }

    /**
     * Check email body footer with different options.
     *
     * @throws Exception when fails
     */
    public void testFooterDeviceNameTimeOption() throws Exception {
        String deviceName = Settings.getDeviceName();
        long time = new GregorianCalendar(2016, 1, 2, 3, 4, 5).getTime().getTime();
        MailMessage message = new MailMessage("+70123456789", true, time, 0, false,
                true, "Email body text", 0, 0, false);

        MailerProperties properties = new MailerProperties();
        properties.setContentOptions(VAL_PREF_EMAIL_CONTENT_DEVICE_NAME, VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME);

        MailFormatter formatter = new MailFormatter(message, getContext().getResources(), properties, null, deviceName);

        String text = formatter.getBody();
        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body>" +
                "Email body text" +
                " <hr style=\"border: none; background-color: #cccccc; height: 1px;\">" +
                " Sent from " + deviceName + " at Feb 2, 2016 3:04:05 AM" +
                "</body></html>", text);
    }

    /**
     * Check email body footer with different options.
     *
     * @throws Exception when fails
     */
    public void testFooterLocation() throws Exception {
        Location location = new Location("provider");
        location.setLatitude(60.555);
        location.setLongitude(30.555);
        MailMessage message = new MailMessage("+70123456789", true, 0, 0, false,
                true, "Email body text", location.getLatitude(), location.getLongitude(), false);

        MailerProperties properties = new MailerProperties();
        properties.setContentOptions(VAL_PREF_EMAIL_CONTENT_LOCATION);

        MailFormatter formatter = new MailFormatter(message, getContext().getResources(), properties, null, null);

        String text = formatter.getBody();
        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body>" +
                "Email body text" +
                " <hr style=\"border: none; background-color: #cccccc; height: 1px;\">" +
                " Last known device location: <a href=\"http://maps.google.com/maps/place/60.555,30.555\">" +
                "60&#176;33'17\"N, 30&#176;33'17\"W</a>" +
                "</body></html>", text);
    }

    /**
     * Check email body footer with different options.
     *
     * @throws Exception when fails
     */
    public void testFooterNoLocation() throws Exception {
        MailMessage message = new MailMessage("+70123456789", true, 0, 0, false,
                true, "Email body text", 0, 0, false);

        MailerProperties properties = new MailerProperties();
        properties.setContentOptions(VAL_PREF_EMAIL_CONTENT_LOCATION);

        MailFormatter formatter = new MailFormatter(message, getContext().getResources(), properties, null, null);

        String text = formatter.getBody();
        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body>" +
                "Email body text " +
                "<hr style=\"border: none; background-color: #cccccc; height: 1px;\"> " +
                "Last known device location: unknown" +
                "</body></html>", text);
    }

    /**
     * Check email body footer with different options.
     *
     * @throws Exception when fails
     */
    public void testContactName() throws Exception {

        MailMessage message = new MailMessage("+12345678901", true, 0, 0, false,
                true, "Email body text", 0, 0, false);

        MailerProperties properties = new MailerProperties();
        properties.setContentOptions(VAL_PREF_EMAIL_CONTENT_CONTACT);

        MailFormatter formatter = new MailFormatter(message, getContext().getResources(),
                properties, Contacts.getContactName(getContext(), "+12345678901"), null);

        String text = formatter.getBody();
        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body>" +
                "Email body text" +
                " <hr style=\"border: none; background-color: #cccccc; height: 1px;\">" +
                " Sender: <a href=\"tel:+12345678901\">+12345678901 (John Dou)</a>" +
                "</body></html>", text);
    }

    /**
     * Check email body footer with different options.
     *
     * @throws Exception when fails
     */
    public void testUnknownContactName() throws Exception {

        MailMessage message = new MailMessage("+12345678901", true, 0, 0, false,
                true, "Email body text", 0, 0, false);

        MailerProperties properties = new MailerProperties();
        properties.setContentOptions(VAL_PREF_EMAIL_CONTENT_CONTACT);

        MailFormatter formatter = new MailFormatter(message, getContext().getResources(),
                properties, null, null);

        String text = formatter.getBody();
        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body>" +
                "Email body text" +
                " <hr style=\"border: none; background-color: #cccccc; height: 1px;\">" +
                " Sender: <a href=\"tel:+12345678901\">+12345678901 (Unknown contact)</a>" +
                "</body></html>", text);
    }

    /**
     * Check email body footer with different options.
     *
     * @throws Exception when fails
     */
    public void testIncomingCallBody() throws Exception {
        long start = new GregorianCalendar(2016, 1, 2, 3, 4, 5).getTime().getTime();
        long end = new GregorianCalendar(2016, 1, 2, 4, 5, 10).getTime().getTime();
        MailMessage message = new MailMessage("+70123456789", true, start, end, false,
                false, null, 0, 0, false);

        MailerProperties properties = new MailerProperties();

        MailFormatter formatter = new MailFormatter(message, getContext().getResources(), properties, null, null);

        String text = formatter.getBody();
        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body>" +
                "You had an incoming call of 1:01:05 duration." +
                "  </body></html>", text);
    }

    /**
     * Check email body footer with different options.
     *
     * @throws Exception when fails
     */
    public void testOutgoingCallBody() throws Exception {
        long start = new GregorianCalendar(2016, 1, 2, 3, 4, 5).getTime().getTime();
        long end = new GregorianCalendar(2016, 1, 2, 4, 5, 15).getTime().getTime();
        MailMessage message = new MailMessage("+70123456789", false, start, end, false,
                false, null, 0, 0, false);

        MailerProperties properties = new MailerProperties();

        MailFormatter formatter = new MailFormatter(message, getContext().getResources(), properties, null, null);

        String text = formatter.getBody();
        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body>" +
                "You had an outgoing call of 1:01:10 duration." +
                "  </body></html>", text);
    }

    /**
     * Check email body footer with different options.
     *
     * @throws Exception when fails
     */
    public void testMissedCallBody() throws Exception {
        long start = new GregorianCalendar(2016, 1, 2, 3, 4, 5).getTime().getTime();
        MailMessage message = new MailMessage("+70123456789", false, start, 0, true,
                false, null, 0, 0, false);

        MailerProperties properties = new MailerProperties();

        MailFormatter formatter = new MailFormatter(message, getContext().getResources(), properties, null, null);

        String text = formatter.getBody();
        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body>" +
                "You had a missed call." +
                "  </body></html>", text);
    }

    /**
     * Check email body footer with different options.
     *
     * @throws Exception when fails
     */
    public void testAllIncomingContentCall() throws Exception {
        String deviceName = Settings.getDeviceName();
        long start = new GregorianCalendar(2016, 1, 2, 3, 4, 5).getTime().getTime();
        long end = new GregorianCalendar(2016, 1, 2, 4, 5, 10).getTime().getTime();

        MailMessage message = new MailMessage("+12345678901", true, start, end, false, false, null, 60.555, 30.555, true);

        MailerProperties properties = new MailerProperties();
        properties.setContentOptions(VAL_PREF_EMAIL_CONTENT_CONTACT, VAL_PREF_EMAIL_CONTENT_LOCATION,
                VAL_PREF_EMAIL_CONTENT_DEVICE_NAME, VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME);

        MailFormatter formatter = new MailFormatter(message, getContext().getResources(), properties,
                Contacts.getContactName(getContext(), "+12345678901"), deviceName);

        String text = formatter.getBody();
        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head>" +
                "<body>" +
                "You had an incoming call of 1:01:05 duration." +
                " <hr style=\"border: none; background-color: #cccccc; height: 1px;\">" +
                " Caller: <a href=\"tel:+12345678901\">+12345678901 (John Dou)</a>" +
                "<br>" +
                "Last known device location: <a href=\"http://maps.google.com/maps/place/60.555,30.555\">60&#176;33'17\"N, 30&#176;33'17\"W</a>" +
                "<br>" +
                "Sent from " + deviceName + " at Feb 2, 2016 3:04:05 AM" +
                "</body></html>", text);
    }

    /**
     * Check email body footer with different options.
     *
     * @throws Exception when fails
     */
    public void testAllOutgoingContentCall() throws Exception {
        String deviceName = Settings.getDeviceName();
        long start = new GregorianCalendar(2016, 1, 2, 3, 4, 5).getTime().getTime();
        long end = new GregorianCalendar(2016, 1, 2, 4, 5, 10).getTime().getTime();

        MailMessage message = new MailMessage("+12345678901", false, start, end, false, false, null, 60.555, 30.555, true);

        MailerProperties properties = new MailerProperties();
        properties.setContentOptions(VAL_PREF_EMAIL_CONTENT_CONTACT, VAL_PREF_EMAIL_CONTENT_LOCATION,
                VAL_PREF_EMAIL_CONTENT_DEVICE_NAME, VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME);

        MailFormatter formatter = new MailFormatter(message, getContext().getResources(), properties,
                Contacts.getContactName(getContext(), "+12345678901"), deviceName);

        String text = formatter.getBody();
        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head>" +
                "<body>" +
                "You had an outgoing call of 1:01:05 duration." +
                " <hr style=\"border: none; background-color: #cccccc; height: 1px;\">" +
                " Called: <a href=\"tel:+12345678901\">+12345678901 (John Dou)</a>" +
                "<br>" +
                "Last known device location: <a href=\"http://maps.google.com/maps/place/60.555,30.555\">60&#176;33'17\"N, 30&#176;33'17\"W</a>" +
                "<br>" +
                "Sent from " + deviceName + " at Feb 2, 2016 3:04:05 AM" +
                "</body></html>", text);
    }

    /**
     * Check email body footer with different options.
     *
     * @throws Exception when fails
     */
    public void testAllContentMissedCall() throws Exception {
        String deviceName = Settings.getDeviceName();
        long start = new GregorianCalendar(2016, 1, 2, 3, 4, 5).getTime().getTime();
        long end = new GregorianCalendar(2016, 1, 2, 4, 5, 10).getTime().getTime();

        MailMessage message = new MailMessage("+12345678901", true, start, end, true, false, null, 60.555, 30.555, true);

        MailerProperties properties = new MailerProperties();
        properties.setContentOptions(VAL_PREF_EMAIL_CONTENT_CONTACT, VAL_PREF_EMAIL_CONTENT_LOCATION,
                VAL_PREF_EMAIL_CONTENT_DEVICE_NAME, VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME);

        MailFormatter formatter = new MailFormatter(message, getContext().getResources(), properties,
                null, deviceName);

        String text = formatter.getBody();
        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head>" +
                "<body>" +
                "You had a missed call." +
                " <hr style=\"border: none; background-color: #cccccc; height: 1px;\">" +
                " Caller: <a href=\"tel:+12345678901\">+12345678901 (Unknown contact)</a>" +
                "<br>" +
                "Last known device location: <a href=\"http://maps.google.com/maps/place/60.555,30.555\">60&#176;33'17\"N, 30&#176;33'17\"W</a>" +
                "<br>" +
                "Sent from " + deviceName + " at Feb 2, 2016 3:04:05 AM" +
                "</body></html>", text);
    }

}