package com.bopr.android.smailer;

import android.app.Application;
import android.test.ApplicationTestCase;

import java.util.Locale;
import java.util.TimeZone;

/**
 * Base tester.
 */
public abstract class BaseTest extends ApplicationTestCase<Application> {

    protected BaseTest() {
        super(Application.class);
        Locale.setDefault(Locale.US);
        TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"));
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("dexmaker.dexcache", getContext().getCacheDir().getPath());
    }

}

