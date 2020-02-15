package com.bopr.android.smailer

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import androidx.test.rule.GrantPermissionRule
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito

/**
 * [GeoLocator] class tester.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
//todo: implement
class GeoLocatorTest : BaseTest() {

    @Rule
    @JvmField
    val permissionRule = GrantPermissionRule.grant(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION)

    private var databaseLocation: GeoCoordinates? = null

    @Before
    @Throws(Exception::class)
    fun setUp() {
        databaseLocation = GeoCoordinates(50.0, 60.0)
    }

    private fun createMockDatabase(): Database {
        val database = Mockito.mock(Database::class.java)
        Mockito.`when`(database.lastLocation).thenReturn(databaseLocation)
        return database
    }

    @Test
    fun testGetLocationGpsOn() {
        val provider = GeoLocator(targetContext, createMockDatabase())
        val location = provider.getLocation()
        Assert.assertNotNull(location)
    }

    //    private Location location(GeoCoordinates coordinates) {
//        Location location = new Location("");
//        location.setLatitude(coordinates.getLatitude());
//        location.setLongitude(coordinates.getLongitude());
//        return location;
//    }
//    private LocationManager createMockManager(boolean gpsEnabled, boolean networkEnabled,
//                                              boolean passiveEnabled) {
//        LocationManager manager = mock(LocationManager.class);
//
//        when(manager.isProviderEnabled(GPS_PROVIDER)).thenReturn(gpsEnabled);
//        when(manager.isProviderEnabled(NETWORK_PROVIDER)).thenReturn(networkEnabled);
//        when(manager.isProviderEnabled(PASSIVE_PROVIDER)).thenReturn(passiveEnabled);
//
//        when(manager.getLastKnownLocation(PASSIVE_PROVIDER)).thenReturn(location(lastPassiveLocation));
//
//        doAnswer(
//                new Answer() {
//                    @Override
//                    public Object answer(InvocationOnMock invocation) {
//                        String provider = (String) invocation.getArguments()[0];
//                        LocationListener listener = (LocationListener) invocation.getArguments()[1];
//                        switch (provider) {
//                            case GPS_PROVIDER:
//                                listener.onLocationChanged(location(gpsLocation));
//                                break;
//                            case NETWORK_PROVIDER:
//                                listener.onLocationChanged(location(networkLocation));
//                                break;
//                        }
//                        return null;
//                    }
//                })
//                .when(manager)
//                .requestSingleUpdate(
//                        anyString(),
//                        any(LocationListener.class),
//                        any(Looper.class));
//
//        return manager;
//    }
//
//
//
//    @Test
//    public void testGetLocationGpsOn() {
//        GeoLocator provider = new GeoLocator(context, createMockDatabase());
//        provider.setLocationManager(createMockManager(true, true, true));
//
//        assertEquals(gpsLocation, provider.getLocation(1000));
//    }
//
//    @Test
//    public void testGetLocationNetworkOn() {
//        GeoLocator provider = new GeoLocator(context, createMockDatabase());
//        provider.setLocationManager(createMockManager(false, true, true));
//
//        assertEquals(networkLocation, provider.getLocation(1000));
//    }
//
//    @Test
//    public void testGetLocationPassiveOn() {
//        GeoLocator provider = new GeoLocator(context, createMockDatabase());
//        provider.setLocationManager(createMockManager(false, false, true));
//
//        assertEquals(lastPassiveLocation, provider.getLocation(1000));
//    }
//
//    @Test
//    public void testGetLocationAllOff() {
//        GeoLocator provider = new GeoLocator(context, createMockDatabase());
//        provider.setLocationManager(createMockManager(false, false, false));
//
//        assertEquals(databaseLocation, provider.getLocation(1000));
//    }
}