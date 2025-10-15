package com.bopr.android.smailer.util

import androidx.test.filters.SmallTest
import com.bopr.android.smailer.BaseTest
import com.bopr.android.smailer.messenger.Event
import com.bopr.android.smailer.provider.telephony.PhoneCallData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Test
import java.util.GregorianCalendar

@SmallTest
class ParseUtilTest : BaseTest() {

    @Test
    fun testParcelize() {
        val source = Event(
            payload = PhoneCallData(
                startTime = GregorianCalendar(2016, 1, 2, 3, 4, 5).time.time,
                phone = "+12345678901",
                isIncoming = true,
                endTime = GregorianCalendar(2016, 1, 2, 4, 5, 10).time.time,
                text = "Message"
            ),
            location = GeoLocation(60.555, 30.555),
            target = "device"
        )

        val bytes = parcelize(source)
        val actual = unparcelize(bytes, source.javaClass.kotlin)

        assertEquals(source, actual)
        assertNotSame(source, actual)
    }

}