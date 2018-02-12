package com.bopr.android.smailer;

import org.junit.Test;

import java.util.Collections;

import static com.bopr.android.smailer.Settings.VAL_PREF_TRIGGER_IN_SMS;
import static com.bopr.android.smailer.util.Util.asSet;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PhoneEventFilterTest {

    @Test
    public void testEmpty() throws Exception {
        PhoneEvent event = new PhoneEvent();
        PhoneEventFilter filter = new PhoneEventFilter();
        assertFalse(filter.accept(event));
    }

    @Test
    public void testInSmsTrigger() throws Exception {
        PhoneEvent event = new PhoneEvent();
        event.setIncoming(true);
        event.setPhone("+123456789");
        event.setText("This is a message for Bob or Ann");

        PhoneEventFilter filter = new PhoneEventFilter();
        filter.setTriggers(asSet(VAL_PREF_TRIGGER_IN_SMS));

        assertTrue(filter.accept(event));
    }

/*
    @Test
    public void testPattern() throws Exception {
        PhoneEventFilter filter = new PhoneEventFilter();
        PhoneEvent event = new PhoneEvent();

        event.setText("This is a message for Bob or Ann");
        filter.getTPattern(".*(Bob|Ann).*");
        assertTrue(filter.accept(event));

        filter.setPattern(".*(bob|ann).*");
        assertFalse(filter.accept(event));

        filter.setPattern("(?i).*(bob|ann).*");
        assertTrue(filter.accept(event));

        event.setText("This is a message from Bob or Ann");
        filter.setPattern("^((?!Bob).)*$");
        assertFalse(filter.accept(event));
    }
*/

    @Test
    public void testPhoneBlackList() throws Exception {
        PhoneEventFilter filter = new PhoneEventFilter();
        filter.setTriggers(asSet(VAL_PREF_TRIGGER_IN_SMS));
        filter.setUsePhoneWhitelist(false);

        PhoneEvent event = new PhoneEvent();
        event.setText("This is a message for Bob or Ann");
        event.setIncoming(true);

        filter.setPhoneBlacklist(Collections.<String>emptySet());
        event.setPhone("111");
        assertTrue(filter.accept(event));

        filter.setPhoneBlacklist(asSet("111", "333"));
        event.setPhone("111");
        assertFalse(filter.accept(event));

        filter.setPhoneBlacklist(asSet("+1(11)", "333"));
        event.setPhone("1 11");
        assertFalse(filter.accept(event));

        event.setPhone("222");
        assertTrue(filter.accept(event));

        filter.setPhoneBlacklist(asSet("111", "222"));
        event.setPhone("222");
        assertFalse(filter.accept(event));
    }

    @Test
    public void testPhoneWhiteList() throws Exception {
        PhoneEventFilter filter = new PhoneEventFilter();
        filter.setTriggers(asSet(VAL_PREF_TRIGGER_IN_SMS));
        filter.setUsePhoneWhitelist(true);

        PhoneEvent event = new PhoneEvent();
        event.setText("This is a message for Bob or Ann");
        event.setIncoming(true);

        filter.setPhoneWhitelist(Collections.<String>emptySet());
        event.setPhone("111");
        assertFalse(filter.accept(event));

        filter.setPhoneWhitelist(asSet("111", "333"));
        event.setPhone("111");
        assertTrue(filter.accept(event));

        event.setPhone("222");
        assertFalse(filter.accept(event));

        filter.setPhoneWhitelist(asSet("111", "222"));
        event.setPhone("222");
        assertTrue(filter.accept(event));
    }

    @Test
    public void testTextBlackList() throws Exception {
        PhoneEventFilter filter = new PhoneEventFilter();
        filter.setTriggers(asSet(VAL_PREF_TRIGGER_IN_SMS));
        filter.setUseTextWhitelist(false);

        PhoneEvent event = new PhoneEvent();
        event.setPhone("111");
        event.setIncoming(true);

        filter.setTextBlacklist(Collections.<String>emptySet());
        event.setText("This is a message for Bob or Ann");
        assertTrue(filter.accept(event));

        filter.setTextBlacklist(asSet("Bob", "Ann"));
        event.setText("This is a message for Bob or Ann");
        assertFalse(filter.accept(event));

        filter.setTextBlacklist(asSet("Bob", "Ann"));
        event.setText("This is a message");
        assertTrue(filter.accept(event));
    }

    @Test
    public void testTextWhiteList() throws Exception {
        PhoneEventFilter filter = new PhoneEventFilter();
        filter.setTriggers(asSet(VAL_PREF_TRIGGER_IN_SMS));
        filter.setUseTextWhitelist(true);

        PhoneEvent event = new PhoneEvent();
        event.setPhone("111");
        event.setIncoming(true);

        filter.setTextWhitelist(Collections.<String>emptySet());
        event.setText("This is a message for Bob or Ann");
        assertFalse(filter.accept(event));

        filter.setTextWhitelist(asSet("Bob", "Ann"));
        event.setText("This is a message for Bob or Ann");
        assertTrue(filter.accept(event));

        filter.setTextWhitelist(asSet("Bob", "Ann"));
        event.setText("This is a message");
        assertFalse(filter.accept(event));
    }
}