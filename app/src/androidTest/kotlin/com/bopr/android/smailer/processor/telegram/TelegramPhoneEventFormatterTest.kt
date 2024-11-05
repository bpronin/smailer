package com.bopr.android.smailer.processor.telegram

import android.Manifest.permission.READ_CONTACTS
import androidx.test.filters.SmallTest
import androidx.test.rule.GrantPermissionRule
import com.bopr.android.smailer.BaseTest
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.PREF_DEVICE_ALIAS
import com.bopr.android.smailer.Settings.Companion.PREF_TELEGRAM_MESSAGE_CONTENT
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_MESSAGE_CONTENT_BODY
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_MESSAGE_CONTENT_CALLER
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_MESSAGE_CONTENT_DEVICE_NAME
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_MESSAGE_CONTENT_DISPATCH_TIME
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_MESSAGE_CONTENT_EVENT_TIME
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_MESSAGE_CONTENT_HEADER
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_MESSAGE_CONTENT_LOCATION
import com.bopr.android.smailer.provider.telephony.PhoneEventData
import com.bopr.android.smailer.util.GeoLocation
import com.bopr.android.smailer.util.getContactName
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import java.util.GregorianCalendar

/**
 * [TelegramPhoneEventFormatter] class tester.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
@SmallTest
class TelegramPhoneEventFormatterTest : BaseTest() {

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(READ_CONTACTS)

    private val testTime = GregorianCalendar(2016, 1, 2, 3, 4, 5).time.time
    private val testCoordinates = GeoLocation(60.555, 30.555)

    @Test
    fun testAllOptionsOff() {
        Settings(targetContext).update {
            clear()
            putStringSet(
                PREF_TELEGRAM_MESSAGE_CONTENT, emptySet()
            )
        }

        val event = PhoneEventData(
            phone = "+12345678901",
            isIncoming = true,
            startTime = testTime,
            text = "Message text",
            location = testCoordinates,
            processTime = testTime,
            acceptor = "device"
        )

        val formatter = TelegramPhoneEventFormatter(targetContext, event)

        assertEquals("", formatter.formatMessage())
    }

    @Test
    fun testDefaultOptions() {
        Settings(targetContext).update {
            clear()
            putStringSet(
                PREF_TELEGRAM_MESSAGE_CONTENT, setOf(
                    VAL_PREF_MESSAGE_CONTENT_BODY
                )
            )
        }

        val event = PhoneEventData(
            phone = "+12345678901",
            isIncoming = true,
            startTime = testTime,
            text = "Message text",
            location = testCoordinates,
            processTime = testTime,
            acceptor = "device"
        )

        val formatter = TelegramPhoneEventFormatter(targetContext, event)

        assertEquals("Message text%0A", formatter.formatMessage())
    }

    @Test
    fun testAllOptionsOn() {
        /* ensure contact is present on the device */
        assertNotNull(targetContext.getContactName("+12345678901"))

        Settings(targetContext).update {
            clear()
            putStringSet(
                PREF_TELEGRAM_MESSAGE_CONTENT,
                setOf(
                    VAL_PREF_MESSAGE_CONTENT_HEADER,
                    VAL_PREF_MESSAGE_CONTENT_EVENT_TIME,
                    VAL_PREF_MESSAGE_CONTENT_BODY,
                    VAL_PREF_MESSAGE_CONTENT_CALLER,
                    VAL_PREF_MESSAGE_CONTENT_DISPATCH_TIME,
                    VAL_PREF_MESSAGE_CONTENT_DEVICE_NAME,
                    VAL_PREF_MESSAGE_CONTENT_LOCATION
                )
            )
            putString(
                PREF_DEVICE_ALIAS, "My Device"
            )
        }

        val event = PhoneEventData(
            phone = "+12345678901",
            isIncoming = true,
            startTime = testTime,
            text = "Message text",
            location = testCoordinates,
            acceptor = "device",
            processTime = testTime
        )

        val formatter = TelegramPhoneEventFormatter(targetContext, event)

        assertEquals(
            "<b>Incoming SMS</b>"
                    + "%0A"
                    + "<b>2/2/16 3:04 AM</b>"
                    + "%0A"
                    + "%0A"
                    + "Message text"
                    + "%0A"
                    + "%0A"
                    + "<i>Sender: +1 234-567-8901 (John Doe)</i>"
                    + "%0A"
                    + "<i>Sent from My Device on 2/2/16 at 3:04:05 AM</i>"
                    + "%0A"
                    + "<i>Last known device location: "
                    + "<a href=\"https://www.google.com/maps/place/60.555+30.555/@60.555,30.555\">"
                    + "60%26%23176%3B33%2717%22N%2C+30%26%23176%3B33%2717%22W</a>"
                    + "</i>",
            formatter.formatMessage()
        )
    }

    @Test
    fun testHasNoCallerContact() {
        /* ensure contact is not present on the device */
        assertNull(targetContext.getContactName("+12223334444"))

        Settings(targetContext).update {
            clear()
            putStringSet(
                PREF_TELEGRAM_MESSAGE_CONTENT,
                setOf(
                    VAL_PREF_MESSAGE_CONTENT_CALLER
                )
            )
        }

        val event = PhoneEventData(
            phone = "+12223334444",
            isIncoming = true,
            startTime = testTime,
            text = "Message text",
            location = testCoordinates,
            processTime = testTime,
            acceptor = "device"
        )

        val formatter = TelegramPhoneEventFormatter(targetContext, event)

        assertEquals("<i>Sender: +1 222-333-4444</i>", formatter.formatMessage())
    }
}