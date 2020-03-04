package com.bopr.android.smailer

import android.Manifest.permission.*
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.content.res.Configuration
import androidx.test.filters.SmallTest
import com.bopr.android.smailer.HtmlMatcher.Companion.htmlEquals
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_EMAIL_CONTENT_CONTACT
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_EMAIL_CONTENT_DEVICE_NAME
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_EMAIL_CONTENT_HEADER
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_EMAIL_CONTENT_LOCATION
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME_SENT
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_EMAIL_CONTENT_REMOTE_COMMAND_LINKS
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
@SmallTest
class MailFormatterTest : BaseTest() {

    private lateinit var context: Context
    private val defaultTime = GregorianCalendar(2016, 1, 2, 3, 4, 5).time.time
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
     * Check incoming SMS email body when all required information is present.
     */
    @Test
    fun testAllContentIncomingSms() {
        val event = PhoneEvent(
                phone = "+12345678901",
                isIncoming = true,
                startTime = defaultTime,
                text = "Message",
                location = defaultCoordinates,
                acceptor = "device",
                processTime = defaultTime
        )

        val formatter = MailFormatter(context, event,
                contactName = "John Dou",
                deviceName = "Device",
                serviceAccount = "service@mail.com",
                options = setOf(VAL_PREF_EMAIL_CONTENT_HEADER,
                        VAL_PREF_EMAIL_CONTENT_CONTACT,
                        VAL_PREF_EMAIL_CONTENT_LOCATION,
                        VAL_PREF_EMAIL_CONTENT_DEVICE_NAME,
                        VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME,
                        VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME_SENT,
                        VAL_PREF_EMAIL_CONTENT_REMOTE_COMMAND_LINKS)
        )

        assertThat(formatter.formatBody(), htmlEquals("incoming_sms_all.html"))
    }

    /**
     * Check formatting incoming sms email subject.
     */
    @Test
    fun testIncomingSmsSubject() {
        val event = PhoneEvent(
                phone = "+70123456789",
                isIncoming = true,
                startTime = 0,
                text = "Email body text",
                acceptor = "device"
        )

        val formatter = MailFormatter(context, event)

        assertEquals("[SMailer] Incoming SMS from +70123456789", formatter.formatSubject())
    }

    /**
     * Check formatting outgoing sms email subject.
     */
    @Test
    fun testOutgoingSmsSubject() {
        val event = PhoneEvent(
                phone = "+70123456789",
                isIncoming = false,
                startTime = 0,
                text = "Email body text",
                acceptor = "device"
        )

        val formatter = MailFormatter(context, event)

        assertEquals("[SMailer] Outgoing SMS to +70123456789", formatter.formatSubject())
    }

    /**
     * Check formatting incoming call email subject.
     */
    @Test
    fun testIncomingCallSubject() {
        val event = PhoneEvent(
                phone = "+70123456789",
                isIncoming = true,
                startTime = 0,
                acceptor = "device"
        )

        val formatter = MailFormatter(context, event)

        assertEquals("[SMailer] Incoming call from +70123456789", formatter.formatSubject())
    }

    /**
     * Check formatting outgoing call email subject.
     */
    @Test
    fun testOutgoingCallSubject() {
        val event = PhoneEvent(
                phone = "+70123456789",
                isIncoming = false,
                startTime = 0,
                acceptor = "device"
        )

        val formatter = MailFormatter(context, event)

        assertEquals("[SMailer] Outgoing call to +70123456789", formatter.formatSubject())
    }

    /**
     * Check formatting outgoing call email subject.
     */
    @Test
    fun testMissedCallSubject() {
        val event = PhoneEvent(
                phone = "+70123456789",
                startTime = 0,
                isMissed = true,
                acceptor = "device"
        )

        val formatter = MailFormatter(context, event)

        assertEquals("[SMailer] Missed call from +70123456789", formatter.formatSubject())
    }

    /**
     * Check that email body does not contain any footer when no options have been chosen.
     */
    @Test
    fun testNoBodyFooter() {
        val event = PhoneEvent(
                phone = "+70123456789",
                isIncoming = true,
                startTime = 0,
                text = "Email body text",
                acceptor = "device"
        )

        val formatter = MailFormatter(context, event)

        assertThat(formatter.formatBody(), htmlEquals("no_body_footer.html"))
    }

    /**
     * Check email body footer with [Settings.VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME] option switched on.
     */
    @Test
    fun testFooterTimeOption() {
        val event = PhoneEvent(
                phone = "+70123456789",
                isIncoming = true,
                startTime = defaultTime,
                text = "Email body text",
                acceptor = "device",
                processTime = defaultTime
        )

        val formatter = MailFormatter(context, event,
                options = setOf(VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME)
        )

        assertThat(formatter.formatBody(), htmlEquals("footer_time_option.html"))
    }

    /**
     * Check email body footer with [Settings.VAL_PREF_EMAIL_CONTENT_DEVICE_NAME] option switched on.
     */
    @Test
    fun testFooterDeviceNameOption() {
        val event = PhoneEvent(
                phone = "+70123456789",
                isIncoming = true,
                startTime = 0,
                text = "Email body text",
                acceptor = "device",
                processTime = defaultTime
        )

        val formatter = MailFormatter(context, event,
                deviceName = "The Device",
                options = setOf(VAL_PREF_EMAIL_CONTENT_DEVICE_NAME)
        )

        assertThat(formatter.formatBody(), htmlEquals("footer_device_option.html"))
    }

    /**
     * Check email body footer with [Settings.VAL_PREF_EMAIL_CONTENT_DEVICE_NAME] option
     * switched on and no devise name specified.
     */
    @Test
    fun testFooterDeviceNameOptionNoValue() {
        val event = PhoneEvent(
                phone = "+70123456789",
                isIncoming = true,
                startTime = 0,
                text = "Email body text",
                acceptor = "device",
                processTime = defaultTime
        )

        val formatter = MailFormatter(context, event,
                options = setOf(VAL_PREF_EMAIL_CONTENT_DEVICE_NAME)
        )

        assertThat(formatter.formatBody(), htmlEquals("footer_no_device_option.html"))
    }

    /**
     * Check email body footer with [Settings.VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME] and
     * [Settings.VAL_PREF_EMAIL_CONTENT_DEVICE_NAME] options switched on.
     */
    @Test
    fun testFooterDeviceNameTimeOption() {
        val event = PhoneEvent(
                phone = "+70123456789",
                isIncoming = true,
                startTime = defaultTime,
                text = "Email body text",
                acceptor = "device",
                processTime = defaultTime
        )

        val formatter = MailFormatter(context, event,
                deviceName = "The Device",
                options = setOf(VAL_PREF_EMAIL_CONTENT_DEVICE_NAME,
                        VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME)
        )

        assertThat(formatter.formatBody(), htmlEquals("footer_time_device_option.html"))
    }

    /**
     * Check email body footer with [Settings.VAL_PREF_EMAIL_CONTENT_LOCATION] option switched on.
     */
    @Test
    fun testFooterLocation() {
        val event = PhoneEvent(
                phone = "+70123456789",
                isIncoming = true,
                startTime = 0,
                text = "Email body text",
                location = defaultCoordinates,
                acceptor = "device"   ,
                processTime = defaultTime
        )

        val formatter = MailFormatter(context, event,
                options = setOf(VAL_PREF_EMAIL_CONTENT_LOCATION)
        )

        assertThat(formatter.formatBody(), htmlEquals("footer_location_option.html"))
    }

    /**
     * Check email body footer with [Settings.VAL_PREF_EMAIL_CONTENT_DEVICE_NAME] option
     * switched on and no location specified in event.
     */
    @Test
    fun testFooterNoLocation() {
        val event = PhoneEvent(
                phone = "+70123456789",
                isIncoming = true,
                startTime = 0,
                text = "Email body text",
                acceptor = "device"    ,
                processTime = defaultTime
        )

        val formatter = MailFormatter(context, event,
                options = setOf(VAL_PREF_EMAIL_CONTENT_LOCATION)
        )

        assertThat(formatter.formatBody(), htmlEquals("footer_no_location.html"))
    }

    /**
     * Check email body footer with [Settings.VAL_PREF_EMAIL_CONTENT_DEVICE_NAME] option
     * switched on and no location permissions.
     */
    @Test
    fun testFooterNoLocationPermissions() {
        val event = PhoneEvent(
                phone = "+70123456789",
                isIncoming = true,
                startTime = 0,
                text = "Email body text",
                acceptor = "device"    ,
                processTime = defaultTime
        )

        whenever(context.checkPermission(eq(ACCESS_COARSE_LOCATION), anyInt(), anyInt())).thenReturn(PERMISSION_DENIED)
        whenever(context.checkPermission(eq(ACCESS_FINE_LOCATION), anyInt(), anyInt())).thenReturn(PERMISSION_DENIED)

        val formatter = MailFormatter(context, event,
                options = setOf(VAL_PREF_EMAIL_CONTENT_LOCATION)
        )

        assertThat(formatter.formatBody(), htmlEquals("footer_no_location_permission.html"))
    }

    /**
     * Check email body footer with [Settings.VAL_PREF_EMAIL_CONTENT_CONTACT] option switched on.
     */
    @Test
    fun testContactName() {
        val event = PhoneEvent(
                phone = "+12345678901",
                isIncoming = true,
                startTime = 0,
                text = "Email body text",
                acceptor = "device"  ,
                processTime = defaultTime
        )

        val formatter = MailFormatter(context, event,
                contactName = "John Dou",
                options = setOf(VAL_PREF_EMAIL_CONTENT_CONTACT)
        )

        assertThat(formatter.formatBody(), htmlEquals("footer_contact_option.html"))
    }

    /**
     * Check email body footer with [Settings.VAL_PREF_EMAIL_CONTENT_CONTACT] option switched on
     * and no permission to read contacts.
     */
    @Test
    fun testContactNameNoPermission() {
        val event = PhoneEvent(
                phone = "+12345678901",
                isIncoming = true,
                startTime = 0,
                text = "Email body text",
                acceptor = "device"     ,
                processTime = defaultTime
        )

        whenever(context.checkPermission(eq(READ_CONTACTS), anyInt(), anyInt())).thenReturn(PERMISSION_DENIED)

        val formatter = MailFormatter(context, event,
                options = setOf(VAL_PREF_EMAIL_CONTENT_CONTACT)
        )

        assertThat(formatter.formatBody(), htmlEquals("footer_contact_no_permission.html"))
    }

    /**
     * Check email body footer with [Settings.VAL_PREF_EMAIL_CONTENT_CONTACT] option switched on
     * and unknown contact name.
     */
    @Test
    fun testUnknownContactName() {
        val event = PhoneEvent(
                phone = "+1234 5678-901",
                isIncoming = true,
                startTime = 0,
                text = "Email body text",
                acceptor = "device"    ,
                processTime = defaultTime
        )

        val formatter = MailFormatter(context, event,
                options = setOf(VAL_PREF_EMAIL_CONTENT_CONTACT)
        )

        assertThat(formatter.formatBody(), htmlEquals("footer_unknown_contact.html"))
    }

    /**
     * Check incoming call email body.
     */
    @Test
    fun testIncomingCallBody() {
        val event = PhoneEvent(
                phone = "+70123456789",
                isIncoming = true,
                startTime = defaultTime,
                endTime = GregorianCalendar(2016, 1, 2, 4, 5, 10).time.time,
                acceptor = "device"
        )

        val formatter = MailFormatter(context, event)

        assertThat(formatter.formatBody(), htmlEquals("incoming_call.html"))
    }

    /**
     * Check outgoing call email body.
     */
    @Test
    fun testOutgoingCallBody() {
        val event = PhoneEvent(
                phone = "+70123456789",
                isIncoming = false,
                startTime = defaultTime,
                endTime = GregorianCalendar(2016, 1, 2, 4, 5, 15).time.time,
                acceptor = "device"
        )

        val formatter = MailFormatter(context, event)

        assertThat(formatter.formatBody(), htmlEquals("outgoing_call.html"))
    }

    /**
     * Check missed call email body.
     */
    @Test
    fun testMissedCallBody() {
        val event = PhoneEvent(
                phone = "+70123456789",
                startTime = defaultTime,
                isMissed = true,
                acceptor = "device"
        )

        val formatter = MailFormatter(context, event)

        assertThat(formatter.formatBody(), htmlEquals("missed_call.html"))
    }

    /**
     * Check incoming call email body when all required information is present.
     */
    @Test
    fun testAllContentIncomingCall() {
        val event = PhoneEvent(
                phone = "+12345678901",
                isIncoming = true,
                startTime = defaultTime,
                endTime = GregorianCalendar(2016, 1, 2, 4, 5, 10).time.time,
                location = defaultCoordinates,
                acceptor = "device" ,
                processTime = defaultTime
        )

        val formatter = MailFormatter(context, event,
                contactName = "John Dou",
                deviceName = "Device",
                options = setOf(
                        VAL_PREF_EMAIL_CONTENT_CONTACT,
                        VAL_PREF_EMAIL_CONTENT_LOCATION,
                        VAL_PREF_EMAIL_CONTENT_DEVICE_NAME,
                        VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME,
                        VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME_SENT)
        )

        assertThat(formatter.formatBody(), htmlEquals("incoming_call_all.html"))
    }

    /**
     * Check outgoing call email body when all required information is present.
     */
    @Test
    fun testAllContentOutgoingCall() {
        val event = PhoneEvent(
                phone = "+12345678901",
                isIncoming = false,
                startTime = defaultTime,
                endTime = GregorianCalendar(2016, 1, 2, 4, 5, 10).time.time,
                location = defaultCoordinates,
                acceptor = "device"  ,
                processTime = defaultTime
        )

        val formatter = MailFormatter(context, event,
                contactName = "John Dou",
                deviceName = "Device",
                options = setOf(VAL_PREF_EMAIL_CONTENT_CONTACT,
                        VAL_PREF_EMAIL_CONTENT_LOCATION,
                        VAL_PREF_EMAIL_CONTENT_DEVICE_NAME,
                        VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME,
                        VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME_SENT)
        )

        assertThat(formatter.formatBody(), htmlEquals("outgoing_call_all.html"))
    }

    /**
     * Check missed call email body when all required information is present.
     */
    @Test
    fun testAllContentMissedCall() {
        val event = PhoneEvent(
                phone = "+12345678901",
                isIncoming = true,
                startTime = defaultTime,
                endTime = GregorianCalendar(2016, 1, 2, 4, 5, 10).time.time,
                isMissed = true,
                location = defaultCoordinates,
                acceptor = "device"    ,
                processTime = defaultTime
        )

        val formatter = MailFormatter(context, event,
                deviceName = "Device",
                options = setOf(VAL_PREF_EMAIL_CONTENT_CONTACT,
                        VAL_PREF_EMAIL_CONTENT_LOCATION,
                        VAL_PREF_EMAIL_CONTENT_DEVICE_NAME,
                        VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME,
                        VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME_SENT)
        )

        assertThat(formatter.formatBody(), htmlEquals("missed_call_all.html"))
    }

    /**
     * Check email body with valid non-default locale specified.
     */
    @Test
    fun testNonDefaultLocale() {
        val calendar = GregorianCalendar(TimeZone.getTimeZone("EST"))

        calendar.set(2016, 1, 2, 3, 4, 5)
        val start = calendar.time.time

        calendar.set(2016, 1, 2, 3, 4, 10)
        val end = calendar.time.time

        val event = PhoneEvent(
                phone = "+12345678901",
                isIncoming = true,
                startTime = start,
                endTime = end,
                isMissed = true,
                location = defaultCoordinates,
                acceptor = "device"  ,
                processTime = defaultTime
        )

        var formatter = MailFormatter(context, event,
                deviceName = "Device",
                locale = Locale("ru", "ru"),
                options = setOf(VAL_PREF_EMAIL_CONTENT_CONTACT,
                        VAL_PREF_EMAIL_CONTENT_LOCATION,
                        VAL_PREF_EMAIL_CONTENT_DEVICE_NAME,
                        VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME,
                        VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME_SENT)
        )

        assertEquals("[SMailer] Пропущенный звонок от +12345678901", formatter.formatSubject())
        assertThat(formatter.formatBody(), htmlEquals("missed_call_ru.html"))

        formatter = MailFormatter(context, event,
                deviceName = "Device",
                locale = Locale.getDefault(),
                options = setOf(VAL_PREF_EMAIL_CONTENT_CONTACT,
                        VAL_PREF_EMAIL_CONTENT_LOCATION,
                        VAL_PREF_EMAIL_CONTENT_DEVICE_NAME,
                        VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME,
                        VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME_SENT)
        )

        assertEquals("[SMailer] Missed call from +12345678901", formatter.formatSubject())
        assertThat(formatter.formatBody(), htmlEquals("missed_call_en.html"))
    }

    /**
     * Check URLs formatting.
     */
    @Test
    fun testFormatUrls() {
        val event = PhoneEvent(
                phone = "+12345678901",
                isIncoming = true,
                startTime = 0,
                text = "Please visit https://www.google.com/search?a=1&b=2 or " +
                        "https://secure.place  site or download from ftp://get.from.here",
                acceptor = "device"
        )

        val formatter = MailFormatter(context, event)

        assertThat(formatter.formatBody(), htmlEquals("urls.html"))
    }

    /**
     * Check remote control links formatting.
     */
    @Test
    fun testRemoteControlLinks() {
        val event = PhoneEvent(
                phone = "+12345678901",
                isIncoming = true,
                startTime = defaultTime,
                endTime = GregorianCalendar(2016, 1, 2, 4, 5, 10).time.time,
                text = "Message",
                location = defaultCoordinates,
                acceptor = "device"
        )

        val formatter = MailFormatter(context, event,
                deviceName = "Device",
                serviceAccount = "service@mail.com",
                options = setOf(VAL_PREF_EMAIL_CONTENT_REMOTE_COMMAND_LINKS)
        )

        assertThat(formatter.formatBody(), htmlEquals("remote_control_links.html"))
    }
}