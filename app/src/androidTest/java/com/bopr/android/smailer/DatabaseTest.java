package com.bopr.android.smailer;

import org.junit.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * {@link Database} class tester.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class DatabaseTest extends BaseTest {

    private Database database;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        database = new Database(getContext(), "test.sqlite");
        database.destroy();
    }

    /**
     * Check default properties.
     */
    @Test
    public void testDefaults() {
        assertEquals(10000L, database.getCapacity());
        assertEquals(TimeUnit.DAYS.toMillis(7), database.getPurgePeriod());
    }

    /**
     * Check {@link Database#putEvent(PhoneEvent)} and {@link Database#getEvents()} methods.
     */
    @Test
    public void testAddGet() {
        database.putEvent(new PhoneEvent("1", true, 1000L, 0L, true, null, null, "Test 1", PhoneEvent.STATE_PENDING));
        database.putEvent(new PhoneEvent("2", false, 2000L, 0L, false, null, null, null, PhoneEvent.STATE_PENDING));
        database.putEvent(new PhoneEvent("3", true, 3000L, 0L, false, null, null, null, PhoneEvent.STATE_PENDING));
        database.putEvent(new PhoneEvent("4", false, 4000L, 0L, false, null, null, null, PhoneEvent.STATE_PENDING));
        database.putEvent(new PhoneEvent("5", true, 5000L, 0L, true, null, null, null, PhoneEvent.STATE_PENDING));
        database.putEvent(new PhoneEvent("6", true, 6000L, 7000L, false, null, null, "Test 1", PhoneEvent.STATE_PENDING));
        database.putEvent(new PhoneEvent("7", false, 7000L, 0L, false, null, null, "Test 2", PhoneEvent.STATE_PENDING));
        database.putEvent(new PhoneEvent("8", true, 8000L, 0L, false, null, null, "Test 3", PhoneEvent.STATE_PENDING));
        database.putEvent(new PhoneEvent("9", false, 9000L, 0L, false, null, null, "Test 4", PhoneEvent.STATE_PENDING));
        database.putEvent(new PhoneEvent("10", true, 10000L, 20000L, false, "SMS text", new GeoCoordinates(10.5, 20.5), "Test 10", PhoneEvent.STATE_PENDING));

        List<PhoneEvent> items = database.getEvents().toList();

        assertEquals(10, items.size());

        PhoneEvent message = items.get(0); /* descending order so it should be the last */
        assertEquals(PhoneEvent.STATE_PENDING, message.getState());
        assertEquals("10", message.getPhone());
        assertTrue(message.isIncoming());
        assertEquals(10000L, message.getStartTime());
        assertEquals(20000L, message.getEndTime().longValue());
        assertFalse(message.isMissed());
        assertTrue(message.isSms());
        assertEquals(10.5, message.getLocation().getLatitude(), 0.1);
        assertEquals(20.5, message.getLocation().getLongitude(), 0.1);
        assertEquals("SMS text", message.getText());
        assertEquals("Test 10", message.getDetails());
    }

    /**
     * Check {@link Database#putEvent(PhoneEvent)} and {@link Database#getEvents()} methods.
     */
    @Test
    public void testUpdateGet() {
        PhoneEvent message = new PhoneEvent("1", true, 1000L, 2000L, false, "SMS text", new GeoCoordinates(10.5, 20.5), "Test 1", PhoneEvent.STATE_PENDING);
        database.putEvent(message);

        List<PhoneEvent> items = database.getEvents().toList();
        assertEquals(1, items.size());

        message = items.get(0);
        assertEquals(PhoneEvent.STATE_PENDING, message.getState());
        assertEquals("1", message.getPhone());
        assertTrue(message.isIncoming());
        assertEquals(1000L, message.getStartTime());
        assertEquals(2000L, message.getEndTime().longValue());
        assertFalse(message.isMissed());
        assertTrue(message.isSms());
        assertEquals(10.5, message.getLocation().getLatitude(), 0.1);
        assertEquals(20.5, message.getLocation().getLongitude(), 0.1);
        assertEquals("SMS text", message.getText());
        assertEquals("Test 1", message.getDetails());

        assertEquals(PhoneEvent.STATE_PENDING, message.getState());
        message.setPhone("2");
        message.setIncoming(false);
        message.setStartTime(2000L);
        message.setEndTime(3000L);
        message.setMissed(true);
        message.setLocation(new GeoCoordinates(11.5, 21.5));
        message.setText("New text");
        message.setDetails("New details");
        database.putEvent(message);

        items = database.getEvents().toList();
        assertEquals(1, items.size());

        message = items.get(0);
        assertEquals(PhoneEvent.STATE_PENDING, message.getState());
        assertEquals("2", message.getPhone());
        assertFalse(message.isIncoming());
        assertEquals(2000L, message.getStartTime());
        assertEquals(3000L, message.getEndTime().longValue());
        assertTrue(message.isMissed());
        assertFalse(message.isSms());
        assertEquals(11.5, message.getLocation().getLatitude(), 0.1);
        assertEquals(21.5, message.getLocation().getLongitude(), 0.1);
        assertEquals("New text", message.getText());
        assertEquals("New details", message.getDetails());
    }

    /**
     * Check {@link Database#clearEvents()}  method.
     */
    @Test
    public void testClear() {
        database.putEvent(new PhoneEvent("1", true, 1000L, 2000L, false, "SMS text", new GeoCoordinates(10.5, 20.5), "Test 1", PhoneEvent.STATE_PENDING));
        database.putEvent(new PhoneEvent("2", false, 2000L, 0L, false, null, null, null, PhoneEvent.STATE_PENDING));
        database.putEvent(new PhoneEvent("3", true, 3000L, 0L, false, null, null, null, PhoneEvent.STATE_PENDING));
        database.putEvent(new PhoneEvent("4", false, 4000L, 0L, false, null, null, null, PhoneEvent.STATE_PENDING));
        database.putEvent(new PhoneEvent("5", true, 5000L, 0L, true, null, null, null, PhoneEvent.STATE_PENDING));
        database.putEvent(new PhoneEvent("6", true, 6000L, 7000L, false, null, null, "Test 1", PhoneEvent.STATE_PENDING));
        database.putEvent(new PhoneEvent("7", false, 7000L, 0L, false, null, null, "Test 2", PhoneEvent.STATE_PENDING));
        database.putEvent(new PhoneEvent("8", true, 8000L, 0L, false, null, null, "Test 3", PhoneEvent.STATE_PENDING));
        database.putEvent(new PhoneEvent("9", false, 9000L, 0L, false, null, null, "Test 4", PhoneEvent.STATE_PENDING));
        database.putEvent(new PhoneEvent("10", true, 10000L, 0L, true, null, null, "Test 5", PhoneEvent.STATE_PENDING));

        assertEquals(10, database.getEvents().getCount());

        database.clearEvents();

        assertEquals(0, database.getEvents().getCount());
    }

    /**
     * Check {@link Database#purge()}  method..
     */
    @Test
    public void testPurge() throws InterruptedException {
        database.putEvent(new PhoneEvent("1", true, 1000L, 2000L, false, "SMS text", new GeoCoordinates(10.5, 20.5), "Test 1", PhoneEvent.STATE_PENDING));
        database.putEvent(new PhoneEvent("2", false, 2000L, 0L, false, null, null, null, PhoneEvent.STATE_PENDING));
        database.putEvent(new PhoneEvent("3", true, 3000L, 0L, false, null, null, null, PhoneEvent.STATE_PENDING));
        database.putEvent(new PhoneEvent("4", false, 4000L, 0L, false, null, null, null, PhoneEvent.STATE_PENDING));
        database.putEvent(new PhoneEvent("5", true, 5000L, 0L, true, null, null, null, PhoneEvent.STATE_PENDING));
        database.putEvent(new PhoneEvent("6", true, 6000L, 7000L, false, null, null, "Test 1", PhoneEvent.STATE_PENDING));
        database.putEvent(new PhoneEvent("7", false, 7000L, 0L, false, null, null, "Test 2", PhoneEvent.STATE_PENDING));
        database.putEvent(new PhoneEvent("8", true, 8000L, 0L, false, null, null, "Test 3", PhoneEvent.STATE_PENDING));
        database.putEvent(new PhoneEvent("9", false, 9000L, 0L, false, null, null, "Test 4", PhoneEvent.STATE_PENDING));
        database.putEvent(new PhoneEvent("10", true, 10000L, 0L, true, null, null, "Test 5", PhoneEvent.STATE_PENDING));

        /* first we have 9 records */
        assertEquals(10, database.getEvents().getCount());

        /* change default capacity and period to small values */
        database.setCapacity(5);
        database.setPurgePeriod(500);

        /* sleep a time that is less than purge period */
        Thread.sleep(100);

        database.purge();

        /* nothing happens cause elapsed time is less than purge period */
        assertEquals(10, database.getEvents().getCount());

        /* sleep again */
        Thread.sleep(500);

        database.purge();

        /* last addition should trigger purge process cause total elapsed time exceeds purge period */
        assertEquals(5, database.getEvents().getCount());
    }

    /**
     * Check {@link Database#getLastLocation()} and {@link Database#saveLastLocation(GeoCoordinates)} methods.
     */
    @Test
    public void testSaveLoadLocation() {
        GeoCoordinates coordinates = new GeoCoordinates(30, 60);
        database.saveLastLocation(coordinates);

        GeoCoordinates actual = database.getLastLocation();

        assertEquals(coordinates.getLatitude(), actual.getLatitude(), 0.1);
        assertEquals(coordinates.getLongitude(), actual.getLongitude(), 0.1);
    }

    /**
     * Check {@link Database#getPendingEvents()}} method.
     */
    @Test
    public void testGetUnsentMessages() {
        database.putEvent(new PhoneEvent("1", true, 1000L, 0L, true, null, null, "Test 1", PhoneEvent.STATE_PENDING));
        database.putEvent(new PhoneEvent("2", false, 2000L, 0L, false, null, null, null, PhoneEvent.STATE_PENDING));
        database.putEvent(new PhoneEvent("3", true, 3000L, 0L, false, null, null, null, PhoneEvent.STATE_PENDING));
        database.putEvent(new PhoneEvent("4", false, 4000L, 0L, false, null, null, null, PhoneEvent.STATE_PENDING));
        database.putEvent(new PhoneEvent("5", true, 5000L, 0L, true, null, null, null, PhoneEvent.STATE_PENDING));
        database.putEvent(new PhoneEvent("6", true, 6000L, 7000L, false, null, null, "Test 1", PhoneEvent.STATE_PENDING));
        database.putEvent(new PhoneEvent("7", false, 7000L, 0L, false, null, null, "Test 2", PhoneEvent.STATE_PENDING));
        database.putEvent(new PhoneEvent("8", true, 8000L, 0L, false, null, null, "Test 3", PhoneEvent.STATE_PENDING));
        database.putEvent(new PhoneEvent("9", false, 9000L, 0L, false, null, null, "Test 4", PhoneEvent.STATE_PENDING));
        database.putEvent(new PhoneEvent("10", true, 10000L, 20000L, false, null, null, "Test 10", PhoneEvent.STATE_PENDING));

        List<PhoneEvent> items = database.getPendingEvents().toList();

        assertEquals(5, items.size());

        PhoneEvent message = items.get(0); /* descending order so it should be the last */
        assertEquals("Test 10", message.getDetails());
    }

}