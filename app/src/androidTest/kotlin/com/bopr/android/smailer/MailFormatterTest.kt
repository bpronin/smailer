package com.bopr.android.smailer

import android.Manifest.permission.*
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.content.res.Configuration
import com.bopr.android.smailer.HtmlMatcher.Companion.htmlEqualsRes
import com.bopr.android.smailer.PhoneEvent.Companion.REASON_ACCEPTED
import com.bopr.android.smailer.PhoneEvent.Companion.STATE_PENDING
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_EMAIL_CONTENT_CONTACT
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_EMAIL_CONTENT_DEVICE_NAME
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_EMAIL_CONTENT_LOCATION
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME_SENT
import com.nhaarman.mockitokotlin2.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import java.util.*

/**
 * [MailFormatter] tester.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class MailFormatterTest : BaseTest() {

    private lateinit var context: Context
    private val defaultTime = GregorianCalendar(2016, 1, 2, 3, 4, 5).time
    private val defaultCoordinates = GeoCoordinates(60.555, 30.555)

    @Before
    fun setUp() {
        context = mock {
            on { resources } doReturn (targetContext.resources)
            on { createConfigurationContext(anyOrNull()) } doAnswer { invocation ->
                val parameter = invocation.arguments[0] as Configuration
                targetContext.createConfigurationContext(parameter)
            }
        }
    }

    /**
     * Check formatting incoming sms email subject.
     */
    @Test
    fun testIncomingSmsSubject() {
        val event = PhoneEvent("+70123456789", true, 0, null, false, "Email body text",
                null, null, STATE_PENDING, "device", REASON_ACCEPTED, false)

        val formatter = MailFormatter(context, event)

        assertEquals("[SMailer] Incoming SMS from +70123456789", formatter.formatSubject())
    }

    /**
     * Check formatting outgoing sms email subject.
     */
    @Test
    fun testOutgoingSmsSubject() {
        val event = PhoneEvent("+70123456789", false, 0, null, false, "Email body text",
                null, null, STATE_PENDING, "device", REASON_ACCEPTED, false)

        val formatter = MailFormatter(context, event)

        assertEquals("[SMailer] Outgoing SMS to +70123456789", formatter.formatSubject())
    }

    /**
     * Check formatting incoming call email subject.
     */
    @Test
    fun testIncomingCallSubject() {
        val event = PhoneEvent("+70123456789", true, 0, null, false, null, null, null,
                STATE_PENDING, "device", REASON_ACCEPTED, false)

        val formatter = MailFormatter(context, event)

        assertEquals("[SMailer] Incoming call from +70123456789", formatter.formatSubject())
    }

    /**
     * Check formatting outgoing call email subject.
     */
    @Test
    fun testOutgoingCallSubject() {
        val event = PhoneEvent("+70123456789", false, 0, null, false, null, null, null,
                STATE_PENDING, "device", REASON_ACCEPTED, false)

        val formatter = MailFormatter(context, event)

        assertEquals("[SMailer] Outgoing call to +70123456789", formatter.formatSubject())
    }

    /**
     * Check formatting outgoing call email subject.
     */
    @Test
    fun testMissedCallSubject() {
        val event = PhoneEvent("+70123456789", false, 0, null, true, null, null, null,
                STATE_PENDING, "device", REASON_ACCEPTED, false)

        val formatter = MailFormatter(context, event)

        assertEquals("[SMailer] Missed call from +70123456789", formatter.formatSubject())
    }

    /**
     * Check that email body does not contain any footer when no options have been chosen.
     */
    @Test
    fun testNoBodyFooter() {
        val event = PhoneEvent("+70123456789", true, 0, null, false, "Email body text",
                null, null, STATE_PENDING, "device", REASON_ACCEPTED, false)

        val formatter = MailFormatter(context, event)

        assertThat(formatter.formatBody(), htmlEqualsRes("no_body_footer.html"))
    }

    /**
     * Check email body footer with [Settings.VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME] option switched on.
     */
    @Test
    fun testFooterTimeOption() {
        val event = PhoneEvent("+70123456789", true, defaultTime.time, null, false,
                "Email body text", null, null, STATE_PENDING, "device", REASON_ACCEPTED, false)

        val formatter = MailFormatter(context, event)
        formatter.setSendTime(defaultTime)
        formatter.setOptions(setOf(VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME))

        assertThat(formatter.formatBody(), htmlEqualsRes("footer_time_option.html"))
    }

    /**
     * Check email body footer with [Settings.VAL_PREF_EMAIL_CONTENT_DEVICE_NAME] option switched on.
     */
    @Test
    fun testFooterDeviceNameOption() {
        val event = PhoneEvent("+70123456789", true, 0, null, false, "Email body text",
                null, null, STATE_PENDING, "device", REASON_ACCEPTED, false)

        val formatter = MailFormatter(context, event)
        formatter.setSendTime(defaultTime)
        formatter.setDeviceName("The Device")
        formatter.setOptions(setOf(VAL_PREF_EMAIL_CONTENT_DEVICE_NAME))

        assertThat(formatter.formatBody(), htmlEqualsRes("footer_device_option.html"))
    }

    /**
     * Check email body footer with [Settings.VAL_PREF_EMAIL_CONTENT_DEVICE_NAME] option
     * switched on and no devise name specified.
     */
    @Test
    fun testFooterDeviceNameOptionNoValue() {
        val event = PhoneEvent("+70123456789", true, 0, null, false, "Email body text",
                null, null, STATE_PENDING, "device", REASON_ACCEPTED, false)

        val formatter = MailFormatter(context, event)
        formatter.setSendTime(defaultTime)
        formatter.setOptions(setOf(VAL_PREF_EMAIL_CONTENT_DEVICE_NAME))

        assertThat(formatter.formatBody(), htmlEqualsRes("footer_no_device_option.html"))
    }

    /**
     * Check email body footer with [Settings.VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME] and
     * [Settings.VAL_PREF_EMAIL_CONTENT_DEVICE_NAME] options switched on.
     */
    @Test
    fun testFooterDeviceNameTimeOption() {
        val event = PhoneEvent("+70123456789", true, defaultTime.time, null, false,
                "Email body text", null, null, STATE_PENDING, "device", REASON_ACCEPTED, false)

        val formatter = MailFormatter(context, event)
        formatter.setSendTime(defaultTime)
        formatter.setDeviceName("The Device")
        formatter.setOptions(setOf(VAL_PREF_EMAIL_CONTENT_DEVICE_NAME,
                VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME))

        assertThat(formatter.formatBody(), htmlEqualsRes("footer_time_device_option.html"))
    }

    /**
     * Check email body footer with [Settings.VAL_PREF_EMAIL_CONTENT_LOCATION] option switched on.
     */
    @Test
    fun testFooterLocation() {
        val event = PhoneEvent("+70123456789", true, 0, null, false, "Email body text",
                defaultCoordinates, null, STATE_PENDING, "device", REASON_ACCEPTED, false)

        val formatter = MailFormatter(context, event)
        formatter.setSendTime(defaultTime)
        formatter.setOptions(setOf(VAL_PREF_EMAIL_CONTENT_LOCATION))

        assertThat(formatter.formatBody(), htmlEqualsRes("footer_location_option.html"))
    }

    /**
     * Check email body footer with [Settings.VAL_PREF_EMAIL_CONTENT_DEVICE_NAME] option
     * switched on and no location specified in event.
     */
    @Test
    fun testFooterNoLocation() {
        val event = PhoneEvent("+70123456789", true, 0, null, false, "Email body text",
                null, null, STATE_PENDING, "device", REASON_ACCEPTED, false)

        val formatter = MailFormatter(context, event)
        formatter.setSendTime(defaultTime)
        formatter.setOptions(setOf(VAL_PREF_EMAIL_CONTENT_LOCATION))

        assertThat(formatter.formatBody(), htmlEqualsRes("footer_no_location.html"))
    }

    /**
     * Check email body footer with [Settings.VAL_PREF_EMAIL_CONTENT_DEVICE_NAME] option
     * switched on and no location permissions.
     */
    @Test
    fun testFooterNoLocationPermissions() {
        val event = PhoneEvent("+70123456789", true, 0, null, false, "Email body text",
                null, null, STATE_PENDING, "device", REASON_ACCEPTED, false)

        whenever(context.checkPermission(eq(ACCESS_COARSE_LOCATION), anyInt(), anyInt())).thenReturn(PERMISSION_DENIED)
        whenever(context.checkPermission(eq(ACCESS_FINE_LOCATION), anyInt(), anyInt())).thenReturn(PERMISSION_DENIED)

        val formatter = MailFormatter(context, event)
        formatter.setSendTime(defaultTime)
        formatter.setOptions(setOf(VAL_PREF_EMAIL_CONTENT_LOCATION))

        assertThat(formatter.formatBody(), htmlEqualsRes("footer_no_location_permission.html"))
    }

    /**
     * Check email body footer with [Settings.VAL_PREF_EMAIL_CONTENT_CONTACT] option switched on.
     */
    @Test
    fun testContactName() {
        val event = PhoneEvent("+12345678901", true, 0, null, false, "Email body text",
                null, null, STATE_PENDING, "device", REASON_ACCEPTED, false)

        val formatter = MailFormatter(context, event)
        formatter.setSendTime(defaultTime)
        formatter.setContactName("John Dou")
        formatter.setOptions(setOf(VAL_PREF_EMAIL_CONTENT_CONTACT))

        assertThat(formatter.formatBody(), htmlEqualsRes("footer_contact_option.html"))
    }

    /**
     * Check email body footer with [Settings.VAL_PREF_EMAIL_CONTENT_CONTACT] option switched on
     * and no permission to read contacts.
     */
    @Test
    fun testContactNameNoPermission() {
        val event = PhoneEvent("+12345678901", true, 0, null, false, "Email body text",
                null, null, STATE_PENDING, "device", REASON_ACCEPTED, false)

        whenever(context.checkPermission(eq(READ_CONTACTS), anyInt(), anyInt())).thenReturn(PERMISSION_DENIED)

        val formatter = MailFormatter(context, event)
        formatter.setSendTime(defaultTime)
        formatter.setOptions(setOf(VAL_PREF_EMAIL_CONTENT_CONTACT))

        assertThat(formatter.formatBody(), htmlEqualsRes("footer_contact_no_permission.html"))
    }

    /**
     * Check email body footer with [Settings.VAL_PREF_EMAIL_CONTENT_CONTACT] option switched on
     * and unknown contact name.
     */
    @Test
    fun testUnknownContactName() {
        val event = PhoneEvent("+1234 5678-901", true, 0, null, false, "Email body text",
                null, null, STATE_PENDING, "device", REASON_ACCEPTED, false)

        val formatter = MailFormatter(context, event)
        formatter.setSendTime(defaultTime)
        formatter.setOptions(setOf(VAL_PREF_EMAIL_CONTENT_CONTACT))

        assertThat(formatter.formatBody(), htmlEqualsRes("footer_unknown_contact.html"))
    }

    /**
     * Check incoming call email body.
     */
    @Test
    fun testIncomingCallBody() {
        val start = defaultTime.time
        val end = GregorianCalendar(2016, 1, 2, 4, 5, 10).time.time
        val event = PhoneEvent("+70123456789", true, start, end, false, null, null,
                null, STATE_PENDING, "device", REASON_ACCEPTED, false)

        val formatter = MailFormatter(context, event)

        assertThat(formatter.formatBody(), htmlEqualsRes("incoming_call.html"))
    }

    /**
     * Check outgoing call email body.
     */
    @Test
    fun testOutgoingCallBody() {
        val start = defaultTime.time
        val end = GregorianCalendar(2016, 1, 2, 4, 5, 15).time.time
        val event = PhoneEvent("+70123456789", false, start, end, false, null, null,
                null, STATE_PENDING, "device", REASON_ACCEPTED, false)

        val formatter = MailFormatter(context, event)

        assertThat(formatter.formatBody(), htmlEqualsRes("outgoing_call.html"))
    }

    /**
     * Check missed call email body.
     */
    @Test
    fun testMissedCallBody() {
        val start = defaultTime.time
        val event = PhoneEvent("+70123456789", false, start, null, true, null, null,
                null, STATE_PENDING, "device", REASON_ACCEPTED, false)

        val formatter = MailFormatter(context, event)

        assertThat(formatter.formatBody(), htmlEqualsRes("missed_call.html"))
    }

    /**
     * Check incoming call email body when all required information is present.
     */
    @Test
    fun testAllIncomingContentCall() {
        val start = defaultTime.time
        val end = GregorianCalendar(2016, 1, 2, 4, 5, 10).time.time
        val event = PhoneEvent("+12345678901", true, start, end, false, null,
                defaultCoordinates, null, STATE_PENDING, "device", REASON_ACCEPTED, false)

        val formatter = MailFormatter(context, event)
        formatter.setSendTime(defaultTime)
        formatter.setContactName("John Dou")
        formatter.setDeviceName("Device")
        formatter.setOptions(setOf(VAL_PREF_EMAIL_CONTENT_CONTACT, VAL_PREF_EMAIL_CONTENT_LOCATION,
                VAL_PREF_EMAIL_CONTENT_DEVICE_NAME, VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME,
                VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME_SENT))

        assertThat(formatter.formatBody(), htmlEqualsRes("incoming_call_all.html"))
    }

    /**
     * Check outgoing call email body when all required information is present.
     */
    @Test
    fun testAllOutgoingContentCall() {
        val start = defaultTime.time
        val end = GregorianCalendar(2016, 1, 2, 4, 5, 10).time.time
        val event = PhoneEvent("+12345678901", false, start, end, false, null,
                defaultCoordinates, null, STATE_PENDING, "device", REASON_ACCEPTED, false)

        val formatter = MailFormatter(context, event)
        formatter.setSendTime(defaultTime)
        formatter.setContactName("John Dou")
        formatter.setDeviceName("Device")
        formatter.setOptions(setOf(VAL_PREF_EMAIL_CONTENT_CONTACT, VAL_PREF_EMAIL_CONTENT_LOCATION,
                VAL_PREF_EMAIL_CONTENT_DEVICE_NAME, VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME,
                VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME_SENT))

        assertThat(formatter.formatBody(), htmlEqualsRes("outgoing_call_all.html"))
    }

    /**
     * Check missed call email body when all required information is present.
     */
    @Test
    fun testAllContentMissedCall() {
        val start = defaultTime.time
        val end = GregorianCalendar(2016, 1, 2, 4, 5, 10).time.time
        val event = PhoneEvent("+12345678901", true, start, end, true, null,
                defaultCoordinates, null, STATE_PENDING, "device", REASON_ACCEPTED, false)

        val formatter = MailFormatter(context, event)
        formatter.setSendTime(defaultTime)
        formatter.setDeviceName("Device")
        formatter.setOptions(setOf(VAL_PREF_EMAIL_CONTENT_CONTACT, VAL_PREF_EMAIL_CONTENT_LOCATION,
                VAL_PREF_EMAIL_CONTENT_DEVICE_NAME, VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME,
                VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME_SENT))

        assertThat(formatter.formatBody(), htmlEqualsRes("missed_call_all.html"))
    }

    /**
     * Check email body with valid non-default locale specified.
     */
    @Test
    fun testNonDefaultLocale() {
        val timeZone = TimeZone.getTimeZone("EST")
        val calendar = GregorianCalendar(timeZone)
        calendar[2016, 1, 2, 3, 4] = 5
        val start = calendar.time.time
        calendar[2016, 1, 2, 3, 4] = 10
        val end = calendar.time.time
        val event = PhoneEvent("+12345678901", true, start, end, true, null,
                defaultCoordinates, null, STATE_PENDING, "device", REASON_ACCEPTED, false)

        val formatter = MailFormatter(context, event)
        formatter.setSendTime(defaultTime)
        formatter.setDeviceName("Device")
        formatter.setLocale(Locale("ru", "ru"))
        formatter.setOptions(setOf(VAL_PREF_EMAIL_CONTENT_CONTACT,
                VAL_PREF_EMAIL_CONTENT_LOCATION,
                VAL_PREF_EMAIL_CONTENT_DEVICE_NAME,
                VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME,
                VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME_SENT))

        assertEquals("[SMailer] Пропущенный звонок от +12345678901", formatter.formatSubject())
        assertThat(formatter.formatBody(), htmlEqualsRes("missed_call_ru.html"))

        formatter.setLocale(Locale.getDefault())

        assertEquals("[SMailer] Missed call from +12345678901", formatter.formatSubject())
        assertThat(formatter.formatBody(), htmlEqualsRes("missed_call_en.html"))
    }

    /**
     * Check URLs formatting.
     */
    @Test
    fun testFormatUrls() {
        val event = PhoneEvent("+12345678901", true, 0, null, false,
                "Please visit https://www.google.com/search?a=1&b=2 or " +
                        "https://secure.place  site or download from ftp://get.from.here",
                null, null, STATE_PENDING, "device", REASON_ACCEPTED, false)

        val formatter = MailFormatter(context, event)

        assertThat(formatter.formatBody(), htmlEqualsRes("urls.html"))
    }

    /**
     * Check remote control links formatting.
     */
    @Test
    fun testRemoteControlLinks() {
        val start = defaultTime.time
        val end = GregorianCalendar(2016, 1, 2, 4, 5, 10).time.time
        val event = PhoneEvent("+12345678901", true, start, end,
                false, "Message", defaultCoordinates, null, STATE_PENDING, "device", REASON_ACCEPTED, false)

        val formatter = MailFormatter(context, event)
        formatter.setDeviceName("Device")
        formatter.setServiceAccount("service@mail.com")
        formatter.setOptions(setOf(Settings.VAL_PREF_EMAIL_CONTENT_REMOTE_COMMAND_LINKS))

        assertThat(formatter.formatBody(), htmlEqualsRes("remote_control_links.html"))
    }
}