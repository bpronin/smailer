package com.bopr.android.smailer;

import android.app.Application;
import android.content.ContentProviderOperation;
import android.location.Location;
import android.test.ApplicationTestCase;

import com.bopr.android.smailer.util.ContactUtil;
import com.bopr.android.smailer.util.DeviceUtil;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Locale;

import static android.provider.ContactsContract.AUTHORITY;
import static android.provider.ContactsContract.CommonDataKinds.Phone;
import static android.provider.ContactsContract.CommonDataKinds.StructuredName;
import static android.provider.ContactsContract.Data;
import static android.provider.ContactsContract.RawContacts;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class MailFormatterTest extends ApplicationTestCase<Application> {

    public MailFormatterTest() {
        super(Application.class);
        Locale.setDefault(Locale.US);
    }

    /**
     * Check formatting incoming sms email subject.
     *
     * @throws Exception when fails
     */
    public void testIncomingSmsSubject() throws Exception {
        MailMessage message = new MailMessage("+70123456789", "Email body text", 0, null);
        MailerProperties properties = new MailerProperties();

        MailFormatter formatter = new MailFormatter(getContext(), properties, message);

        String text = formatter.getSubject();
        assertEquals("[SMailer] Incoming SMS from +70123456789", text);
    }

    /**
     * Check that email body does not contain any footer when no options have chosen.
     *
     * @throws Exception when fails
     */
    public void testNoBodyFooter() throws Exception {
        MailMessage message = new MailMessage("+70123456789", "Email body text", 0, null);

        MailerProperties properties = new MailerProperties();
        properties.setContentTime(false);
        properties.setContentDeviceName(false);
        properties.setContentLocation(false);
        properties.setContentContactName(false);

        MailFormatter formatter = new MailFormatter(getContext(), properties, message);

        String text = formatter.getBody();
        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; " +
                "charset=utf-8\"></head><body>Email body text" +
                "</body></html>", text);
    }

    /**
     * Check email body footer with different options.
     *
     * @throws Exception when fails
     */
    public void testFooterTimeOption() throws Exception {
        long time = new GregorianCalendar(2016, 1, 2, 3, 4, 5).getTime().getTime();
        MailMessage message = new MailMessage("+70123456789", "Email body text", time, null);

        MailerProperties properties = new MailerProperties();
        properties.setContentTime(true);
        properties.setContentDeviceName(false);
        properties.setContentLocation(false);
        properties.setContentContactName(false);

        MailFormatter formatter = new MailFormatter(getContext(), properties, message);

        String text = formatter.getBody();
        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; " +
                "charset=utf-8\"></head><body>Email body text<hr style=\"border: none; " +
                "background-color: #cccccc; height: 1px;\">Sent at Feb 2, 2016 3:04:05 AM<br>" +
                "</body></html>", text);
    }

    /**
     * Check email body footer with different options.
     *
     * @throws Exception when fails
     */
    public void testFooterDeviceNameOption() throws Exception {
        MailMessage message = new MailMessage("+70123456789", "Email body text", 0, null);

        MailerProperties properties = new MailerProperties();
        properties.setContentTime(false);
        properties.setContentDeviceName(true);
        properties.setContentLocation(false);
        properties.setContentContactName(false);

        MailFormatter formatter = new MailFormatter(getContext(), properties, message);

        String text = formatter.getBody();
        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; " +
                "charset=utf-8\"></head><body>Email body text<hr style=\"border: none; " +
                "background-color: #cccccc; height: 1px;\">Sent from "
                + DeviceUtil.getDeviceName() + "<br>" +
                "</body></html>", text);
    }

    /**
     * Check email body footer with different options.
     *
     * @throws Exception when fails
     */
    public void testFooterDeviceNameTimeOption() throws Exception {
        long time = new GregorianCalendar(2016, 1, 2, 3, 4, 5).getTime().getTime();
        MailMessage message = new MailMessage("+70123456789", "Email body text", time, null);

        MailerProperties properties = new MailerProperties();
        properties.setContentTime(true);
        properties.setContentDeviceName(true);
        properties.setContentLocation(false);
        properties.setContentContactName(false);

        MailFormatter formatter = new MailFormatter(getContext(), properties, message);

        String text = formatter.getBody();
        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; " +
                "charset=utf-8\"></head><body>Email body text<hr style=\"border: none; " +
                "background-color: #cccccc; height: 1px;\">Sent from "
                + DeviceUtil.getDeviceName() + "<br>" +
                "at Feb 2, 2016 3:04:05 AM<br></body></html>", text);
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
        MailMessage message = new MailMessage("+70123456789", "Email body text", 0, location);

        MailerProperties properties = new MailerProperties();
        properties.setContentTime(false);
        properties.setContentDeviceName(false);
        properties.setContentLocation(true);
        properties.setContentContactName(false);

        MailFormatter formatter = new MailFormatter(getContext(), properties, message);

        String text = formatter.getBody();
        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; " +
                "charset=utf-8\"></head><body>Email body text<hr style=\"border: none; " +
                "background-color: #cccccc; height: 1px;\">Sent from location " +
                "<a href=\"http://maps.google.com/maps/place/60.555,30.555\">60&#176;33'17\"N, 30&#176;33'17\"W</a>" +
                "<br></body></html>", text);
    }

    /**
     * Check email body footer with different options.
     *
     * @throws Exception when fails
     */
    public void testFooterNoLocation() throws Exception {
        MailMessage message = new MailMessage("+70123456789", "Email body text", 0, null);

        MailerProperties properties = new MailerProperties();
        properties.setContentTime(false);
        properties.setContentDeviceName(false);
        properties.setContentLocation(true);
        properties.setContentContactName(false);

        MailFormatter formatter = new MailFormatter(getContext(), properties, message);

        String text = formatter.getBody();
        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; " +
                "charset=utf-8\"></head><body>Email body text</body></html>", text);
    }

    /**
     * Check email body footer with different options.
     *
     * @throws Exception when fails
     */
    public void testContactName() throws Exception {
        String phone = "+12345678901";

        if (ContactUtil.getContactName(getContext(), phone) == null) {
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
                    .withValue(Phone.NUMBER, phone)
                    .withValue(Phone.TYPE, Phone.TYPE_MOBILE)
                    .build());

            getContext().getContentResolver().applyBatch(AUTHORITY, ops);
        }

        MailMessage message = new MailMessage(phone, "Email body text", 0, null);

        MailerProperties properties = new MailerProperties();
        properties.setContentTime(false);
        properties.setContentDeviceName(false);
        properties.setContentLocation(false);
        properties.setContentContactName(true);

        MailFormatter formatter = new MailFormatter(getContext(), properties, message);

        String text = formatter.getBody();
        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; " +
                "charset=utf-8\"></head><body>Email body text<hr style=\"border: none; " +
                "background-color: #cccccc; height: 1px;\">Sent by John Dou" +
                "<br></body></html>", text);
    }

}