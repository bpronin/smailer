package com.bopr.android.smailer.util

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import androidx.test.rule.GrantPermissionRule
import com.bopr.android.smailer.BaseTest
import com.bopr.android.smailer.data.Database
import com.bopr.android.smailer.data.Database.Companion.databaseName
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * [GeoLocator] class tester.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class GeoLocatorTest : BaseTest() {

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(ACCESS_COARSE_LOCATION,
            ACCESS_FINE_LOCATION)

    private lateinit var database: Database

    @Before
    fun setUp() {
        databaseName = "test.sqlite"
        targetContext.deleteDatabase(databaseName)
        database = Database(targetContext).apply {
            lastLocation = GeoCoordinates(50.0, 60.0)
        }
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testGetLocationGpsOn() {
        val locator = GeoLocator(targetContext, database)
        val location = locator.getLocation()
        assertNotNull(location)
    }

}