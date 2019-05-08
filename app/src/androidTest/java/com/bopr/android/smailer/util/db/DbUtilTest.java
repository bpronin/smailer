package com.bopr.android.smailer.util.db;

import android.util.Log;

import com.bopr.android.smailer.BaseTest;
import com.bopr.android.smailer.Database;
import com.bopr.android.smailer.PhoneEvent;

import org.junit.Test;

import java.util.List;

public class DbUtilTest extends BaseTest {

    @Test
    public void testCopyTable() {
        Database database = new Database(getContext(), "smailer-bo-pho.sqlite");
        List<PhoneEvent> list = database.getEvents().toList();
        Log.d(TAG, "testCopyTable: " + list);
        database.close();
    }
}