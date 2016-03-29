package com.bopr.android.smailer;

import android.location.LocationManager;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link LocationProvider} class tester.
 * Note: to run this test set device - Dev Settings ->  Allow mock locations
 * and Geolocation should be ON
 */
public class LocationProviderTest extends BaseTest {

    private LocationManager locationManager;
    private Database database;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        database = mock(Database.class);
        when(database.getLastLocation()).thenReturn(new GeoCoordinates(10, 20));

//        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
//        locationManager.addTestProvider(GPS_PROVIDER, false, false, false, false, true, true, true, 0, 5);
//        locationManager.addTestProvider(NETWORK_PROVIDER, false, false, false, false, true, true, true, 0, 5);
    }

    @Override
    protected void tearDown() throws Exception {
//        locationManager.removeTestProvider(GPS_PROVIDER);
//        locationManager.removeTestProvider(NETWORK_PROVIDER);
        super.tearDown();
    }

//    @NonNull
//    private Location createLocation(String networkProvider, int latitude, int longitude,
//                                    int accuracy, int time, int nanos) {
//        Location location = new Location(networkProvider);
//        location.setLatitude(latitude);
//        location.setLongitude(longitude);
//        location.setAccuracy(accuracy);
//        location.setTime(time);
//        location.setElapsedRealtimeNanos(nanos);
//        return location;
//    }

//    public void testGetLocationGps() throws Exception {
//        locationManager.setTestProviderLocation(GPS_PROVIDER, createLocation(GPS_PROVIDER, 30, 60, 100, 200, 300));
//        locationManager.setTestProviderLocation(NETWORK_PROVIDER, createLocation(NETWORK_PROVIDER, 20, 70, 200, 300, 400));
//        locationManager.setTestProviderEnabled(GPS_PROVIDER, true);
//        locationManager.setTestProviderEnabled(NETWORK_PROVIDER, true);
//
//        LocationProvider provider = new LocationProvider(getContext(), database);
//        GeoCoordinates location = provider.getLocation();
//
//        assertEquals(new GeoCoordinates(30, 60), location);
//    }
//
//    public void testGetLocationNetwork() throws Exception {
//        locationManager.setTestProviderLocation(GPS_PROVIDER, createLocation(GPS_PROVIDER, 30, 60, 100, 200, 300));
//        locationManager.setTestProviderLocation(NETWORK_PROVIDER, createLocation(NETWORK_PROVIDER, 20, 70, 200, 300, 400));
//        locationManager.setTestProviderEnabled(GPS_PROVIDER, false);
//        locationManager.setTestProviderEnabled(NETWORK_PROVIDER, true);
//
//        LocationProvider provider = new LocationProvider(getContext(), database);
//        GeoCoordinates location = provider.getLocation();
//
//        assertEquals(new GeoCoordinates(20, 70), location);
//    }
//
//    public void testGetLocationPassive() throws Exception {
//
//        locationManager.setTestProviderLocation(GPS_PROVIDER, createLocation(GPS_PROVIDER, 30, 60, 100, 200, 300));
//        locationManager.setTestProviderLocation(NETWORK_PROVIDER, createLocation(NETWORK_PROVIDER, 20, 70, 200, 300, 400)); /* this should be the result */
//        locationManager.setTestProviderEnabled(NETWORK_PROVIDER, false);
//        locationManager.setTestProviderEnabled(GPS_PROVIDER, false);
//
//        LocationProvider provider = new LocationProvider(getContext(), database);
//        GeoCoordinates location = provider.getLocation();
//
//        assertEquals(new GeoCoordinates(20, 70), location);
//    }

    public void testBlockingRequest() throws Exception{
        LocationProvider provider = new LocationProvider(getContext(), database);
        GeoCoordinates coordinates = provider.getLocation(1000);

        assertNotNull(coordinates);
    }
}

