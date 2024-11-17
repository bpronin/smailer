package com.bopr.android.smailer.messenger.mail

import android.content.Context
import android.content.res.Configuration
import androidx.test.filters.SmallTest
import com.bopr.android.smailer.BaseTest
import com.bopr.android.smailer.util.GeoLocation
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.junit.Before
import java.util.GregorianCalendar

/**
 * [PhoneCallEmailFormatter] class tester.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
@SmallTest
class PhoneCallEmailFormatterTest : BaseTest() {

    private lateinit var context: Context
    private val defaultTime = GregorianCalendar(2016, 1, 2, 3, 4, 5).time.time
    private val defaultCoordinates = GeoLocation(60.555, 30.555)

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
/*
    *//**
     * Check incoming SMS email body when all required information is present.
     *//*
    @Test
    fun testAllContentIncomingSms() {
        val event = PhoneEventData(
            phone = "+12345678901",
            isIncoming = true,
            startTime = defaultTime,
            text = "Message",
            location = defaultCoordinates,
            acceptor = "device",
            processTime = defaultTime
        )

        val formatter = MailPhoneEventFormatter(
            context, event,
            contactName = "John Dou",
            deviceName = "Device",
            serviceAccount = "service@mail.com",
            options = setOf(
                VAL_PREF_MESSAGE_CONTENT_HEADER,
                VAL_PREF_MESSAGE_CONTENT_CALLER,
                VAL_PREF_MESSAGE_CONTENT_LOCATION,
                VAL_PREF_MESSAGE_CONTENT_DEVICE_NAME,
                VAL_PREF_MESSAGE_CONTENT_EVENT_TIME,
                VAL_PREF_MESSAGE_CONTENT_DISPATCH_TIME,
                VAL_PREF_MESSAGE_CONTENT_CONTROL_LINKS
            )
        )

        MatcherAssert.assertThat(
            formatter.formatBody(),
            HtmlMatcher.htmlEquals("incoming_sms_all.html")
        )
    }

    *//**
     * Check formatting incoming sms email subject.
     *//*
    @Test
    fun testIncomingSmsSubject() {
        val event = PhoneEventData(
            phone = "+70123456789",
            isIncoming = true,
            startTime = 0,
            text = "Email body text",
            acceptor = "device"
        )

        val formatter = MailPhoneEventFormatter(context, event)

        Assert.assertEquals("[SMailer] Incoming SMS from +70123456789", formatter.formatSubject())
    }

    *//**
     * Check formatting outgoing sms email subject.
     *//*
    @Test
    fun testOutgoingSmsSubject() {
        val event = PhoneEventData(
            phone = "+70123456789",
            isIncoming = false,
            startTime = 0,
            text = "Email body text",
            acceptor = "device"
        )

        val formatter = MailPhoneEventFormatter(context, event)

        Assert.assertEquals("[SMailer] Outgoing SMS to +70123456789", formatter.formatSubject())
    }

    *//**
     * Check formatting incoming call email subject.
     *//*
    @Test
    fun testIncomingCallSubject() {
        val event = PhoneEventData(
            phone = "+70123456789",
            isIncoming = true,
            startTime = 0,
            acceptor = "device"
        )

        val formatter = MailPhoneEventFormatter(context, event)

        Assert.assertEquals("[SMailer] Incoming call from +70123456789", formatter.formatSubject())
    }

    *//**
     * Check formatting outgoing call email subject.
     *//*
    @Test
    fun testOutgoingCallSubject() {
        val event = PhoneEventData(
            phone = "+70123456789",
            isIncoming = false,
            startTime = 0,
            acceptor = "device"
        )

        val formatter = MailPhoneEventFormatter(context, event)

        Assert.assertEquals("[SMailer] Outgoing call to +70123456789", formatter.formatSubject())
    }

    *//**
     * Check formatting outgoing call email subject.
     *//*
    @Test
    fun testMissedCallSubject() {
        val event = PhoneEventData(
            phone = "+70123456789",
            startTime = 0,
            isMissed = true,
            acceptor = "device"
        )

        val formatter = MailPhoneEventFormatter(context, event)

        Assert.assertEquals("[SMailer] Missed call from +70123456789", formatter.formatSubject())
    }

    *//**
     * Check that email body does not contain any footer when no options have been chosen.
     *//*
    @Test
    fun testNoBodyFooter() {
        val event = PhoneEventData(
            phone = "+70123456789",
            isIncoming = true,
            startTime = 0,
            text = "Email body text",
            acceptor = "device"
        )

        val formatter = MailPhoneEventFormatter(context, event)

        MatcherAssert.assertThat(
            formatter.formatBody(),
            HtmlMatcher.htmlEquals("no_body_footer.html")
        )
    }

    *//**
     * Check email body footer with [Settings.VAL_PREF_MESSAGE_CONTENT_EVENT_TIME] option switched on.
     *//*
    @Test
    fun testFooterTimeOption() {
        val event = PhoneEventData(
            phone = "+70123456789",
            isIncoming = true,
            startTime = defaultTime,
            text = "Email body text",
            acceptor = "device",
            processTime = defaultTime
        )

        val formatter = MailPhoneEventFormatter(
            context, event,
            options = setOf(VAL_PREF_MESSAGE_CONTENT_EVENT_TIME)
        )

        MatcherAssert.assertThat(
            formatter.formatBody(),
            HtmlMatcher.htmlEquals("footer_time_option.html")
        )
    }

    *//**
     * Check email body footer with [Settings.VAL_PREF_MESSAGE_CONTENT_DEVICE_NAME] option switched on.
     *//*
    @Test
    fun testFooterDeviceNameOption() {
        val event = PhoneEventData(
            phone = "+70123456789",
            isIncoming = true,
            startTime = 0,
            text = "Email body text",
            acceptor = "device",
            processTime = defaultTime
        )

        val formatter = MailPhoneEventFormatter(
            context, event,
            deviceName = "The Device",
            options = setOf(VAL_PREF_MESSAGE_CONTENT_DEVICE_NAME)
        )

        MatcherAssert.assertThat(
            formatter.formatBody(),
            HtmlMatcher.htmlEquals("footer_device_option.html")
        )
    }

    *//**
     * Check email body footer with [Settings.VAL_PREF_MESSAGE_CONTENT_DEVICE_NAME] option
     * switched on and no devise name specified.
     *//*
    @Test
    fun testFooterDeviceNameOptionNoValue() {
        val event = PhoneEventData(
            phone = "+70123456789",
            isIncoming = true,
            startTime = 0,
            text = "Email body text",
            acceptor = "device",
            processTime = defaultTime
        )

        val formatter = MailPhoneEventFormatter(
            context, event,
            options = setOf(VAL_PREF_MESSAGE_CONTENT_DEVICE_NAME)
        )

        MatcherAssert.assertThat(
            formatter.formatBody(),
            HtmlMatcher.htmlEquals("footer_no_device_option.html")
        )
    }

    *//**
     * Check email body footer with [Settings.VAL_PREF_MESSAGE_CONTENT_EVENT_TIME] and
     * [Settings.VAL_PREF_MESSAGE_CONTENT_DEVICE_NAME] options switched on.
     *//*
    @Test
    fun testFooterDeviceNameTimeOption() {
        val event = PhoneEventData(
            phone = "+70123456789",
            isIncoming = true,
            startTime = defaultTime,
            text = "Email body text",
            acceptor = "device",
            processTime = defaultTime
        )

        val formatter = MailPhoneEventFormatter(
            context, event,
            deviceName = "The Device",
            options = setOf(
                VAL_PREF_MESSAGE_CONTENT_DEVICE_NAME,
                VAL_PREF_MESSAGE_CONTENT_EVENT_TIME
            )
        )

        MatcherAssert.assertThat(
            formatter.formatBody(),
            HtmlMatcher.htmlEquals("footer_time_device_option.html")
        )
    }

    *//**
     * Check email body footer with [Settings.VAL_PREF_MESSAGE_CONTENT_LOCATION] option switched on.
     *//*
    @Test
    fun testFooterLocation() {
        val event = PhoneEventData(
            phone = "+70123456789",
            isIncoming = true,
            startTime = 0,
            text = "Email body text",
            location = defaultCoordinates,
            acceptor = "device",
            processTime = defaultTime
        )

        val formatter = MailPhoneEventFormatter(
            context, event,
            options = setOf(VAL_PREF_MESSAGE_CONTENT_LOCATION)
        )

        MatcherAssert.assertThat(
            formatter.formatBody(),
            HtmlMatcher.htmlEquals("footer_location_option.html")
        )
    }

    *//**
     * Check email body footer with [Settings.VAL_PREF_MESSAGE_CONTENT_DEVICE_NAME] option
     * switched on and no location specified in event.
     *//*
    @Test
    fun testFooterNoLocation() {
        val event = PhoneEventData(
            phone = "+70123456789",
            isIncoming = true,
            startTime = 0,
            text = "Email body text",
            acceptor = "device",
            processTime = defaultTime
        )

        val formatter = MailPhoneEventFormatter(
            context, event,
            options = setOf(VAL_PREF_MESSAGE_CONTENT_LOCATION)
        )

        MatcherAssert.assertThat(
            formatter.formatBody(),
            HtmlMatcher.htmlEquals("footer_no_location.html")
        )
    }

    *//**
     * Check email body footer with [Settings.VAL_PREF_MESSAGE_CONTENT_DEVICE_NAME] option
     * switched on and no location permissions.
     *//*
    @Test
    fun testFooterNoLocationPermissions() {
        val event = PhoneEventData(
            phone = "+70123456789",
            isIncoming = true,
            startTime = 0,
            text = "Email body text",
            acceptor = "device",
            processTime = defaultTime
        )

        whenever(
            context.checkPermission(
                eq(Manifest.permission.ACCESS_COARSE_LOCATION),
                ArgumentMatchers.anyInt(),
                ArgumentMatchers.anyInt()
            )
        ).thenReturn(PackageManager.PERMISSION_DENIED)
        whenever(
            context.checkPermission(
                eq(Manifest.permission.ACCESS_FINE_LOCATION),
                ArgumentMatchers.anyInt(),
                ArgumentMatchers.anyInt()
            )
        ).thenReturn(PackageManager.PERMISSION_DENIED)

        val formatter = MailPhoneEventFormatter(
            context, event,
            options = setOf(VAL_PREF_MESSAGE_CONTENT_LOCATION)
        )

        MatcherAssert.assertThat(
            formatter.formatBody(),
            HtmlMatcher.htmlEquals("footer_no_location_permission.html")
        )
    }

    *//**
     * Check email body footer with [Settings.VAL_PREF_MESSAGE_CONTENT_CALLER] option switched on.
     *//*
    @Test
    fun testContactName() {
        val event = PhoneEventData(
            phone = "+12345678901",
            isIncoming = true,
            startTime = 0,
            text = "Email body text",
            acceptor = "device",
            processTime = defaultTime
        )

        val formatter = MailPhoneEventFormatter(
            context, event,
            contactName = "John Dou",
            options = setOf(VAL_PREF_MESSAGE_CONTENT_CALLER)
        )

        MatcherAssert.assertThat(
            formatter.formatBody(),
            HtmlMatcher.htmlEquals("footer_contact_option.html")
        )
    }

    *//**
     * Check email body footer with [Settings.VAL_PREF_MESSAGE_CONTENT_CALLER] option switched on
     * and no permission to read contacts.
     *//*
    @Test
    fun testContactNameNoPermission() {
        val event = PhoneEventData(
            phone = "+12345678901",
            isIncoming = true,
            startTime = 0,
            text = "Email body text",
            acceptor = "device",
            processTime = defaultTime
        )

        whenever(
            context.checkPermission(
                eq(Manifest.permission.READ_CONTACTS),
                ArgumentMatchers.anyInt(),
                ArgumentMatchers.anyInt()
            )
        ).thenReturn(PackageManager.PERMISSION_DENIED)

        val formatter = MailPhoneEventFormatter(
            context, event,
            options = setOf(VAL_PREF_MESSAGE_CONTENT_CALLER)
        )

        MatcherAssert.assertThat(
            formatter.formatBody(),
            HtmlMatcher.htmlEquals("footer_contact_no_permission.html")
        )
    }

    *//**
     * Check email body footer with [Settings.VAL_PREF_MESSAGE_CONTENT_CALLER] option switched on
     * and unknown contact name.
     *//*
    @Test
    fun testUnknownContactName() {
        val event = PhoneEventData(
            phone = "+1234 5678-901",
            isIncoming = true,
            startTime = 0,
            text = "Email body text",
            acceptor = "device",
            processTime = defaultTime
        )

        val formatter = MailPhoneEventFormatter(
            context, event,
            options = setOf(VAL_PREF_MESSAGE_CONTENT_CALLER)
        )

        MatcherAssert.assertThat(
            formatter.formatBody(),
            HtmlMatcher.htmlEquals("footer_unknown_contact.html")
        )
    }

    *//**
     * Check email body footer with [Settings.VAL_PREF_MESSAGE_CONTENT_CALLER] option switched on,
     *  unknown contact name and custom search URL.
     *//*
    @Test
    fun testSearchPhoneLink() {
        val event = PhoneEventData(
            phone = "+1234 5678-901",
            isIncoming = true,
            startTime = 0,
            text = "Email body text",
            acceptor = "device",
            processTime = defaultTime
        )

        val formatter = MailPhoneEventFormatter(
            context, event,
            phoneSearchUrl = "https://www.neberitrubku.ru/nomer-telefona/{phone}",
            options = setOf(VAL_PREF_MESSAGE_CONTENT_CALLER)
        )

        MatcherAssert.assertThat(
            formatter.formatBody(),
            HtmlMatcher.htmlEquals("footer_search_contact.html")
        )
    }

    *//**
     * Check incoming call email body.
     *//*
    @Test
    fun testIncomingCallBody() {
        val event = PhoneEventData(
            phone = "+70123456789",
            isIncoming = true,
            startTime = defaultTime,
            endTime = GregorianCalendar(2016, 1, 2, 4, 5, 10).time.time,
            acceptor = "device"
        )

        val formatter = MailPhoneEventFormatter(context, event)

        MatcherAssert.assertThat(
            formatter.formatBody(),
            HtmlMatcher.htmlEquals("incoming_call.html")
        )
    }

    *//**
     * Check outgoing call email body.
     *//*
    @Test
    fun testOutgoingCallBody() {
        val event = PhoneEventData(
            phone = "+70123456789",
            isIncoming = false,
            startTime = defaultTime,
            endTime = GregorianCalendar(2016, 1, 2, 4, 5, 15).time.time,
            acceptor = "device"
        )

        val formatter = MailPhoneEventFormatter(context, event)

        MatcherAssert.assertThat(
            formatter.formatBody(),
            HtmlMatcher.htmlEquals("outgoing_call.html")
        )
    }

    *//**
     * Check missed call email body.
     *//*
    @Test
    fun testMissedCallBody() {
        val event = PhoneEventData(
            phone = "+70123456789",
            startTime = defaultTime,
            isMissed = true,
            acceptor = "device"
        )

        val formatter = MailPhoneEventFormatter(context, event)

        MatcherAssert.assertThat(formatter.formatBody(), HtmlMatcher.htmlEquals("missed_call.html"))
    }

    *//**
     * Check incoming call email body when all required information is present.
     *//*
    @Test
    fun testAllContentIncomingCall() {
        val event = PhoneEventData(
            phone = "+12345678901",
            isIncoming = true,
            startTime = defaultTime,
            endTime = GregorianCalendar(2016, 1, 2, 4, 5, 10).time.time,
            location = defaultCoordinates,
            acceptor = "device",
            processTime = defaultTime
        )

        val formatter = MailPhoneEventFormatter(
            context, event,
            contactName = "John Dou",
            deviceName = "Device",
            options = setOf(
                VAL_PREF_MESSAGE_CONTENT_CALLER,
                VAL_PREF_MESSAGE_CONTENT_LOCATION,
                VAL_PREF_MESSAGE_CONTENT_DEVICE_NAME,
                VAL_PREF_MESSAGE_CONTENT_EVENT_TIME,
                VAL_PREF_MESSAGE_CONTENT_DISPATCH_TIME
            )
        )

        MatcherAssert.assertThat(
            formatter.formatBody(),
            HtmlMatcher.htmlEquals("incoming_call_all.html")
        )
    }

    *//**
     * Check outgoing call email body when all required information is present.
     *//*
    @Test
    fun testAllContentOutgoingCall() {
        val event = PhoneEventData(
            phone = "+12345678901",
            isIncoming = false,
            startTime = defaultTime,
            endTime = GregorianCalendar(2016, 1, 2, 4, 5, 10).time.time,
            location = defaultCoordinates,
            acceptor = "device",
            processTime = defaultTime
        )

        val formatter = MailPhoneEventFormatter(
            context, event,
            contactName = "John Dou",
            deviceName = "Device",
            options = setOf(
                VAL_PREF_MESSAGE_CONTENT_CALLER,
                VAL_PREF_MESSAGE_CONTENT_LOCATION,
                VAL_PREF_MESSAGE_CONTENT_DEVICE_NAME,
                VAL_PREF_MESSAGE_CONTENT_EVENT_TIME,
                VAL_PREF_MESSAGE_CONTENT_DISPATCH_TIME
            )
        )

        MatcherAssert.assertThat(
            formatter.formatBody(),
            HtmlMatcher.htmlEquals("outgoing_call_all.html")
        )
    }

    *//**
     * Check missed call email body when all required information is present.
     *//*
    @Test
    fun testAllContentMissedCall() {
        val event = PhoneEventData(
            phone = "+12345678901",
            isIncoming = true,
            startTime = defaultTime,
            endTime = GregorianCalendar(2016, 1, 2, 4, 5, 10).time.time,
            isMissed = true,
            location = defaultCoordinates,
            acceptor = "device",
            processTime = defaultTime
        )

        val formatter = MailPhoneEventFormatter(
            context, event,
            deviceName = "Device",
            options = setOf(
                VAL_PREF_MESSAGE_CONTENT_CALLER,
                VAL_PREF_MESSAGE_CONTENT_LOCATION,
                VAL_PREF_MESSAGE_CONTENT_DEVICE_NAME,
                VAL_PREF_MESSAGE_CONTENT_EVENT_TIME,
                VAL_PREF_MESSAGE_CONTENT_DISPATCH_TIME
            )
        )

        MatcherAssert.assertThat(
            formatter.formatBody(),
            HtmlMatcher.htmlEquals("missed_call_all.html")
        )
    }

    *//**
     * Check email body with valid non-default locale specified.
     *//*
    @Test
    fun testNonDefaultLocale() {
        val event = PhoneEventData(
            phone = "+12345678901",
            isIncoming = true,
            startTime = defaultTime,
            text = "Message",
            location = defaultCoordinates,
            acceptor = "device",
            processTime = defaultTime
        )

        val formatter = MailPhoneEventFormatter(
            context, event,
            contactName = "John Dou",
            deviceName = "Device",
            serviceAccount = "service@mail.com",
            locale = Locale("ru", "ru"),
            options = setOf(
                VAL_PREF_MESSAGE_CONTENT_HEADER,
                VAL_PREF_MESSAGE_CONTENT_CALLER,
                VAL_PREF_MESSAGE_CONTENT_LOCATION,
                VAL_PREF_MESSAGE_CONTENT_DEVICE_NAME,
                VAL_PREF_MESSAGE_CONTENT_EVENT_TIME,
                VAL_PREF_MESSAGE_CONTENT_DISPATCH_TIME,
                VAL_PREF_MESSAGE_CONTENT_CONTROL_LINKS
            )
        )

        Assert.assertEquals("[SMailer] Входящее SMS от +12345678901", formatter.formatSubject())
        MatcherAssert.assertThat(
            formatter.formatBody(),
            HtmlMatcher.htmlEquals("incoming_sms_all_ru.html")
        )
    }

    *//**
     * Check URLs formatting.
     *//*
    @Test
    fun testFormatUrls() {
        val event = PhoneEventData(
            phone = "+12345678901",
            isIncoming = true,
            startTime = 0,
            text = "Please visit https://www.google.com/search?a=1&b=2 or " +
                    "https://secure.place  site or download from ftp://get.from.here",
            acceptor = "device"
        )

        val formatter = MailPhoneEventFormatter(context, event)

        MatcherAssert.assertThat(formatter.formatBody(), HtmlMatcher.htmlEquals("urls.html"))
    }

    *//**
     * Check remote control links formatting.
     *//*
    @Test
    fun testRemoteControlLinks() {
        val event = PhoneEventData(
            phone = "+12345678901",
            isIncoming = true,
            startTime = defaultTime,
            endTime = GregorianCalendar(2016, 1, 2, 4, 5, 10).time.time,
            text = "Message",
            location = defaultCoordinates,
            acceptor = "device"
        )

        val formatter = MailPhoneEventFormatter(
            context, event,
            deviceName = "Device",
            serviceAccount = "service@mail.com",
            options = setOf(VAL_PREF_MESSAGE_CONTENT_CONTROL_LINKS)
        )

        val body = formatter.formatBody()
        MatcherAssert.assertThat(body, HtmlMatcher.htmlEquals("remote_control_links.html"))

        *//* Ensure that there are no line breaks in href values. Otherwise reply email body will be formatted incorrectly *//*
        Assert.assertTrue(
            body.contains(
                "mailto:service@mail.com?subject=Re: [SMailer] Incoming SMS from " +
                        "+12345678901&amp;body=To device &quot;Device&quot;: %0d%0a add phone +12345678901 to blacklist"
            )
        )
        Assert.assertTrue(
            body.contains(
                "mailto:service@mail.com?subject=Re: [SMailer] Incoming SMS from " +
                        "+12345678901&amp;body=To device &quot;Device&quot;: %0d%0a add text &quot;Message&quot; to blacklist"
            )
        )
        Assert.assertTrue(
            body.contains(
                "mailto:service@mail.com?subject=Re: [SMailer] Incoming SMS from " +
                        "+12345678901&amp;body=To device &quot;Device&quot;: %0d%0a send SMS message &quot;Sample text&quot; to +12345678901"
            )
        )
    }*/
}