package com.bopr.android.smailer;

import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SmsFilterTest {

    @Test
    public void testEmpty() throws Exception {
        SmsFilter filter = new SmsFilter();
        Sms sms = new Sms();
        sms.setText("This is a message for Bob or Ann");
        assertTrue(filter.accept(sms));
    }

    @Test
    public void testText() throws Exception {
        SmsFilter filter = new SmsFilter();
        Sms sms = new Sms();

        sms.setText("This is a message for Bob or Ann");
        filter.setPattern(".*(Bob|Ann).*");
        assertTrue(filter.accept(sms));

        filter.setPattern(".*(bob|ann).*");
        assertFalse(filter.accept(sms));

        filter.setPattern("(?i).*(bob|ann).*");
        assertTrue(filter.accept(sms));

        sms.setText("This is a message from Bob or Ann");
        filter.setPattern("^((?!Bob).)*$");
        assertFalse(filter.accept(sms));
    }

    @Test
    public void testBlackList() throws Exception {
        SmsFilter filter = new SmsFilter();
        filter.setBlackListed(true);
        Sms sms = new Sms();

        filter.setBlackList(Collections.<String>emptySet());
        sms.setPhone("111");
        assertTrue(filter.accept(sms));

        filter.setBlackList(new HashSet<>(asList("111", "333")));
        sms.setPhone("111");
        assertFalse(filter.accept(sms));

        sms.setPhone("222");
        assertTrue(filter.accept(sms));

        filter.setBlackList(new HashSet<>(asList("111", "222")));
        sms.setPhone("222");
        assertFalse(filter.accept(sms));
    }

    @Test
    public void testWhiteList() throws Exception {
        SmsFilter filter = new SmsFilter();
        filter.setBlackListed(false);
        Sms sms = new Sms();

        filter.setBlackList(Collections.<String>emptySet());
        sms.setPhone("111");
        assertFalse(filter.accept(sms));

        filter.setWhiteList(new HashSet<>(asList("111", "333")));
        sms.setPhone("111");
        assertTrue(filter.accept(sms));

        sms.setPhone("222");
        assertFalse(filter.accept(sms));

        filter.setWhiteList(new HashSet<>(asList("111", "222")));
        sms.setPhone("222");
        assertTrue(filter.accept(sms));
    }

}