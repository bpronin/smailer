package com.bopr.android.smailer;

import org.junit.Test;

import java.util.HashSet;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SmsFilterTest {

    @Test
    public void testText() throws Exception {
        SmsFilter filter = new SmsFilter();
        Sms sms = new Sms();

        filter.setPattern(".*Bob");
        sms.setText("This is a message from Bob");
        assertTrue(filter.accept(sms));

        filter.setPattern("^((?!Bob).)*$");
        sms.setText("This is a message from Bob");
        assertFalse(filter.accept(sms));
    }

    @Test
    public void testBlackList() throws Exception {
        SmsFilter filter = new SmsFilter();
        Sms sms = new Sms();

        filter.setBlackList(new HashSet<>(asList("111", "333")));
        sms.setPhone("111");
        assertFalse(filter.accept(sms));

        sms.setPhone("222");
        assertTrue(filter.accept(sms));

        filter.setBlackList(new HashSet<>(asList("111", "222")));
        sms.setPhone("222");
        assertFalse(filter.accept(sms));
    }

}