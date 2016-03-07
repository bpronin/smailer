package com.bopr.android.smailer;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.location.Location;

import com.bopr.android.smailer.util.ContactUtil;
import com.bopr.android.smailer.util.DeviceUtil;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import static android.provider.ContactsContract.AUTHORITY;
import static android.provider.ContactsContract.CommonDataKinds.Phone;
import static android.provider.ContactsContract.CommonDataKinds.StructuredName;
import static android.provider.ContactsContract.Data;
import static android.provider.ContactsContract.RawContacts;
import static org.junit.Assert.assertEquals;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class MailFormatterUnitTest {

    public Context getContext() {
        return RuntimeEnvironment.application;
    }

    @BeforeClass
    public static void startUpTest() {
        Locale.setDefault(Locale.US);
    }

    /**
     * Check formatting incoming sms email subject.
     *
     * @throws Exception when fails
     */
    @Test
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
    @Test
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
    @Test
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
    @Test
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
    @Test
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
    @Test
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
    @Test
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
//    @Test
    public void testContactName() throws Exception {
//        ShadowApplication app = ShadowApplication.getInstance();
//        app.grantPermissions(Manifest.permission.WRITE_CONTACTS);
//        app.grantPermissions(Manifest.permission.READ_CONTACTS);

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
                .withValue(Phone.NUMBER, "12345")
                .withValue(Phone.TYPE, Phone.TYPE_MOBILE)
                .build());

//        getContext().getContentResolver().applyBatch(AUTHORITY, ops);
//        List<String> contactNames = ContactUtil.getContactNames(getContext());
//        System.out.println(contactNames);
//        String contactName = ContactUtil.getContactName(app.getApplicationContext(), "12345");
//        System.out.println(contactName);

        MailMessage message = new MailMessage("+12345678901", "Email body text", 0, null);

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

//    public static List<String> getContactNames(Context context) {
//        Cursor cursor = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
//                null, null, null, null);
//        List<String> result = new ArrayList<>();
//        if (cursor != null) {
//            if (cursor.moveToFirst()) {
//                result.add(cursor.getString(cursor.getColumnIndex(PhoneLookup.DISPLAY_NAME)));
//            }
//            if (!cursor.isClosed()) {
//                cursor.close();
//            }
//        }
//        return result;
//    }

}