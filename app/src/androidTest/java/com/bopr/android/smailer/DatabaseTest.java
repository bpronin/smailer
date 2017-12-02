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
     *
     * @throws Exception when failed
     */
    @Test
    public void testDefaults() throws Exception {
        assertEquals(10000L, database.getCapacity());
        assertEquals(TimeUnit.DAYS.toMillis(7), database.getPurgePeriod());
    }

    /**
     * Check {@link Database#updateMessage(PhoneEvent)} and {@link Database#getMessages()} methods.
     *
     * @throws Exception when failed
     */
    @Test
    public void testAddGet() throws Exception {
        database.updateMessage(new PhoneEvent("1", true, 1000L, 0L, true, null, null, false, "Test 1"));
        database.updateMessage(new PhoneEvent("2", false, 2000L, 0L, false, null, null, false, null));
        database.updateMessage(new PhoneEvent("3", true, 3000L, 0L, false, null, null, false, null));
        database.updateMessage(new PhoneEvent("4", false, 4000L, 0L, false, null, null, false, null));
        database.updateMessage(new PhoneEvent("5", true, 5000L, 0L, true, null, null, false, null));
        database.updateMessage(new PhoneEvent("6", true, 6000L, 7000L, false, null, null, false, "Test 1"));
        database.updateMessage(new PhoneEvent("7", false, 7000L, 0L, false, null, null, false, "Test 2"));
        database.updateMessage(new PhoneEvent("8", true, 8000L, 0L, false, null, null, false, "Test 3"));
        database.updateMessage(new PhoneEvent("9", false, 9000L, 0L, false, null, null, false, "Test 4"));
        database.updateMessage(new PhoneEvent("10", true, 10000L, 20000L, false, "SMS text", new GeoCoordinates(10.5, 20.5), true, "Test 10"));

        List<PhoneEvent> items = database.getMessages().getAll();

        assertEquals(10, items.size());

        PhoneEvent message = items.get(0); /* descending order so it should be the last */
        assertNotNull(message.getId());
        assertEquals(true, message.isProcessed());
        assertEquals("10", message.getPhone());
        assertEquals(true, message.isIncoming());
        assertEquals(10000L, message.getStartTime().longValue());
        assertEquals(20000L, message.getEndTime().longValue());
        assertEquals(false, message.isMissed());
        assertEquals(true, message.isSms());
        assertEquals(10.5, message.getLocation().getLatitude());
        assertEquals(20.5, message.getLocation().getLongitude());
        assertEquals("SMS text", message.getText());
        assertEquals("Test 10", message.getDetails());
    }

    /**
     * Check {@link Database#updateMessage(PhoneEvent)} and {@link Database#getMessages()} methods.
     *
     * @throws Exception when failed
     */
    @Test
    public void testUpdateGet() throws Exception {
        PhoneEvent message = new PhoneEvent("1", true, 1000L, 2000L, false, "SMS text", new GeoCoordinates(10.5, 20.5), true, "Test 1");
        database.updateMessage(message);

        List<PhoneEvent> items = database.getMessages().getAll();
        assertEquals(1, items.size());

        message = items.get(0);
        assertTrue(message.getId() != -1);
        assertEquals(true, message.isProcessed());
        assertEquals("1", message.getPhone());
        assertEquals(true, message.isIncoming());
        assertEquals(1000L, message.getStartTime().longValue());
        assertEquals(2000L, message.getEndTime().longValue());
        assertEquals(false, message.isMissed());
        assertEquals(true, message.isSms());
        assertEquals(10.5, message.getLocation().getLatitude());
        assertEquals(20.5, message.getLocation().getLongitude());
        assertEquals("SMS text", message.getText());
        assertEquals("Test 1", message.getDetails());

        message.setProcessed(false);
        message.setPhone("2");
        message.setIncoming(false);
        message.setStartTime(2000L);
        message.setEndTime(3000L);
        message.setMissed(true);
        message.setLocation(new GeoCoordinates(11.5, 21.5));
        message.setText("New text");
        message.setDetails("New details");
        database.updateMessage(message);

        items = database.getMessages().getAll();
        assertEquals(1, items.size());

        message = items.get(0);
        assertTrue(message.getId() != -1);
        assertEquals(false, message.isProcessed());
        assertEquals("2", message.getPhone());
        assertEquals(false, message.isIncoming());
        assertEquals(2000L, message.getStartTime().longValue());
        assertEquals(3000L, message.getEndTime().longValue());
        assertEquals(true, message.isMissed());
        assertEquals(false, message.isSms());
        assertEquals(11.5, message.getLocation().getLatitude());
        assertEquals(21.5, message.getLocation().getLongitude());
        assertEquals("New text", message.getText());
        assertEquals("New details", message.getDetails());
    }

    /**
     * Check {@link Database#clearMessages()}  method.
     *
     * @throws Exception when failed
     */
    @Test
    public void testClear() throws Exception {
        database.updateMessage(new PhoneEvent("1", true, 1000L, 2000L, false, "SMS text", new GeoCoordinates(10.5, 20.5), true, "Test 1"));
        database.updateMessage(new PhoneEvent("2", false, 2000L, 0L, false, null, null, false, null));
        database.updateMessage(new PhoneEvent("3", true, 3000L, 0L, false, null, null, false, null));
        database.updateMessage(new PhoneEvent("4", false, 4000L, 0L, false, null, null, false, null));
        database.updateMessage(new PhoneEvent("5", true, 5000L, 0L, true, null, null, false, null));
        database.updateMessage(new PhoneEvent("6", true, 6000L, 7000L, false, null, null, false, "Test 1"));
        database.updateMessage(new PhoneEvent("7", false, 7000L, 0L, false, null, null, false, "Test 2"));
        database.updateMessage(new PhoneEvent("8", true, 8000L, 0L, false, null, null, false, "Test 3"));
        database.updateMessage(new PhoneEvent("9", false, 9000L, 0L, false, null, null, false, "Test 4"));
        database.updateMessage(new PhoneEvent("10", true, 10000L, 0L, true, null, null, false, "Test 5"));

        assertEquals(10, database.getMessages().getCount());

        database.clearMessages();

        assertEquals(0, database.getMessages().getCount());
    }

    /**
     * Check {@link Database#purge()}  method..
     *
     * @throws Exception when failed
     */
    @Test
    public void testPurge() throws Exception {
        database.updateMessage(new PhoneEvent("1", true, 1000L, 2000L, false, "SMS text", new GeoCoordinates(10.5, 20.5), true, "Test 1"));
        database.updateMessage(new PhoneEvent("2", false, 2000L, 0L, false, null, null, false, null));
        database.updateMessage(new PhoneEvent("3", true, 3000L, 0L, false, null, null, false, null));
        database.updateMessage(new PhoneEvent("4", false, 4000L, 0L, false, null, null, false, null));
        database.updateMessage(new PhoneEvent("5", true, 5000L, 0L, true, null, null, false, null));
        database.updateMessage(new PhoneEvent("6", true, 6000L, 7000L, false, null, null, false, "Test 1"));
        database.updateMessage(new PhoneEvent("7", false, 7000L, 0L, false, null, null, false, "Test 2"));
        database.updateMessage(new PhoneEvent("8", true, 8000L, 0L, false, null, null, false, "Test 3"));
        database.updateMessage(new PhoneEvent("9", false, 9000L, 0L, false, null, null, false, "Test 4"));
        database.updateMessage(new PhoneEvent("10", true, 10000L, 0L, true, null, null, false, "Test 5"));

        /* first we have 9 records */
        assertEquals(10, database.getMessages().getCount());

        /* change default capacity and period to small values */
        database.setCapacity(5);
        database.setPurgePeriod(500);

        /* sleep a time that is less than purge period */
        Thread.sleep(100);

        database.purge();

        /* nothing happens cause elapsed time is less than purge period */
        assertEquals(10, database.getMessages().getCount());

        /* sleep again */
        Thread.sleep(500);

        database.purge();

        /* last addition should trigger purge process cause total elapsed time exceeds purge period */
        assertEquals(5, database.getMessages().getCount());
    }

    /**
     * Check {@link Database#getLastLocation()} and {@link Database#saveLastLocation(GeoCoordinates)} methods.
     *
     * @throws Exception when failed
     */
    @Test
    public void testSaveLoadLocation() throws Exception {
        GeoCoordinates coordinates = new GeoCoordinates(30, 60);
        database.saveLastLocation(coordinates);

        GeoCoordinates actual = database.getLastLocation();

        assertEquals(coordinates.getLatitude(), actual.getLatitude());
        assertEquals(coordinates.getLongitude(), actual.getLongitude());
    }

    /**
     * Check {@link Database#getUnsentMessages()}} method.
     *
     * @throws Exception when failed
     */
    @Test
    public void testGetUnsentMessages() throws Exception {
        database.updateMessage(new PhoneEvent("1", true, 1000L, 0L, true, null, null, true, "Test 1"));
        database.updateMessage(new PhoneEvent("2", false, 2000L, 0L, false, null, null, true, null));
        database.updateMessage(new PhoneEvent("3", true, 3000L, 0L, false, null, null, true, null));
        database.updateMessage(new PhoneEvent("4", false, 4000L, 0L, false, null, null, true, null));
        database.updateMessage(new PhoneEvent("5", true, 5000L, 0L, true, null, null, true, null));
        database.updateMessage(new PhoneEvent("6", true, 6000L, 7000L, false, null, null, false, "Test 1"));
        database.updateMessage(new PhoneEvent("7", false, 7000L, 0L, false, null, null, false, "Test 2"));
        database.updateMessage(new PhoneEvent("8", true, 8000L, 0L, false, null, null, false, "Test 3"));
        database.updateMessage(new PhoneEvent("9", false, 9000L, 0L, false, null, null, false, "Test 4"));
        database.updateMessage(new PhoneEvent("10", true, 10000L, 20000L, false, null, null, false, "Test 10"));

        List<PhoneEvent> items = database.getUnsentMessages().getAll();

        assertEquals(5, items.size());

        PhoneEvent message = items.get(0); /* descending order so it should be the last */
        assertNotNull(message.getId());
        assertEquals("Test 10", message.getDetails());
    }

}