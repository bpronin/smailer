package com.bopr.android.smailer.processor.telegram

import androidx.test.filters.SmallTest
import com.bopr.android.smailer.BaseTest
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.PREF_DEVICE_ALIAS
import com.bopr.android.smailer.Settings.Companion.PREF_TELEGRAM_MESSAGE_SHOW_CALLER
import com.bopr.android.smailer.Settings.Companion.PREF_TELEGRAM_MESSAGE_SHOW_DEVICE_NAME
import com.bopr.android.smailer.Settings.Companion.PREF_TELEGRAM_MESSAGE_SHOW_HEADER
import com.bopr.android.smailer.Settings.Companion.PREF_TELEGRAM_MESSAGE_SHOW_EVENT_TIME
import com.bopr.android.smailer.Settings.Companion.PREF_TELEGRAM_MESSAGE_SHOW_LOCATION
import com.bopr.android.smailer.Settings.Companion.PREF_TELEGRAM_MESSAGE_SHOW_PROCESS_TIME
import com.bopr.android.smailer.provider.telephony.PhoneEventData
import com.bopr.android.smailer.util.GeoCoordinates
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.GregorianCalendar

/**
 * [TelegramPhoneEventFormatter] class tester.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
@SmallTest
class TelegramPhoneEventFormatterTest : BaseTest() {

    private val testTime = GregorianCalendar(2016, 1, 2, 3, 4, 5).time.time
    private val testCoordinates = GeoCoordinates(60.555, 30.555)

    @Test
    fun testAllOptionsOff() {
        Settings(targetContext).update {
            clear()
            putBoolean(PREF_TELEGRAM_MESSAGE_SHOW_HEADER, false)
            putBoolean(PREF_TELEGRAM_MESSAGE_SHOW_CALLER, false)
            putBoolean(PREF_TELEGRAM_MESSAGE_SHOW_DEVICE_NAME, false)
            putBoolean(PREF_TELEGRAM_MESSAGE_SHOW_EVENT_TIME, false)
            putBoolean(PREF_TELEGRAM_MESSAGE_SHOW_PROCESS_TIME, false)
            putBoolean(PREF_TELEGRAM_MESSAGE_SHOW_LOCATION, false)
            putString(PREF_DEVICE_ALIAS, "My Device")
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

        assertEquals("Message text", formatter.formatMessage())
    }

    @Test
    fun testAllOptionsOn() {
        Settings(targetContext).update {
            clear()
            putBoolean(PREF_TELEGRAM_MESSAGE_SHOW_HEADER, true)
            putBoolean(PREF_TELEGRAM_MESSAGE_SHOW_CALLER, true)
            putBoolean(PREF_TELEGRAM_MESSAGE_SHOW_DEVICE_NAME, true)
            putBoolean(PREF_TELEGRAM_MESSAGE_SHOW_EVENT_TIME, true)
            putBoolean(PREF_TELEGRAM_MESSAGE_SHOW_PROCESS_TIME, true)
            putBoolean(PREF_TELEGRAM_MESSAGE_SHOW_LOCATION, true)
            putString(PREF_DEVICE_ALIAS, "My Device")
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
        val message = formatter.formatMessage()

        assertEquals(
            "<b>Incoming SMS at 2/2/16 3:04 AM</b>" +
                    "%0A%0A" +
                    "Message text" +
                    "%0A%0A" +
                    "<i>Sender: +12345678901 (Unknown contact)</i>" +
                    "%0A" +
                    "<i>Sent from My Device at 2/2/16 3:04 AM</i>" +
                    "%0A" +
                    "<i>Last known device location: " +
                    "<a href=\"https://www.google.com/maps/place/60.555+30.555/@60.555,30.555\">" +
                    "60&#176;33'17\"N, 30&#176;33'17\"W" +
                    "</a>" +
                    "</i>",
            message
        )
    }
}