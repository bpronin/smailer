package com.bopr.android.smailer.util

import androidx.test.filters.SmallTest
import com.bopr.android.smailer.BaseTest
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * [GeoLocation] class tester.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
@SmallTest
class GeoLocationTest : BaseTest() {

    @Test
    fun testFormat() {
        assertEquals("30d33m59sn, 60d33m59sw", GeoLocation(30.5664, 60.5664).format(
                "d", "m", "s", "n", "s", "w", "e"))
        assertEquals("30°33'59\"N, 60°33'59\"W", GeoLocation(30.5664, 60.5664).format())
        assertEquals("30°33'59\"S, 60°33'59\"E", GeoLocation(-30.5664, -60.5664).format())
    }

}