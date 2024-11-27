package com.bopr.android.smailer.util

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import androidx.test.rule.GrantPermissionRule.*
import com.bopr.android.smailer.BaseTest
import com.bopr.android.smailer.util.GeoLocation.Companion.getGeoLocation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

/**
 * [GeoLocation] class tester.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class GeoLocationTest : BaseTest() {

    @get:Rule
    var permissionRule = grant(
        ACCESS_COARSE_LOCATION,
        ACCESS_FINE_LOCATION
    )

    @Test
    fun testFormat() {
        assertEquals(
            "30d33m59sn, 60d33m59sw", GeoLocation(30.5664, 60.5664).format(
                "d", "m", "s", "n", "s", "w", "e"
            )
        )
        assertEquals("30°33'59\"N, 60°33'59\"W", GeoLocation(30.5664, 60.5664).format())
        assertEquals("30°33'59\"S, 60°33'59\"E", GeoLocation(-30.5664, -60.5664).format())
    }

    @Test
    fun testGetGeoLocation() {
//        getFusedLocationProviderClient(targetContext).apply {
//            setMockMode(true)
//            setMockLocation(Location("flp").apply {
//                latitude = 1.0
//                longitude = 2.0
//            })
//            val request = CurrentLocationRequest.Builder()
//                .setDurationMillis(5000)
//                .build()
//            val l = runBlocking {
//                getCurrentLocation(request, null).await()
//            }
//            print(l)
//        }

        assertNotNull(targetContext.getGeoLocation())
    }

}