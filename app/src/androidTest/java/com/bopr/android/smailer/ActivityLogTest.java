package com.bopr.android.smailer;

import android.app.Application;
import android.test.ApplicationTestCase;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * {@link ActivityLog} tester.
 */
public class ActivityLogTest extends ApplicationTestCase<Application> {

    private ActivityLog log;

    public ActivityLogTest() {
        super(Application.class);
        Locale.setDefault(Locale.US);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        log = ActivityLog.getInstance(getContext());
        log.clear();
    }

    public void testAll() throws Exception {
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

        List<ActivityLogItem> items = new ArrayList<>();

        ActivityLog.Cursor cursor = log.getAll();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            items.add(cursor.get());
            cursor.moveToNext();
        }

        assertEquals(10, items.size());

        /* Descending order! */
        assertEquals("Missed call from +79052345675.\nSend failed.", items.get(0).getMessage());
        assertEquals("Outgoing call to +79052345674.\nSend failed.", items.get(1).getMessage());
        assertEquals("Incoming call from +79052345673.\nSend failed.", items.get(2).getMessage());
        assertEquals("Outgoing SMS to +79052345672.\nSend failed.", items.get(3).getMessage());
        assertEquals("Incoming SMS from +79052345671.\nSend failed.", items.get(4).getMessage());

        assertEquals("Missed call from +79052345675.\nSend success.", items.get(5).getMessage());
        assertEquals("Outgoing call to +79052345674.\nSend success.", items.get(6).getMessage());
        assertEquals("Incoming call from +79052345673.\nSend success.", items.get(7).getMessage());
        assertEquals("Outgoing SMS to +79052345672.\nSend success.", items.get(8).getMessage());
        assertEquals("Incoming SMS from +79052345671.\nSend success.", items.get(9).getMessage());

        assertEquals("java.lang.Exception: Test 5", items.get(0).getDetails());
        assertEquals("java.lang.Exception: Test 4", items.get(1).getDetails());
        assertEquals("java.lang.Exception: Test 3", items.get(2).getDetails());
        assertEquals("java.lang.Exception: Test 2", items.get(3).getDetails());
        assertEquals("java.lang.Exception: Test 1", items.get(4).getDetails());
    }

}