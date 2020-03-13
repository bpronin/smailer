package com.bopr.android.smailer.util

import androidx.test.filters.SmallTest
import com.bopr.android.smailer.BaseTest
import com.bopr.android.smailer.PhoneEvent
import com.bopr.android.smailer.PhoneEvent.Companion.STATUS_ACCEPTED
import com.bopr.android.smailer.PhoneEvent.Companion.STATUS_NUMBER_BLACKLISTED
import com.bopr.android.smailer.PhoneEvent.Companion.STATUS_TEXT_BLACKLISTED
import com.bopr.android.smailer.PhoneEvent.Companion.STATUS_TRIGGER_OFF
import com.bopr.android.smailer.PhoneEventFilter
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_TRIGGER_IN_SMS
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_TRIGGER_MISSED_CALLS
import org.junit.Assert.assertEquals
import org.junit.Test

@SmallTest
class PhoneEventFilterTest : BaseTest() {

    private fun createEvent(phone: String, acceptor: String = "Device",
                            startTime: Long = 1000,
                            isIncoming: Boolean = true,
                            isMissed: Boolean = true,
                            isRead: Boolean = true,
                            text: String? = null): PhoneEvent {
        return PhoneEvent(
                phone = phone,
                isIncoming = isIncoming,
                startTime = startTime,
                isMissed = isMissed,
                text = text,
                acceptor = acceptor,
                isRead = isRead
        )
    }

    @Test
    fun testEmpty() {
        val event = createEvent("123")
        val filter = PhoneEventFilter()

        assertEquals(STATUS_TRIGGER_OFF, filter.test(event))
    }

    @Test
    fun testIncomingSmsTrigger() {
        val filter = PhoneEventFilter()
        filter.triggers = setOf(VAL_PREF_TRIGGER_IN_SMS)

        val event = createEvent(
                isIncoming = true,
                phone = "+123456789",
                text = "This is a message for Bob or Ann"
        )

        assertEquals(STATUS_ACCEPTED, filter.test(event))
    }

    @Test
    fun testPhoneBlacklist() {
        val filter = PhoneEventFilter().apply {
            triggers = setOf(VAL_PREF_TRIGGER_IN_SMS)
        }

        var event = createEvent(
                text = "This is a message for Bob or Ann",
                isIncoming = true,
                phone = "111"
        )

        assertEquals(STATUS_ACCEPTED, filter.test(event))

        filter.phoneBlacklist = listOf("111", "333")
        event = event.copy(phone = "111")

        assertEquals(STATUS_NUMBER_BLACKLISTED, filter.test(event))

        filter.phoneBlacklist = listOf("+1(11)", "333")
        event = event.copy(phone = "1 11")

        assertEquals(STATUS_NUMBER_BLACKLISTED, filter.test(event))

        event = event.copy(phone = "222")

        assertEquals(STATUS_ACCEPTED, filter.test(event))

        filter.phoneBlacklist = listOf("111", "222")
        event = event.copy(phone = "222")

        assertEquals(STATUS_NUMBER_BLACKLISTED, filter.test(event))
    }

    @Test
    fun testPhoneBlacklistPattern() {
        val filter = PhoneEventFilter().apply {
            triggers = setOf(VAL_PREF_TRIGGER_MISSED_CALLS)
            phoneBlacklist = listOf("+79628810***")
        }

        var event = createEvent(
                isIncoming = true,
                isMissed = true,
                phone = "+79628810559"
        )

        assertEquals(STATUS_NUMBER_BLACKLISTED, filter.test(event))

        event = event.copy(phone = "+79628810558")

        assertEquals(STATUS_NUMBER_BLACKLISTED, filter.test(event))

        event = event.copy(phone = "+79628811111")

        assertEquals(STATUS_ACCEPTED, filter.test(event))
    }

    @Test
    fun testPhoneWhitelist() {
        val filter = PhoneEventFilter(
            triggers = setOf(VAL_PREF_TRIGGER_IN_SMS)
        )

        var event = createEvent(
                text = "This is a message for Bob or Ann",
                isIncoming = true,
                phone = "111"
        )

        assertEquals(STATUS_ACCEPTED, filter.test(event))

        filter.phoneWhitelist = listOf("111", "333")
        event = event.copy(phone = "111")

        assertEquals(STATUS_ACCEPTED, filter.test(event))

        event = event.copy(phone = "222")

        assertEquals(STATUS_ACCEPTED, filter.test(event))

        filter.phoneWhitelist = listOf("111", "222")
        event = event.copy(phone = "222")

        assertEquals(STATUS_ACCEPTED, filter.test(event))
    }

    @Test
    fun testTextBlacklist() {
        var event = createEvent(
                phone = "111",
                isIncoming = true,
                text = "This is a message for Bob or Ann"
        )

        val filter = PhoneEventFilter(
            triggers = setOf(VAL_PREF_TRIGGER_IN_SMS)
        )
        assertEquals(STATUS_ACCEPTED, filter.test(event))

        event = event.copy(text = "This is a message for Bob or Ann")
        filter.textBlacklist = listOf("Bob", "Ann")
        assertEquals(STATUS_TEXT_BLACKLISTED, filter.test(event))

        event = event.copy(text = "This is a message for Bobson or Ann")
        filter.textBlacklist = listOf("BOB")
        assertEquals(STATUS_TEXT_BLACKLISTED, filter.test(event))

        event = event.copy(text = "This is a message for Bob or Ann")
        filter.textBlacklist = listOf("bob")
        assertEquals(STATUS_TEXT_BLACKLISTED, filter.test(event))

        event = event.copy(text = "This is a message")
        filter.textBlacklist = listOf("Bob", "Ann")
        assertEquals(STATUS_ACCEPTED, filter.test(event))
    }

    @Test
    fun testTextBlacklistRegex() {
        var event = createEvent(
                phone = "111",
                isIncoming = true,
                text = "This is a message for Bob or Ann"
        )
        val filter = PhoneEventFilter().apply {
            triggers = setOf(VAL_PREF_TRIGGER_IN_SMS)
        }
        assertEquals(STATUS_ACCEPTED, filter.test(event))

        filter.textBlacklist = listOf(escapeRegex("REGEX:.*John.*"))
        event = event.copy(text = "This is a message for Bob or Ann")
        assertEquals(STATUS_ACCEPTED, filter.test(event))

        filter.textBlacklist = listOf("REX:(.*)John(.*)", "REGEX:.*someone.*", "REGEX:.*other*")
        event = event.copy(text = "This is a message for John or someone else")
        assertEquals(STATUS_TEXT_BLACKLISTED, filter.test(event))

        filter.textBlacklist = listOf("REGEX:(?i:.*SOMEONE.*)")
        event = event.copy(text = "This is a message for John or someone else")
        assertEquals(STATUS_TEXT_BLACKLISTED, filter.test(event))

        filter.textBlacklist = listOf("REGEX:?i:.*SOMEONE.*") /* invalid pattern */
        event = event.copy(text = "This is a message for John or someone else")
        assertEquals(STATUS_ACCEPTED, filter.test(event))
    }

    @Test
    fun testTextWhitelist() {
        val filter = PhoneEventFilter()
        filter.triggers = setOf(VAL_PREF_TRIGGER_IN_SMS)
        filter.textWhitelist = listOf()

        var event = createEvent(
                phone = "111",
                isIncoming = true,
                text = "This is a message for Bob or Ann"
        )
        assertEquals(STATUS_ACCEPTED, filter.test(event))

        filter.textWhitelist = listOf("Bob", "Ann")
        event = event.copy(text = "This is a message for Bob or Ann")
        assertEquals(STATUS_ACCEPTED, filter.test(event))

        filter.textWhitelist = listOf("Bob", "Ann")
        event = event.copy(text = "This is a message")
        assertEquals(STATUS_ACCEPTED, filter.test(event))
    }
}