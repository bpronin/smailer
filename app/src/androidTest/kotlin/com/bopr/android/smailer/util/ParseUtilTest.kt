package com.bopr.android.smailer.util

import androidx.test.filters.SmallTest
import com.bopr.android.smailer.BaseTest
import com.bopr.android.smailer.provider.telephony.PhoneEventInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Test
import java.util.GregorianCalendar

@SmallTest
class ParseUtilTest : BaseTest() {

    @Test
    fun testParcelize() {
        val source = PhoneEventInfo(
            phone = "+12345678901",
            isIncoming = true,
            startTime = GregorianCalendar(2016, 1, 2, 3, 4, 5).time.time,
            endTime = GregorianCalendar(2016, 1, 2, 4, 5, 10).time.time,
            text = "Message",
            location = GeoCoordinates(60.555, 30.555),
            acceptor = "device"
        )

        val bytes = parcelize(source)
        val actual = unparcelize(bytes, PhoneEventInfo::class)

        assertEquals(source, actual)
        assertNotSame(source, actual)
    }

}