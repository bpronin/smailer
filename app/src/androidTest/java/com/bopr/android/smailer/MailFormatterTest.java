package com.bopr.android.smailer;

import android.content.Context;
import android.content.res.Configuration;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_CONTACTS;
import static android.content.pm.PackageManager.PERMISSION_DENIED;
import static com.bopr.android.smailer.HtmlMatcher.htmlEqualsRes;
import static com.bopr.android.smailer.PhoneEvent.STATE_PENDING;
import static com.bopr.android.smailer.Settings.VAL_PREF_EMAIL_CONTENT_CONTACT;
import static com.bopr.android.smailer.Settings.VAL_PREF_EMAIL_CONTENT_DEVICE_NAME;
import static com.bopr.android.smailer.Settings.VAL_PREF_EMAIL_CONTENT_LOCATION;
import static com.bopr.android.smailer.Settings.VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME;
import static com.bopr.android.smailer.Settings.VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME_SENT;
import static com.bopr.android.smailer.Settings.VAL_PREF_EMAIL_CONTENT_REMOTE_COMMAND_LINKS;
import static com.bopr.android.smailer.util.Util.setOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
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
    }

    /**
     * Check formatting incoming sms email subject.
     */
    @Test
    public void testIncomingSmsSubject() {
        PhoneEvent event = new PhoneEvent("+70123456789", true, 0, null, false, "Email body text",
                null, null, STATE_PENDING, null);

        MailFormatter formatter = new MailFormatter(context, event);

        assertEquals("[SMailer] Incoming SMS from +70123456789", formatter.formatSubject());
    }

    /**
     * Check formatting outgoing sms email subject.
     */
    @Test
    public void testOutgoingSmsSubject() {
        PhoneEvent event = new PhoneEvent("+70123456789", false, 0, null, false, "Email body text",
                null, null, STATE_PENDING, null);

        MailFormatter formatter = new MailFormatter(context, event);

        assertEquals("[SMailer] Outgoing SMS to +70123456789", formatter.formatSubject());
    }

    /**
     * Check formatting incoming call email subject.
     */
    @Test
    public void testIncomingCallSubject() {
        PhoneEvent event = new PhoneEvent("+70123456789", true, 0, null, false, null, null, null,
                STATE_PENDING, null);

        MailFormatter formatter = new MailFormatter(context, event);

        assertEquals("[SMailer] Incoming call from +70123456789", formatter.formatSubject());
    }

    /**
     * Check formatting outgoing call email subject.
     */
    @Test
    public void testOutgoingCallSubject() {
        PhoneEvent event = new PhoneEvent("+70123456789", false, 0, null, false, null, null, null,
                STATE_PENDING, null);

        MailFormatter formatter = new MailFormatter(context, event);

        assertEquals("[SMailer] Outgoing call to +70123456789", formatter.formatSubject());
    }

    /**
     * Check formatting outgoing call email subject.
     */
    @Test
    public void testMissedCallSubject() {
        PhoneEvent event = new PhoneEvent("+70123456789", false, 0, null, true, null, null, null,
                STATE_PENDING, null);

        MailFormatter formatter = new MailFormatter(context, event);

        assertEquals("[SMailer] Missed call from +70123456789", formatter.formatSubject());
    }

    /**
     * Check that email body does not contain any footer when no options have been chosen.
     */
    @Test
    public void testNoBodyFooter() {
        PhoneEvent event = new PhoneEvent("+70123456789", true, 0, null, false, "Email body text",
                null, null, STATE_PENDING, null);

        MailFormatter formatter = new MailFormatter(context, event);

        assertThat(formatter.formatBody(), htmlEqualsRes("no_body_footer.html"));
    }

    /**
     * Check email body footer with {@link Settings#VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME} option switched on.
     */
    @Test
    public void testFooterTimeOption() {
        PhoneEvent event = new PhoneEvent("+70123456789", true, defaultTime.getTime(), null, false,
                "Email body text", null, null, STATE_PENDING, null);

        MailFormatter formatter = new MailFormatter(context, event);
        formatter.setSendTime(defaultTime);
        formatter.setContentOptions(setOf(VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME));

        assertThat(formatter.formatBody(), htmlEqualsRes("footer_time_option.html"));
    }

    /**
     * Check email body footer with {@link Settings#VAL_PREF_EMAIL_CONTENT_DEVICE_NAME} option switched on.
     */
    @Test
    public void testFooterDeviceNameOption() {
        PhoneEvent event = new PhoneEvent("+70123456789", true, 0, null, false, "Email body text",
                null, null, STATE_PENDING, null);

        MailFormatter formatter = new MailFormatter(context, event);
        formatter.setSendTime(defaultTime);
        formatter.setDeviceName("The Device");
        formatter.setContentOptions(setOf(VAL_PREF_EMAIL_CONTENT_DEVICE_NAME));

        assertThat(formatter.formatBody(), htmlEqualsRes("footer_device_option.html"));
    }

    /**
     * Check email body footer with {@link Settings#VAL_PREF_EMAIL_CONTENT_DEVICE_NAME} option
     * switched on and no devise name specified.
     */
    @Test
    public void testFooterDeviceNameOptionNoValue() {
        PhoneEvent event = new PhoneEvent("+70123456789", true, 0, null, false, "Email body text",
                null, null, STATE_PENDING, null);

        MailFormatter formatter = new MailFormatter(context, event);
        formatter.setSendTime(defaultTime);
        formatter.setContentOptions(setOf(VAL_PREF_EMAIL_CONTENT_DEVICE_NAME));

        assertThat(formatter.formatBody(), htmlEqualsRes("footer_no_device_option.html"));
    }

    /**
     * Check email body footer with {@link Settings#VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME} and
     * {@link Settings#VAL_PREF_EMAIL_CONTENT_DEVICE_NAME} options switched on.
     */
    @Test
    public void testFooterDeviceNameTimeOption() {
        PhoneEvent event = new PhoneEvent("+70123456789", true, defaultTime.getTime(), null, false,
                "Email body text", null, null, STATE_PENDING, null);

        MailFormatter formatter = new MailFormatter(context, event);
        formatter.setSendTime(defaultTime);
        formatter.setDeviceName("The Device");
        formatter.setContentOptions(setOf(VAL_PREF_EMAIL_CONTENT_DEVICE_NAME,
                VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME));

        assertThat(formatter.formatBody(), htmlEqualsRes("footer_time_device_option.html"));
    }

    /**
     * Check email body footer with {@link Settings#VAL_PREF_EMAIL_CONTENT_LOCATION} option switched on.
     */
    @Test
    public void testFooterLocation() {
        PhoneEvent event = new PhoneEvent("+70123456789", true, 0, null, false, "Email body text",
                new GeoCoordinates(60.555, 30.555), null, STATE_PENDING, null);

        MailFormatter formatter = new MailFormatter(context, event);
        formatter.setSendTime(defaultTime);
        formatter.setContentOptions(setOf(VAL_PREF_EMAIL_CONTENT_LOCATION));

        assertThat(formatter.formatBody(), htmlEqualsRes("footer_location_option.html"));
    }

    /**
     * Check email body footer with {@link Settings#VAL_PREF_EMAIL_CONTENT_DEVICE_NAME} option
     * switched on and no location specified in event.
     */
    @Test
    public void testFooterNoLocation() {
        PhoneEvent event = new PhoneEvent("+70123456789", true, 0, null, false, "Email body text",
                null, null, STATE_PENDING, null);

        MailFormatter formatter = new MailFormatter(context, event);
        formatter.setSendTime(defaultTime);
        formatter.setContentOptions(setOf(VAL_PREF_EMAIL_CONTENT_LOCATION));

        assertThat(formatter.formatBody(), htmlEqualsRes("footer_no_location.html"));
    }

    /**
     * Check email body footer with {@link Settings#VAL_PREF_EMAIL_CONTENT_DEVICE_NAME} option
     * switched on and no location permissions.
     */
    @Test
    public void testFooterNoLocationPermissions() {
        PhoneEvent event = new PhoneEvent("+70123456789", true, 0, null, false, "Email body text",
                null, null, STATE_PENDING, null);

        MailFormatter formatter = new MailFormatter(context, event);
        formatter.setSendTime(defaultTime);
        formatter.setContentOptions(setOf(VAL_PREF_EMAIL_CONTENT_LOCATION));

        when(context.checkPermission(eq(ACCESS_COARSE_LOCATION), anyInt(), anyInt())).thenReturn(PERMISSION_DENIED);
        when(context.checkPermission(eq(ACCESS_FINE_LOCATION), anyInt(), anyInt())).thenReturn(PERMISSION_DENIED);

        assertThat(formatter.formatBody(), htmlEqualsRes("footer_no_location_permission.html"));
    }

    /**
     * Check email body footer with {@link Settings#VAL_PREF_EMAIL_CONTENT_CONTACT} option switched on.
     */
    @Test
    public void testContactName() {
        PhoneEvent event = new PhoneEvent("+12345678901", true, 0, null, false, "Email body text",
                null, null, STATE_PENDING, null);

        MailFormatter formatter = new MailFormatter(context, event);
        formatter.setSendTime(defaultTime);
        formatter.setContactName("John Dou");
        formatter.setContentOptions(setOf(VAL_PREF_EMAIL_CONTENT_CONTACT));

        assertThat(formatter.formatBody(), htmlEqualsRes("footer_contact_option.html"));
    }

    /**
     * Check email body footer with {@link Settings#VAL_PREF_EMAIL_CONTENT_CONTACT} option switched on
     * and no permission to read contacts.
     */
    @Test
    public void testContactNameNoPermission() {
        PhoneEvent event = new PhoneEvent("+12345678901", true, 0, null, false, "Email body text",
                null, null, STATE_PENDING, null);

        MailFormatter formatter = new MailFormatter(context, event);
        formatter.setSendTime(defaultTime);
        formatter.setContentOptions(setOf(VAL_PREF_EMAIL_CONTENT_CONTACT));
        when(context.checkPermission(eq(READ_CONTACTS), anyInt(), anyInt())).thenReturn(PERMISSION_DENIED);

        assertThat(formatter.formatBody(), htmlEqualsRes("footer_contact_no_permission.html"));
    }

    /**
     * Check email body footer with {@link Settings#VAL_PREF_EMAIL_CONTENT_CONTACT} option switched on
     * and unknown contact name.
     */
    @Test
    public void testUnknownContactName() {
        PhoneEvent event = new PhoneEvent("+1234 5678-901", true, 0, null, false, "Email body text",
                null, null, STATE_PENDING, null);

        MailFormatter formatter = new MailFormatter(context, event);
        formatter.setSendTime(defaultTime);
        formatter.setContentOptions(setOf(VAL_PREF_EMAIL_CONTENT_CONTACT));

        assertThat(formatter.formatBody(), htmlEqualsRes("footer_unknown_contact.html"));
    }

    /**
     * Check incoming call email body.
     */
    @Test
    public void testIncomingCallBody() {
        long start = defaultTime.getTime();
        long end = new GregorianCalendar(2016, 1, 2, 4, 5, 10).getTime().getTime();
        PhoneEvent event = new PhoneEvent("+70123456789", true, start, end, false, null, null,
                null, STATE_PENDING, null);

        MailFormatter formatter = new MailFormatter(context, event);

        assertThat(formatter.formatBody(), htmlEqualsRes("incoming_call.html"));
    }

    /**
     * Check outgoing call email body.
     */
    @Test
    public void testOutgoingCallBody() {
        long start = defaultTime.getTime();
        long end = new GregorianCalendar(2016, 1, 2, 4, 5, 15).getTime().getTime();
        PhoneEvent event = new PhoneEvent("+70123456789", false, start, end, false, null, null,
                null, STATE_PENDING, null);

        MailFormatter formatter = new MailFormatter(context, event);

        assertThat(formatter.formatBody(), htmlEqualsRes("outgoing_call.html"));
    }

    /**
     * Check missed call email body.
     */
    @Test
    public void testMissedCallBody() {
        long start = defaultTime.getTime();
        PhoneEvent event = new PhoneEvent("+70123456789", false, start, null, true, null, null,
                null, STATE_PENDING, null);

        MailFormatter formatter = new MailFormatter(context, event);

        assertThat(formatter.formatBody(), htmlEqualsRes("missed_call.html"));
    }

    /**
     * Check incoming call email body when all required information is present.
     */
    @Test
    public void testAllIncomingContentCall() {
        long start = defaultTime.getTime();
        long end = new GregorianCalendar(2016, 1, 2, 4, 5, 10).getTime().getTime();

        PhoneEvent event = new PhoneEvent("+12345678901", true, start, end, false, null,
                new GeoCoordinates(60.555, 30.555), null, STATE_PENDING, null);

        MailFormatter formatter = new MailFormatter(context, event);
        formatter.setSendTime(defaultTime);
        formatter.setContactName("John Dou");
        formatter.setDeviceName("Device");
        formatter.setContentOptions(setOf(VAL_PREF_EMAIL_CONTENT_CONTACT, VAL_PREF_EMAIL_CONTENT_LOCATION,
                VAL_PREF_EMAIL_CONTENT_DEVICE_NAME, VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME,
                VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME_SENT));

        assertThat(formatter.formatBody(), htmlEqualsRes("incoming_call_all.html"));
    }

    /**
     * Check outgoing call email body when all required information is present.
     */
    @Test
    public void testAllOutgoingContentCall() {
        long start = defaultTime.getTime();
        long end = new GregorianCalendar(2016, 1, 2, 4, 5, 10).getTime().getTime();

        PhoneEvent event = new PhoneEvent("+12345678901", false, start, end, false, null,
                new GeoCoordinates(60.555, 30.555), null, STATE_PENDING, null);

        MailFormatter formatter = new MailFormatter(context, event);
        formatter.setSendTime(defaultTime);
        formatter.setContactName("John Dou");
        formatter.setDeviceName("Device");
        formatter.setContentOptions(setOf(VAL_PREF_EMAIL_CONTENT_CONTACT, VAL_PREF_EMAIL_CONTENT_LOCATION,
                VAL_PREF_EMAIL_CONTENT_DEVICE_NAME, VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME,
                VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME_SENT));

        assertThat(formatter.formatBody(), htmlEqualsRes("outgoing_call_all.html"));
    }

    /**
     * Check missed call email body when all required information is present.
     */
    @Test
    public void testAllContentMissedCall() {
        long start = defaultTime.getTime();
        long end = new GregorianCalendar(2016, 1, 2, 4, 5, 10).getTime().getTime();

        PhoneEvent event = new PhoneEvent("+12345678901", true, start, end, true, null,
                new GeoCoordinates(60.555, 30.555), null, STATE_PENDING, null);

        MailFormatter formatter = new MailFormatter(context, event);
        formatter.setSendTime(defaultTime);
        formatter.setDeviceName("Device");
        formatter.setContentOptions(setOf(VAL_PREF_EMAIL_CONTENT_CONTACT, VAL_PREF_EMAIL_CONTENT_LOCATION,
                VAL_PREF_EMAIL_CONTENT_DEVICE_NAME, VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME,
                VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME_SENT));

        assertThat(formatter.formatBody(), htmlEqualsRes("missed_call_all.html"));
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

        PhoneEvent event = new PhoneEvent("+12345678901", true, start, end, true, null,
                new GeoCoordinates(60.555, 30.555), null, STATE_PENDING, null);

        MailFormatter formatter = new MailFormatter(context, event);
        formatter.setSendTime(defaultTime);
        formatter.setDeviceName("Device");
        formatter.setLocale(new Locale("ru", "ru"));
        formatter.setContentOptions(setOf(VAL_PREF_EMAIL_CONTENT_CONTACT,
                VAL_PREF_EMAIL_CONTENT_LOCATION,
                VAL_PREF_EMAIL_CONTENT_DEVICE_NAME,
                VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME,
                VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME_SENT));

        assertEquals("[SMailer] Пропущенный звонок от +12345678901", formatter.formatSubject());
        assertThat(formatter.formatBody(), htmlEqualsRes("missed_call_ru.html"));

        formatter.setLocale(Locale.getDefault());

        assertEquals("[SMailer] Missed call from +12345678901", formatter.formatSubject());
        assertThat(formatter.formatBody(), htmlEqualsRes("missed_call_en.html"));
    }

    /**
     * Check URLs formatting.
     */
    @Test
    public void testFormatUrls() {
        PhoneEvent event = new PhoneEvent("+12345678901", true, 0, null, false,
                "Please visit http://google.com site", null, null, STATE_PENDING, null);

        MailFormatter formatter = new MailFormatter(context, event);

        assertThat(formatter.formatBody(), htmlEqualsRes("urls.html"));
    }

    /**
     * Check remote control links formatting.
     */
    @Test
    public void testRemoteControlLinks() {
        long start = defaultTime.getTime();
        long end = new GregorianCalendar(2016, 1, 2, 4, 5, 10).getTime().getTime();

        PhoneEvent event = new PhoneEvent("+12345678901", true, start, end,
                false, "Message", new GeoCoordinates(60.555, 30.555), null, STATE_PENDING, null);

        MailFormatter formatter = new MailFormatter(context, event);
        formatter.setDeviceName("Device");
        formatter.setServiceAccount("service@mail.com");
        formatter.setContentOptions(setOf(VAL_PREF_EMAIL_CONTENT_REMOTE_COMMAND_LINKS));

        assertThat(formatter.formatBody(), htmlEqualsRes("remote_control_links.html"));
    }

}