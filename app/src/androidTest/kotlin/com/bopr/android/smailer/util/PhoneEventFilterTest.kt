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

    private fun createEvent(phone: String, acceptor: String = "Device",
                            startTime: Long = 1000,
                            isIncoming: Boolean = true,
                            isMissed: Boolean = true,
                            isRead: Boolean = true,
                            text: String? = null): PhoneEvent {
        return PhoneEvent(
                phone = phone,
                acceptor = acceptor,
                startTime = startTime,
                isIncoming = isIncoming,
                isMissed = isMissed,
                isRead = isRead,
                text = text
        )
    }

    @Test
    fun testEmpty() {
        val event = createEvent("123")
        val filter = PhoneEventFilter()

        assertEquals(REASON_TRIGGER_OFF, filter.test(event))
    }

    @Test
    fun testInSmsTrigger() {
        val filter = PhoneEventFilter()
        filter.triggers = asSet(VAL_PREF_TRIGGER_IN_SMS)

        val event = createEvent(
                isIncoming = true,
                phone = "+123456789",
                text = "This is a message for Bob or Ann"
        )

        assertEquals(REASON_ACCEPTED, filter.test(event))
    }

    @Test
    fun testPhoneBlackList() {
        val filter = PhoneEventFilter().apply {
            triggers = asSet(VAL_PREF_TRIGGER_IN_SMS)
            phoneBlacklist = mutableSetOf()
        }

        var event = createEvent(
                text = "This is a message for Bob or Ann",
                isIncoming = true,
                phone = "111"
        )

        assertEquals(REASON_ACCEPTED, filter.test(event))

        filter.phoneBlacklist = asSet("111", "333")
        event = event.copy(phone = "111")

        assertEquals(REASON_NUMBER_BLACKLISTED, filter.test(event))

        filter.phoneBlacklist = asSet("+1(11)", "333")
        event = event.copy(phone = "1 11")

        assertEquals(REASON_NUMBER_BLACKLISTED, filter.test(event))

        event = event.copy(phone = "222")

        assertEquals(REASON_ACCEPTED, filter.test(event))

        filter.phoneBlacklist = asSet("111", "222")
        event = event.copy(phone = "222")

        assertEquals(REASON_NUMBER_BLACKLISTED, filter.test(event))
    }

    @Test
    fun testPhoneBlackListPattern() {
        val filter = PhoneEventFilter().apply {
            triggers = asSet(VAL_PREF_TRIGGER_MISSED_CALLS)
            phoneBlacklist = asSet("+79628810***")
        }

        var event = createEvent(
                isIncoming = true,
                isMissed = true,
                phone = "+79628810559"
        )

        assertEquals(REASON_NUMBER_BLACKLISTED, filter.test(event))

        event = event.copy(phone = "+79628810558")

        assertEquals(REASON_NUMBER_BLACKLISTED, filter.test(event))

        event = event.copy(phone = "+79628811111")

        assertEquals(REASON_ACCEPTED, filter.test(event))
    }

    @Test
    fun testPhoneWhiteList() {
        val filter = PhoneEventFilter().apply {
            triggers = asSet(VAL_PREF_TRIGGER_IN_SMS)
            phoneWhitelist = mutableSetOf()
        }

        var event = createEvent(
                text = "This is a message for Bob or Ann",
                isIncoming = true,
                phone = "111"
        )

        assertEquals(REASON_ACCEPTED, filter.test(event))

        filter.phoneWhitelist = asSet("111", "333")
        event = event.copy(phone = "111")

        assertEquals(REASON_ACCEPTED, filter.test(event))

        event = event.copy(phone = "222")

        assertEquals(REASON_ACCEPTED, filter.test(event))

        filter.phoneWhitelist = asSet("111", "222")
        event = event.copy(phone = "222")

        assertEquals(REASON_ACCEPTED, filter.test(event))
    }

    @Test
    fun testTextBlackList() {
        val filter = PhoneEventFilter()
        filter.triggers = asSet(VAL_PREF_TRIGGER_IN_SMS)
        filter.textBlacklist = mutableSetOf()

        var event = createEvent(
                phone = "111",
                isIncoming = true,
                text = "This is a message for Bob or Ann"
        )
        assertEquals(REASON_ACCEPTED, filter.test(event))

        filter.textBlacklist = asSet("Bob", "Ann")
        event = event.copy(text = "This is a message for Bob or Ann")

        assertEquals(REASON_TEXT_BLACKLISTED, filter.test(event))

        filter.textBlacklist = asSet("Bob", "Ann")
        event = event.copy(text = "This is a message")

        assertEquals(REASON_ACCEPTED, filter.test(event))
    }

    @Test
    fun testTextBlackListPattern() {
        val filter = PhoneEventFilter()
        filter.triggers = asSet(VAL_PREF_TRIGGER_IN_SMS)
        filter.textBlacklist = asSet(quoteRegex("(.*)Bob(.*)"))

        var event = createEvent(
                phone = "111",
                isIncoming = true,
                text = "This is a message for Bob or Ann"
        )
        assertEquals(REASON_TEXT_BLACKLISTED, filter.test(event))

        filter.textBlacklist = asSet(quoteRegex("(.*)John(.*)"))
        event = event.copy(text = "This is a message for Bob or Ann")

        assertEquals(REASON_ACCEPTED, filter.test(event))

        filter.textBlacklist = asSet("(.*)John(.*)")
        event = event.copy(text = "This is a message for (.*)John(.*)")

        assertEquals(REASON_TEXT_BLACKLISTED, filter.test(event))
    }

    @Test
    fun testTextWhiteList() {
        val filter = PhoneEventFilter()
        filter.triggers = asSet(VAL_PREF_TRIGGER_IN_SMS)
        filter.textWhitelist = mutableSetOf()

        var event = createEvent(
                phone = "111",
                isIncoming = true,
                text = "This is a message for Bob or Ann"
        )
        assertEquals(REASON_ACCEPTED, filter.test(event))

        filter.textWhitelist = asSet("Bob", "Ann")
        event = event.copy(text = "This is a message for Bob or Ann")

        assertEquals(REASON_ACCEPTED, filter.test(event))

        filter.textWhitelist = asSet("Bob", "Ann")
        event = event.copy(text = "This is a message")

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