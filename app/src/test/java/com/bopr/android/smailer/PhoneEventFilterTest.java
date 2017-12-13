package com.bopr.android.smailer;

import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PhoneEventFilterTest {

    @Test
    public void testEmpty() throws Exception {
        PhoneEventFilter filter = new PhoneEventFilter();
        PhoneEvent event = new PhoneEvent();
        event.setText("This is a message for Bob or Ann");
        assertTrue(filter.accept(event));
    }

    @Test
    public void testText() throws Exception {
        PhoneEventFilter filter = new PhoneEventFilter();
        PhoneEvent event = new PhoneEvent();

        event.setText("This is a message for Bob or Ann");
        filter.setPattern(".*(Bob|Ann).*");
        assertTrue(filter.accept(event));

        filter.setPattern(".*(bob|ann).*");
        assertFalse(filter.accept(event));

        filter.setPattern("(?i).*(bob|ann).*");
        assertTrue(filter.accept(event));

        event.setText("This is a message from Bob or Ann");
        filter.setPattern("^((?!Bob).)*$");
        assertFalse(filter.accept(event));
    }

    @Test
    public void testBlackList() throws Exception {
        PhoneEventFilter filter = new PhoneEventFilter();
        filter.setUseNumberWhiteList(false);
        PhoneEvent event = new PhoneEvent();

        filter.setNumberWhitelist(Collections.<String>emptySet());
        filter.setNumberBlacklist(Collections.<String>emptySet());
        event.setPhone("111");
        assertTrue(filter.accept(event));

        filter.setNumberBlacklist(new HashSet<>(asList("111", "333")));
        event.setPhone("111");
        assertFalse(filter.accept(event));

        filter.setNumberBlacklist(new HashSet<>(asList("+1(11)", "333")));
        event.setPhone("1 11");
        assertFalse(filter.accept(event));

        event.setPhone("222");
        assertTrue(filter.accept(event));

        filter.setNumberBlacklist(new HashSet<>(asList("111", "222")));
        event.setPhone("222");
        assertFalse(filter.accept(event));
    }

    @Test
    public void testWhiteList() throws Exception {
        PhoneEventFilter filter = new PhoneEventFilter();
        filter.setUseNumberWhiteList(true);
        PhoneEvent event = new PhoneEvent();

        filter.setNumberWhitelist(Collections.<String>emptySet());
        filter.setNumberBlacklist(Collections.<String>emptySet());
        event.setPhone("111");
        assertFalse(filter.accept(event));

        filter.setNumberWhitelist(new HashSet<>(asList("111", "333")));
        event.setPhone("111");
        assertTrue(filter.accept(event));

        event.setPhone("222");
        assertFalse(filter.accept(event));

        filter.setNumberWhitelist(new HashSet<>(asList("111", "222")));
        event.setPhone("222");
        assertTrue(filter.accept(event));
    }

}