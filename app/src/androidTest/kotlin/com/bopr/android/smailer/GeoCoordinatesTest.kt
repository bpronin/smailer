package com.bopr.android.smailer

import androidx.test.filters.SmallTest
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * [GeoCoordinates] class tester.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
@SmallTest
class GeoCoordinatesTest : BaseTest() {

    @Test
    fun testFormat() {
        assertEquals("30d33m59sn, 60d33m59sw", GeoCoordinates(30.5664, 60.5664).format(
                "d", "m", "s", "n", "s", "w", "e"))
        assertEquals("30°33'59\"N, 60°33'59\"W", GeoCoordinates(30.5664, 60.5664).format())
        assertEquals("30°33'59\"S, 60°33'59\"E", GeoCoordinates(-30.5664, -60.5664).format())
    }

}