package com.bopr.android.smailer;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Looper;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import androidx.annotation.NonNull;
import androidx.test.rule.GrantPermissionRule;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;
import static android.location.LocationManager.PASSIVE_PROVIDER;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link GeoLocator} class tester.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class GeoLocatorTest extends BaseTest {

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION);

    private GeoCoordinates lastPassiveLocation;
    private GeoCoordinates gpsLocation;
    private GeoCoordinates networkLocation;
    private GeoCoordinates databaseLocation;
    private Context context;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        context = getContext();

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
                    public Object answer(InvocationOnMock invocation) {
                        String provider = (String) invocation.getArguments()[0];
                        LocationListener listener = (LocationListener) invocation.getArguments()[1];
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

    @Test
    public void testGetLocationGpsOn() {
        GeoLocator provider = new GeoLocator(context, createMockDatabase());
        provider.setLocationManager(createMockManager(true, true, true));

        assertEquals(gpsLocation, provider.getLocation(1000));
    }

    @Test
    public void testGetLocationNetworkOn() {
        GeoLocator provider = new GeoLocator(context, createMockDatabase());
        provider.setLocationManager(createMockManager(false, true, true));

        assertEquals(networkLocation, provider.getLocation(1000));
    }

    @Test
    public void testGetLocationPassiveOn() {
        GeoLocator provider = new GeoLocator(context, createMockDatabase());
        provider.setLocationManager(createMockManager(false, false, true));

        assertEquals(lastPassiveLocation, provider.getLocation(1000));
    }

    @Test
    public void testGetLocationAllOff() {
        GeoLocator provider = new GeoLocator(context, createMockDatabase());
        provider.setLocationManager(createMockManager(false, false, false));

        assertEquals(databaseLocation, provider.getLocation(1000));
    }

}

