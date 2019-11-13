package com.bopr.android.smailer;

import org.junit.Test;

import java.util.Collections;

import static com.bopr.android.smailer.PhoneEvent.REASON_ACCEPT;
import static com.bopr.android.smailer.PhoneEvent.REASON_NUMBER_BLACKLISTED;
import static com.bopr.android.smailer.PhoneEvent.REASON_TEXT_BLACKLISTED;
import static com.bopr.android.smailer.Settings.VAL_PREF_TRIGGER_IN_SMS;
import static com.bopr.android.smailer.util.Util.asSet;
import static com.bopr.android.smailer.util.Util.quoteRegex;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class PhoneEventFilterTest {

    @Test
    public void testEmpty() {
        PhoneEvent event = new PhoneEvent();
        PhoneEventFilter filter = new PhoneEventFilter();

        try {
            filter.test(event);
            fail();
        } catch (NullPointerException ok) {
            /* ok */
        }
    }

    @Test
    public void testInSmsTrigger() {
        PhoneEvent event = new PhoneEvent();
        event.setIncoming(true);
        event.setPhone("+123456789");
        event.setText("This is a message for Bob or Ann");

        PhoneEventFilter filter = new PhoneEventFilter();
        filter.setTriggers(asSet(VAL_PREF_TRIGGER_IN_SMS));

        assertEquals(REASON_ACCEPT, filter.test(event));
    }

    @Test
    public void testPhoneBlackList() {
        PhoneEventFilter filter = new PhoneEventFilter();
        filter.setTriggers(asSet(VAL_PREF_TRIGGER_IN_SMS));

        PhoneEvent event = new PhoneEvent();
        event.setText("This is a message for Bob or Ann");
        event.setIncoming(true);

        filter.setPhoneBlacklist(Collections.<String>emptySet());
        event.setPhone("111");
        assertEquals(REASON_ACCEPT, filter.test(event));

        filter.setPhoneBlacklist(asSet("111", "333"));
        event.setPhone("111");
        assertEquals(REASON_NUMBER_BLACKLISTED, filter.test(event));

        filter.setPhoneBlacklist(asSet("+1(11)", "333"));
        event.setPhone("1 11");
        assertEquals(REASON_NUMBER_BLACKLISTED, filter.test(event));

        event.setPhone("222");
        assertEquals(REASON_ACCEPT, filter.test(event));

        filter.setPhoneBlacklist(asSet("111", "222"));
        event.setPhone("222");
        assertEquals(REASON_NUMBER_BLACKLISTED, filter.test(event));
    }

    @Test
    public void testPhoneBlackListPattern() {
        PhoneEventFilter filter = new PhoneEventFilter();
//        filter.setTriggers(asSet(VAL_PREF_TRIGGER_IN_SMS));
        filter.setPhoneBlacklist(asSet("+79628810***"));

        PhoneEvent event = new PhoneEvent();
        event.setIncoming(true);
        event.setMissed(true);

        event.setPhone("+79628810559");
        assertEquals(REASON_NUMBER_BLACKLISTED, filter.test(event));

        event.setPhone("+79628810558");
        assertEquals(REASON_NUMBER_BLACKLISTED, filter.test(event));

        event.setPhone("+79628811111");
        assertEquals(REASON_ACCEPT, filter.test(event));
    }

    @Test
    public void testPhoneWhiteList() {
        PhoneEventFilter filter = new PhoneEventFilter();
        filter.setTriggers(asSet(VAL_PREF_TRIGGER_IN_SMS));

        PhoneEvent event = new PhoneEvent();
        event.setText("This is a message for Bob or Ann");
        event.setIncoming(true);

        filter.setPhoneWhitelist(Collections.<String>emptySet());
        event.setPhone("111");
        assertEquals(REASON_ACCEPT, filter.test(event));

        filter.setPhoneWhitelist(asSet("111", "333"));
        event.setPhone("111");
        assertEquals(REASON_ACCEPT, filter.test(event));

        event.setPhone("222");
        assertEquals(REASON_ACCEPT, filter.test(event));

        filter.setPhoneWhitelist(asSet("111", "222"));
        event.setPhone("222");
        assertEquals(REASON_ACCEPT, filter.test(event));
    }

    @Test
    public void testTextBlackList() {
        PhoneEventFilter filter = new PhoneEventFilter();
        filter.setTriggers(asSet(VAL_PREF_TRIGGER_IN_SMS));

        PhoneEvent event = new PhoneEvent();
        event.setPhone("111");
        event.setIncoming(true);

        filter.setTextBlacklist(Collections.<String>emptySet());
        event.setText("This is a message for Bob or Ann");
        assertEquals(REASON_ACCEPT, filter.test(event));

        filter.setTextBlacklist(asSet("Bob", "Ann"));
        event.setText("This is a message for Bob or Ann");
        assertEquals(REASON_TEXT_BLACKLISTED, filter.test(event));

        filter.setTextBlacklist(asSet("Bob", "Ann"));
        event.setText("This is a message");
        assertEquals(REASON_ACCEPT, filter.test(event));
    }

    @Test
    public void testTextBlackListPattern() {
        PhoneEventFilter filter = new PhoneEventFilter();
        filter.setTriggers(asSet(VAL_PREF_TRIGGER_IN_SMS));

        PhoneEvent event = new PhoneEvent();
        event.setPhone("111");
        event.setIncoming(true);

        filter.setTextBlacklist(asSet(quoteRegex("(.*)Bob(.*)")));
        event.setText("This is a message for Bob or Ann");
        assertEquals(REASON_TEXT_BLACKLISTED, filter.test(event));

        filter.setTextBlacklist(asSet(quoteRegex("(.*)John(.*)")));
        event.setText("This is a message for Bob or Ann");
        assertEquals(REASON_ACCEPT, filter.test(event));

        filter.setTextBlacklist(asSet("(.*)John(.*)"));
        event.setText("This is a message for (.*)John(.*)");
        assertEquals(REASON_TEXT_BLACKLISTED, filter.test(event));
    }

    @Test
    public void testTextWhiteList() {
        PhoneEventFilter filter = new PhoneEventFilter();
        filter.setTriggers(asSet(VAL_PREF_TRIGGER_IN_SMS));

        PhoneEvent event = new PhoneEvent();
        event.setPhone("111");
        event.setIncoming(true);

        filter.setTextWhitelist(Collections.<String>emptySet());
        event.setText("This is a message for Bob or Ann");
        assertEquals(REASON_ACCEPT, filter.test(event));

        filter.setTextWhitelist(asSet("Bob", "Ann"));
        event.setText("This is a message for Bob or Ann");
        assertEquals(REASON_ACCEPT, filter.test(event));

        filter.setTextWhitelist(asSet("Bob", "Ann"));
        event.setText("This is a message");
        assertEquals(REASON_ACCEPT, filter.test(event));
    }

//    @Test
//    public void testPhonePattern() {
//        PhoneEventFilter filter = new PhoneEventFilter();
//        PhoneEvent event = new PhoneEvent();
//
//        filter.setPhoneBlacklist(".*(Bob|Ann).*");
//        event.setText("+79628810559");
//        event.setText("+79628810559");
//        assertTrue(filter.accept(event));
//    }
}