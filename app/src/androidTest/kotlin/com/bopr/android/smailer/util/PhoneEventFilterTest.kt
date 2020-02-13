package com.bopr.android.smailer.util

import com.bopr.android.smailer.PhoneEvent
import com.bopr.android.smailer.PhoneEvent.Companion.REASON_ACCEPTED
import com.bopr.android.smailer.PhoneEvent.Companion.REASON_NUMBER_BLACKLISTED
import com.bopr.android.smailer.PhoneEvent.Companion.REASON_TEXT_BLACKLISTED
import com.bopr.android.smailer.PhoneEvent.Companion.REASON_TRIGGER_OFF
import com.bopr.android.smailer.PhoneEventFilter
import com.bopr.android.smailer.Settings.VAL_PREF_TRIGGER_IN_SMS
import com.bopr.android.smailer.Settings.VAL_PREF_TRIGGER_MISSED_CALLS
import com.bopr.android.smailer.util.TextUtil.quoteRegex
import com.bopr.android.smailer.util.Util.asSet
import org.junit.Assert.assertEquals
import org.junit.Test

class PhoneEventFilterTest {

    @Test
    fun testEmpty() {
        val event = PhoneEvent()
        val filter = PhoneEventFilter()

        assertEquals(REASON_TRIGGER_OFF, filter.test(event))
    }

    @Test
    fun testInSmsTrigger() {
        val filter = PhoneEventFilter()
        filter.triggers = asSet(VAL_PREF_TRIGGER_IN_SMS)

        val event = PhoneEvent()
        event.isIncoming = true
        event.phone = "+123456789"
        event.text = "This is a message for Bob or Ann"

        assertEquals(REASON_ACCEPTED, filter.test(event))
    }

    @Test
    fun testPhoneBlackList() {
        val filter = PhoneEventFilter().apply {
            triggers = asSet(VAL_PREF_TRIGGER_IN_SMS)
            phoneBlacklist = emptySet()
        }

        val event = PhoneEvent().apply {
            text = "This is a message for Bob or Ann"
            isIncoming = true
            phone = "111"
        }

        assertEquals(REASON_ACCEPTED, filter.test(event))

        filter.phoneBlacklist = asSet("111", "333")
        event.phone = "111"

        assertEquals(REASON_NUMBER_BLACKLISTED, filter.test(event))

        filter.phoneBlacklist = asSet("+1(11)", "333")
        event.phone = "1 11"

        assertEquals(REASON_NUMBER_BLACKLISTED, filter.test(event))

        event.phone = "222"

        assertEquals(REASON_ACCEPTED, filter.test(event))

        filter.phoneBlacklist = asSet("111", "222")
        event.phone = "222"

        assertEquals(REASON_NUMBER_BLACKLISTED, filter.test(event))
    }

    @Test
    fun testPhoneBlackListPattern() {
        val filter = PhoneEventFilter().apply {
            triggers = asSet(VAL_PREF_TRIGGER_MISSED_CALLS)
            phoneBlacklist = asSet("+79628810***")
        }

        val event = PhoneEvent().apply {
            isIncoming = true
            isMissed = true
            phone = "+79628810559"
        }

        assertEquals(REASON_NUMBER_BLACKLISTED, filter.test(event))

        event.phone = "+79628810558"

        assertEquals(REASON_NUMBER_BLACKLISTED, filter.test(event))

        event.phone = "+79628811111"

        assertEquals(REASON_ACCEPTED, filter.test(event))
    }

    @Test
    fun testPhoneWhiteList() {
        val filter = PhoneEventFilter().apply {
            triggers = asSet(VAL_PREF_TRIGGER_IN_SMS)
            phoneWhitelist = emptySet()
        }

        val event = PhoneEvent().apply {
            text = "This is a message for Bob or Ann"
            isIncoming = true
            phone = "111"
        }

        assertEquals(REASON_ACCEPTED, filter.test(event))

        filter.phoneWhitelist = asSet("111", "333")
        event.phone = "111"

        assertEquals(REASON_ACCEPTED, filter.test(event))

        event.phone = "222"

        assertEquals(REASON_ACCEPTED, filter.test(event))

        filter.phoneWhitelist = asSet("111", "222")
        event.phone = "222"

        assertEquals(REASON_ACCEPTED, filter.test(event))
    }

    @Test
    fun testTextBlackList() {
        val filter = PhoneEventFilter()
        filter.triggers = asSet(VAL_PREF_TRIGGER_IN_SMS)
        filter.textBlacklist = emptySet()

        val event = PhoneEvent()
        event.phone = "111"
        event.isIncoming = true
        event.text = "This is a message for Bob or Ann"

        assertEquals(REASON_ACCEPTED, filter.test(event))

        filter.textBlacklist = asSet("Bob", "Ann")
        event.text = "This is a message for Bob or Ann"

        assertEquals(REASON_TEXT_BLACKLISTED, filter.test(event))

        filter.textBlacklist = asSet("Bob", "Ann")
        event.text = "This is a message"

        assertEquals(REASON_ACCEPTED, filter.test(event))
    }

    @Test
    fun testTextBlackListPattern() {
        val filter = PhoneEventFilter()
        filter.triggers = asSet(VAL_PREF_TRIGGER_IN_SMS)
        filter.textBlacklist = asSet(quoteRegex("(.*)Bob(.*)"))

        val event = PhoneEvent()
        event.phone = "111"
        event.isIncoming = true
        event.text = "This is a message for Bob or Ann"

        assertEquals(REASON_TEXT_BLACKLISTED, filter.test(event))

        filter.textBlacklist = asSet(quoteRegex("(.*)John(.*)"))
        event.text = "This is a message for Bob or Ann"

        assertEquals(REASON_ACCEPTED, filter.test(event))

        filter.textBlacklist = asSet("(.*)John(.*)")
        event.text = "This is a message for (.*)John(.*)"

        assertEquals(REASON_TEXT_BLACKLISTED, filter.test(event))
    }

    @Test
    fun testTextWhiteList() {
        val filter = PhoneEventFilter()
        filter.triggers = asSet(VAL_PREF_TRIGGER_IN_SMS)
        filter.textWhitelist = emptySet()

        val event = PhoneEvent()
        event.phone = "111"
        event.isIncoming = true
        event.text = "This is a message for Bob or Ann"

        assertEquals(REASON_ACCEPTED, filter.test(event))

        filter.textWhitelist = asSet("Bob", "Ann")
        event.text = "This is a message for Bob or Ann"

        assertEquals(REASON_ACCEPTED, filter.test(event))

        filter.textWhitelist = asSet("Bob", "Ann")
        event.text = "This is a message"

        assertEquals(REASON_ACCEPTED, filter.test(event))
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