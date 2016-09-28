package com.bopr.android.smailer;

import android.annotation.SuppressLint;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Looper;
import android.support.annotation.NonNull;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;
import static android.location.LocationManager.PASSIVE_PROVIDER;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link Locator} class tester.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
@SuppressWarnings("ResourceType")
@SuppressLint("MissingPermission")
public class LocatorTest extends BaseTest {

    private GeoCoordinates lastPassiveLocation;
    private GeoCoordinates gpsLocation;
    private GeoCoordinates networkLocation;
    private GeoCoordinates databaseLocation;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        gpsLocation = new GeoCoordinates(10, 20);
        networkLocation = new GeoCoordinates(20, 30);
        lastPassiveLocation = new GeoCoordinates(30, 40);
        databaseLocation = new GeoCoordinates(50, 60);
    }

    private Location location(GeoCoordinates coordinates) {
        Location location = new Location("");
        location.setLatitude(coordinates.getLatitude());
        location.setLongitude(coordinates.getLongitude());
        return location;
    }

    private LocationManager createMockManager(boolean gpsEnabled, boolean networkEnabled,
                                              boolean passiveEnabled) {
        LocationManager manager = mock(LocationManager.class);

        when(manager.isProviderEnabled(GPS_PROVIDER)).thenReturn(gpsEnabled);
        when(manager.isProviderEnabled(NETWORK_PROVIDER)).thenReturn(networkEnabled);
        when(manager.isProviderEnabled(PASSIVE_PROVIDER)).thenReturn(passiveEnabled);

        when(manager.getLastKnownLocation(PASSIVE_PROVIDER)).thenReturn(location(lastPassiveLocation));

        doAnswer(
                new Answer() {
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        String provider = invocation.getArgumentAt(0, String.class);
                        LocationListener listener = invocation.getArgumentAt(1, LocationListener.class);
                        switch (provider) {
                            case GPS_PROVIDER:
                                listener.onLocationChanged(location(gpsLocation));
                                break;
                            case NETWORK_PROVIDER:
                                listener.onLocationChanged(location(networkLocation));
                                break;
                        }
                        return null;
                    }
                })
                .when(manager)
                .requestSingleUpdate(
                        anyString(),
                        any(LocationListener.class),
                        any(Looper.class));

        return manager;
    }

    @NonNull
    private Database createMockDatabase() {
        Database database = mock(Database.class);
        when(database.getLastLocation()).thenReturn(databaseLocation);
        return database;
    }

    public void testGetLocationGpsOn() throws Exception {
        Locator provider = new Locator(getContext(), createMockDatabase());
        provider.setLocationManager(createMockManager(true, true, true));

        assertEquals(gpsLocation, provider.getLocation(1000));
    }

    public void testGetLocationNetworkOn() throws Exception {
        Locator provider = new Locator(getContext(), createMockDatabase());
        provider.setLocationManager(createMockManager(false, true, true));

        assertEquals(networkLocation, provider.getLocation(1000));
    }

    public void testGetLocationPassiveOn() throws Exception {
        Locator provider = new Locator(getContext(), createMockDatabase());
        provider.setLocationManager(createMockManager(false, false, true));

        assertEquals(lastPassiveLocation, provider.getLocation(1000));
    }

    public void testGetLocationAllOff() throws Exception {
        Locator provider = new Locator(getContext(), createMockDatabase());
        provider.setLocationManager(createMockManager(false, false, false));

        assertEquals(databaseLocation, provider.getLocation(1000));
    }

}

