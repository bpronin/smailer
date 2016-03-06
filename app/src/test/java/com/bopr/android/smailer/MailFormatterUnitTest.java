package com.bopr.android.smailer;

import android.Manifest;
import android.app.Application;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.location.Location;
import android.provider.ContactsContract;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.internal.Shadow;
import org.robolectric.shadows.ShadowApplication;

import java.util.ArrayList;

import static android.provider.ContactsContract.CommonDataKinds.Phone;
import static android.provider.ContactsContract.CommonDataKinds.StructuredName;
import static android.provider.ContactsContract.Data;
import static android.provider.ContactsContract.RawContacts;
import static org.junit.Assert.assertEquals;
import static org.robolectric.RuntimeEnvironment.application;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class MailFormatterUnitTest {

    /**
     * Check formatting incoming sms email subject.
     *
     * @throws Exception when fails
     */
    @Test
    public void testIncomingSmsSubject() throws Exception {
        MailMessage message = new MailMessage("+70123456789", "Email body text", 0, null);
        MailerProperties properties = new MailerProperties();

        HtmlMailFormatter formatter = new HtmlMailFormatter(application, properties, message);

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

        HtmlMailFormatter formatter = new HtmlMailFormatter(application, properties, message);

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
        MailMessage message = new MailMessage("+70123456789", "Email body text", 0, null);

        MailerProperties properties = new MailerProperties();
        properties.setContentTime(true);
        properties.setContentDeviceName(false);
        properties.setContentLocation(false);
        properties.setContentContactName(false);

        HtmlMailFormatter formatter = new HtmlMailFormatter(application, properties, message);

        String text = formatter.getBody();
        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; " +
                "charset=utf-8\"></head><body>Email body text<hr style=\"border: none; " +
                "background-color: #cccccc; height: 1px;\">Sent at 1970-01-01 03:00<br>" +
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

        HtmlMailFormatter formatter = new HtmlMailFormatter(application, properties, message);

        String text = formatter.getBody();
        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; " +
                "charset=utf-8\"></head><body>Email body text<hr style=\"border: none; " +
                "background-color: #cccccc; height: 1px;\">Sent from Unknown unknown<br>" +
                "</body></html>", text);
    }

    /**
     * Check email body footer with different options.
     *
     * @throws Exception when fails
     */
    @Test
    public void testFooterDeviceNameTimeOption() throws Exception {
        MailMessage message = new MailMessage("+70123456789", "Email body text", 0, null);

        MailerProperties properties = new MailerProperties();
        properties.setContentTime(true);
        properties.setContentDeviceName(true);
        properties.setContentLocation(false);
        properties.setContentContactName(false);

        HtmlMailFormatter formatter = new HtmlMailFormatter(application, properties, message);

        String text = formatter.getBody();
        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; " +
                "charset=utf-8\"></head><body>Email body text<hr style=\"border: none; " +
                "background-color: #cccccc; height: 1px;\">Sent from Unknown unknown<br>" +
                "at 1970-01-01 03:00<br></body></html>", text);
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

        HtmlMailFormatter formatter = new HtmlMailFormatter(application, properties, message);

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

        HtmlMailFormatter formatter = new HtmlMailFormatter(application, properties, message);

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
        ShadowApplication app = ShadowApplication.getInstance();
        app.grantPermissions(Manifest.permission.WRITE_CONTACTS);

        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        ops.add(ContentProviderOperation.newInsert(
                RawContacts.CONTENT_URI)
                .withValue(RawContacts.ACCOUNT_TYPE, null)
                .withValue(RawContacts.ACCOUNT_NAME, null)
                .build());

        ops.add(ContentProviderOperation.newInsert(
                Data.CONTENT_URI)
                .withValueBackReference(Data.RAW_CONTACT_ID, 0)
                .withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
                .withValue(StructuredName.DISPLAY_NAME, "The User").build());

        ops.add(ContentProviderOperation.
                newInsert(Data.CONTENT_URI)
                .withValueBackReference(Data.RAW_CONTACT_ID, 0)
                .withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
                .withValue(Phone.NUMBER, "12345")
                .withValue(Phone.TYPE, Phone.TYPE_MOBILE)
                .build());

        ContentProviderResult[] results = app.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);

        MailMessage message = new MailMessage("12345", "Email body text", 0, null);

        MailerProperties properties = new MailerProperties();
        properties.setContentTime(false);
        properties.setContentDeviceName(false);
        properties.setContentLocation(false);
        properties.setContentContactName(true);

        HtmlMailFormatter formatter = new HtmlMailFormatter(app.getApplicationContext(), properties, message);

        String text = formatter.getBody();
        assertEquals("<html><head><meta http-equiv=\"content-type\" content=\"text/html; " +
                "charset=utf-8\"></head><body>Email body text<hr style=\"border: none; " +
                "background-color: #cccccc; height: 1px;\">Sent from location " +
                "<a href=\"http://maps.google.com/maps/place/60.555,30.555\">60&#176;33'17\"N, 30&#176;33'17\"W</a>" +
                "<br></body></html>", text);
    }

}