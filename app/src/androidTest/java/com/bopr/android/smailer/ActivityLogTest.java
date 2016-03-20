package com.bopr.android.smailer;

import android.app.Application;
import android.support.annotation.NonNull;
import android.test.ApplicationTestCase;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * {@link ActivityLog} tester.
 */
public class ActivityLogTest extends ApplicationTestCase<Application> {

    private ActivityLog log;

    public ActivityLogTest() {
        super(Application.class);
        Locale.setDefault(Locale.US);
    }

    @NonNull
    private List<ActivityLogItem> asList(ActivityLog log) {
        List<ActivityLogItem> items = new ArrayList<>();

        ActivityLog.Cursor cursor = log.getAll();
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
        log = new ActivityLog(getContext());
        getContext().deleteDatabase(ActivityLog.DB_NAME);
    }

    public void testDefaults() throws Exception {
        assertEquals(10000, log.getCapacity());
        assertEquals(TimeUnit.DAYS.toMillis(1), log.getPurgePeriod());
    }

    public void testClear() throws Exception {
        log.success(new MailMessage("+79052345671", true, 1000, 0, false, true, null, null));
        log.success(new MailMessage("+79052345672", false, 2000, 0, false, true, null, null));
        log.success(new MailMessage("+79052345673", true, 3000, 0, false, false, null, null));
        log.success(new MailMessage("+79052345674", false, 4000, 0, false, false, null, null));
        log.success(new MailMessage("+79052345675", true, 5000, 0, true, false, null, null));
        log.error(new MailMessage("+79052345671", true, 6000, 0, false, true, null, null), new Exception("Test 1"));
        log.error(new MailMessage("+79052345672", false, 7000, 0, false, true, null, null), new Exception("Test 2"));
        log.error(new MailMessage("+79052345673", true, 8000, 0, false, false, null, null), new Exception("Test 3"));
        log.error(new MailMessage("+79052345674", false, 9000, 0, false, false, null, null), new Exception("Test 4"));
        log.error(new MailMessage("+79052345675", true, 10000, 0, true, false, null, null), new Exception("Test 5"));

        assertEquals(10, log.getAll().getCount());

        log.clear();

        assertEquals(0, log.getAll().getCount());
    }

    public void testAddGet() throws Exception {
        log.success(new MailMessage("+79052345671", true, 1000, 0, false, true, null, null));
        log.success(new MailMessage("+79052345672", false, 2000, 0, false, true, null, null));
        log.success(new MailMessage("+79052345673", true, 3000, 0, false, false, null, null));
        log.success(new MailMessage("+79052345674", false, 4000, 0, false, false, null, null));
        log.success(new MailMessage("+79052345675", true, 5000, 0, true, false, null, null));

        log.error(new MailMessage("+79052345671", true, 6000, 0, false, true, null, null), new Exception("Test 1"));
        log.error(new MailMessage("+79052345672", false, 7000, 0, false, true, null, null), new Exception("Test 2"));
        log.error(new MailMessage("+79052345673", true, 8000, 0, false, false, null, null), new Exception("Test 3"));
        log.error(new MailMessage("+79052345674", false, 9000, 0, false, false, null, null), new Exception("Test 4"));
        log.error(new MailMessage("+79052345675", true, 10000, 0, true, false, null, null), new Exception("Test 5"));

        List<ActivityLogItem> items = asList(log);

        assertEquals(10, items.size());

        /* Descending order! */
        assertEquals("Missed call from +79052345675. Send failed.", items.get(0).getMessage());
        assertEquals("Outgoing call to +79052345674. Send failed.", items.get(1).getMessage());
        assertEquals("Incoming call from +79052345673. Send failed.", items.get(2).getMessage());
        assertEquals("Outgoing SMS to +79052345672. Send failed.", items.get(3).getMessage());
        assertEquals("Incoming SMS from +79052345671. Send failed.", items.get(4).getMessage());

        assertEquals("Missed call from +79052345675. Send success.", items.get(5).getMessage());
        assertEquals("Outgoing call to +79052345674. Send success.", items.get(6).getMessage());
        assertEquals("Incoming call from +79052345673. Send success.", items.get(7).getMessage());
        assertEquals("Outgoing SMS to +79052345672. Send success.", items.get(8).getMessage());
        assertEquals("Incoming SMS from +79052345671. Send success.", items.get(9).getMessage());

        assertEquals("java.lang.Exception: Test 5", items.get(0).getDetails());
        assertEquals("java.lang.Exception: Test 4", items.get(1).getDetails());
        assertEquals("java.lang.Exception: Test 3", items.get(2).getDetails());
        assertEquals("java.lang.Exception: Test 2", items.get(3).getDetails());
        assertEquals("java.lang.Exception: Test 1", items.get(4).getDetails());
    }

    public void testPurge() throws Exception {
        log.success(new MailMessage("1", true, 1000, 0, false, true, null, null));
        log.success(new MailMessage("2", false, 2000, 0, false, true, null, null));
        log.success(new MailMessage("3", true, 3000, 0, false, false, null, null));
        log.success(new MailMessage("4", false, 4000, 0, false, false, null, null));
        log.success(new MailMessage("5", true, 5000, 0, true, false, null, null));
        log.error(new MailMessage("6", true, 6000, 0, false, true, null, null), new Exception("Test 1"));
        log.error(new MailMessage("7", false, 7000, 0, false, true, null, null), new Exception("Test 2"));
        log.error(new MailMessage("8", true, 8000, 0, false, false, null, null), new Exception("Test 3"));
        log.error(new MailMessage("9", false, 9000, 0, false, false, null, null), new Exception("Test 4"));

        /* first we have 9 records */
        assertEquals(9, log.getAll().getCount());

        /* change default capacity and period to small values */
        log.setCapacity(5);
        log.setPurgePeriod(100);

        /* sleep a time that is less than purge period */
        Thread.sleep(10);
        /* and add another record */
        log.error(new MailMessage("10", true, 10000, 0, true, false, null, null), new Exception("Test 5"));

        /* nothing happen cause elapsed time is less than purge period */
        assertEquals(10, log.getAll().getCount());

        /* sleep again */
        Thread.sleep(90);
        /* and add another record */
        log.success(new MailMessage("11", true, 1000, 0, false, true, null, null));

        /* last addition should trigger purge process cause total elapsed time exceeds purge period */
        assertEquals(5, log.getAll().getCount());

        List<ActivityLogItem> items = asList(log);
        assertEquals("Incoming SMS from 11. Send success.", items.get(0).getMessage());
        assertEquals("Missed call from 10. Send failed.", items.get(1).getMessage());
        assertEquals("Outgoing call to 9. Send failed.", items.get(2).getMessage());
        assertEquals("Incoming call from 8. Send failed.", items.get(3).getMessage());
        assertEquals("Outgoing SMS to 7. Send failed.", items.get(4).getMessage());
    }

}