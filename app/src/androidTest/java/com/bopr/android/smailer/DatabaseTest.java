package com.bopr.android.smailer;

import android.app.Application;
import android.support.annotation.NonNull;
import android.test.ApplicationTestCase;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * {@link Database} class tester.
 */
public class DatabaseTest extends ApplicationTestCase<Application> {

    private Database database;

    public DatabaseTest() {
        super(Application.class);
        Locale.setDefault(Locale.US);
    }

    @NonNull
    private List<MailMessage> asList(Database.MailMessageCursor cursor) {
        List<MailMessage> items = new ArrayList<>();

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            items.add(cursor.get());
            cursor.moveToNext();
        }
        cursor.close();

        return items;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        database = new Database(getContext());
        database.destroy();
    }

    /**
     * Check default properties.
     *
     * @throws Exception when failed
     */
    public void testDefaults() throws Exception {
        assertEquals(10000, database.getCapacity());
        assertEquals(TimeUnit.DAYS.toMillis(7), database.getPurgePeriod());
    }

    /**
     * Check {@link Database#addMessage(MailMessage, Throwable)} and {@link Database#getMessages()} methods.
     *
     * @throws Exception when failed
     */
    public void testAddGet() throws Exception {
        database.addMessage(new MailMessage("1", true, 1000, 2000, false, true, "SMS text", 10.5, 20.5, true), new Exception("Test 1"));
        database.addMessage(new MailMessage("2", false, 2000, 0, false, true, null, 0, 0, false), null);
        database.addMessage(new MailMessage("3", true, 3000, 0, false, false, null, 0, 0, false), null);
        database.addMessage(new MailMessage("4", false, 4000, 0, false, false, null, 0, 0, false), null);
        database.addMessage(new MailMessage("5", true, 5000, 0, true, false, null, 0, 0, false), null);
        database.addMessage(new MailMessage("6", true, 6000, 7000, false, true, null, 0, 0, false), new Exception("Test 1"));
        database.addMessage(new MailMessage("7", false, 7000, 0, false, true, null, 0, 0, false), new Exception("Test 2"));
        database.addMessage(new MailMessage("8", true, 8000, 0, false, false, null, 0, 0, false), new Exception("Test 3"));
        database.addMessage(new MailMessage("9", false, 9000, 0, false, false, null, 0, 0, false), new Exception("Test 4"));
        database.addMessage(new MailMessage("10", true, 10000, 0, true, false, null, 0, 0, false), new Exception("Test 5"));

        List<MailMessage> items = asList(database.getMessages());

        assertEquals(10, items.size());

        MailMessage message = items.get(0);
        assertTrue(message.getId() != -1);
        assertEquals(true, message.isSent());
        assertEquals("1", message.getPhone());
        assertEquals(true, message.isIncoming());
        assertEquals(1000, message.getStartTime());
        assertEquals(2000, message.getEndTime());
        assertEquals(false, message.isMissed());
        assertEquals(true, message.isSms());
        assertEquals(10.5, message.getLatitude());
        assertEquals(20.5, message.getLongitude());

        assertEquals(null, message.getText()); /* to read text need to call database.getMessageText() */
        message.setText(database.getMessageText(message.getId()));
        assertEquals("SMS text", message.getText());

        assertEquals("java.lang.Exception: Test 1", database.getMessageDetails(message.getId()));

        assertNull(database.getMessageDetails(items.get(1).getId()));
    }

    /**
     * Check {@link Database#clear()}  method.
     *
     * @throws Exception when failed
     */
    public void testClear() throws Exception {
        database.addMessage(new MailMessage("+79052345671", true, 1000, 2000, false, true, "SMS text", 10.5, 20.5, true), new Exception("Test 1"));
        database.addMessage(new MailMessage("+79052345672", false, 2000, 0, false, true, null, 0, 0, false), null);
        database.addMessage(new MailMessage("+79052345673", true, 3000, 0, false, false, null, 0, 0, false), null);
        database.addMessage(new MailMessage("+79052345674", false, 4000, 0, false, false, null, 0, 0, false), null);
        database.addMessage(new MailMessage("+79052345675", true, 5000, 0, true, false, null, 0, 0, false), null);
        database.addMessage(new MailMessage("+79052345671", true, 6000, 7000, false, true, null, 0, 0, false), new Exception("Test 1"));
        database.addMessage(new MailMessage("+79052345672", false, 7000, 0, false, true, null, 0, 0, false), new Exception("Test 2"));
        database.addMessage(new MailMessage("+79052345673", true, 8000, 0, false, false, null, 0, 0, false), new Exception("Test 3"));
        database.addMessage(new MailMessage("+79052345674", false, 9000, 0, false, false, null, 0, 0, false), new Exception("Test 4"));
        database.addMessage(new MailMessage("+79052345675", true, 10000, 0, true, false, null, 0, 0, false), new Exception("Test 5"));

        assertEquals(10, database.getMessages().getCount());

        database.clear();

        assertEquals(0, database.getMessages().getCount());
    }

    /**
     * Check {@link Database#purge()}  method..
     *
     * @throws Exception when failed
     */
    public void testPurge() throws Exception {
        database.addMessage(new MailMessage("+79052345671", true, 1000, 2000, false, true, "SMS text", 10.5, 20.5, true), new Exception("Test 1"));
        database.addMessage(new MailMessage("+79052345672", false, 2000, 0, false, true, null, 0, 0, false), null);
        database.addMessage(new MailMessage("+79052345673", true, 3000, 0, false, false, null, 0, 0, false), null);
        database.addMessage(new MailMessage("+79052345674", false, 4000, 0, false, false, null, 0, 0, false), null);
        database.addMessage(new MailMessage("+79052345675", true, 5000, 0, true, false, null, 0, 0, false), null);
        database.addMessage(new MailMessage("+79052345671", true, 6000, 7000, false, true, null, 0, 0, false), new Exception("Test 1"));
        database.addMessage(new MailMessage("+79052345672", false, 7000, 0, false, true, null, 0, 0, false), new Exception("Test 2"));
        database.addMessage(new MailMessage("+79052345673", true, 8000, 0, false, false, null, 0, 0, false), new Exception("Test 3"));
        database.addMessage(new MailMessage("+79052345674", false, 9000, 0, false, false, null, 0, 0, false), new Exception("Test 4"));
        database.addMessage(new MailMessage("+79052345674", false, 9000, 0, false, false, null, 0, 0, false), new Exception("Test 4"));

        /* first we have 9 records */
        assertEquals(10, database.getMessages().getCount());

        /* change default capacity and period to small values */
        database.setCapacity(5);
        database.setPurgePeriod(100);

        /* sleep a time that is less than purge period */
        Thread.sleep(10);

        database.purge();

        /* nothing happen cause elapsed time is less than purge period */
        assertEquals(10, database.getMessages().getCount());

        /* sleep again */
        Thread.sleep(90);

        database.purge();

        /* last addition should trigger purge process cause total elapsed time exceeds purge period */
        assertEquals(5, database.getMessages().getCount());

//        List<MailMessage> items = asList(database.getMessages());
//        assertEquals("Incoming SMS from 11. Send success.", items.get(0).getMessage());
//        assertEquals("Missed call from 10. Send failed.", items.get(1).getMessage());
//        assertEquals("Outgoing call to 9. Send failed.", items.get(2).getMessage());
//        assertEquals("Incoming call from 8. Send failed.", items.get(3).getMessage());
//        assertEquals("Outgoing SMS to 7. Send failed.", items.get(4).getMessage());
    }

}