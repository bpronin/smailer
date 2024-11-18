package com.bopr.android.smailer.util

import androidx.test.filters.SmallTest
import com.bopr.android.smailer.BaseTest
import com.bopr.android.smailer.provider.telephony.PhoneCallInfo
import com.bopr.android.smailer.provider.telephony.PhoneCallInfo.Companion.FLAG_BYPASS_NONE
import com.bopr.android.smailer.provider.telephony.PhoneCallInfo.Companion.FLAG_BYPASS_NUMBER_BLACKLISTED
import com.bopr.android.smailer.provider.telephony.PhoneCallInfo.Companion.FLAG_BYPASS_TEXT_BLACKLISTED
import com.bopr.android.smailer.provider.telephony.PhoneCallInfo.Companion.FLAG_BYPASS_TRIGGER_OFF
import com.bopr.android.smailer.provider.telephony.PhoneCallFilter
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_TRIGGER_IN_SMS
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_TRIGGER_MISSED_CALLS
import org.junit.Assert.assertEquals
import org.junit.Test

@SmallTest
class PhoneCallFilterTest : BaseTest() {

    private fun createCallInfo(phone: String, acceptor: String = "Device",
                               startTime: Long = 1000,
                               isIncoming: Boolean = true,
                               isMissed: Boolean = true,
                               isRead: Boolean = true,
                               text: String? = null): PhoneCallInfo {
        return PhoneCallInfo(
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
        val call = createCallInfo("123")
        val filter = PhoneCallFilter()

        assertEquals(FLAG_BYPASS_TRIGGER_OFF, filter.test(call))
    }

    @Test
    fun testIncomingSmsTrigger() {
        val filter = PhoneCallFilter()
        filter.triggers = setOf(VAL_PREF_TRIGGER_IN_SMS)

        val call = createCallInfo(
                isIncoming = true,
                phone = "+123456789",
                text = "This is a message for Bob or Ann"
        )

        assertEquals(FLAG_BYPASS_NONE, filter.test(call))
    }

    @Test
    fun testPhoneBlacklist() {
        val filter = PhoneCallFilter().apply {
            triggers = setOf(VAL_PREF_TRIGGER_IN_SMS)
        }

        var call = createCallInfo(
                text = "This is a message for Bob or Ann",
                isIncoming = true,
                phone = "111"
        )

        assertEquals(FLAG_BYPASS_NONE, filter.test(call))

        filter.phoneBlacklist = setOf("111", "333")
        call = call.copy(phone = "111")

        assertEquals(FLAG_BYPASS_NUMBER_BLACKLISTED, filter.test(call))

        filter.phoneBlacklist = setOf("+1(11)", "333")
        call = call.copy(phone = "1 11")

        assertEquals(FLAG_BYPASS_NUMBER_BLACKLISTED, filter.test(call))

        call = call.copy(phone = "222")

        assertEquals(FLAG_BYPASS_NONE, filter.test(call))

        filter.phoneBlacklist = setOf("111", "222")
        call = call.copy(phone = "222")

        assertEquals(FLAG_BYPASS_NUMBER_BLACKLISTED, filter.test(call))
    }

    @Test
    fun testPhoneBlacklistPattern() {
        val filter = PhoneCallFilter().apply {
            triggers = setOf(VAL_PREF_TRIGGER_MISSED_CALLS)
            phoneBlacklist = setOf("+79628810***")
        }

        var call = createCallInfo(
                isIncoming = true,
                isMissed = true,
                phone = "+79628810559"
        )

        assertEquals(FLAG_BYPASS_NUMBER_BLACKLISTED, filter.test(call))

        call = call.copy(phone = "+79628810558")

        assertEquals(FLAG_BYPASS_NUMBER_BLACKLISTED, filter.test(call))

        call = call.copy(phone = "+79628811111")

        assertEquals(FLAG_BYPASS_NONE, filter.test(call))
    }

    @Test
    fun testPhoneWhitelist() {
        val filter = PhoneCallFilter(
                triggers = setOf(VAL_PREF_TRIGGER_IN_SMS)
        )

        var call = createCallInfo(
                text = "This is a message for Bob or Ann",
                isIncoming = true,
                phone = "111"
        )

        assertEquals(FLAG_BYPASS_NONE, filter.test(call))

        filter.phoneWhitelist = setOf("111", "333")
        call = call.copy(phone = "111")

        assertEquals(FLAG_BYPASS_NONE, filter.test(call))

        call = call.copy(phone = "222")

        assertEquals(FLAG_BYPASS_NONE, filter.test(call))

        filter.phoneWhitelist = setOf("111", "222")
        call = call.copy(phone = "222")

        assertEquals(FLAG_BYPASS_NONE, filter.test(call))
    }

    @Test
    fun testTextBlacklist() {
        var call = createCallInfo(
                phone = "111",
                isIncoming = true,
                text = "This is a message for Bob or Ann"
        )

        val filter = PhoneCallFilter(
                triggers = setOf(VAL_PREF_TRIGGER_IN_SMS)
        )
        assertEquals(FLAG_BYPASS_NONE, filter.test(call))

        call = call.copy(text = "This is a message for Bob or Ann")
        filter.textBlacklist = setOf("Bob", "Ann")
        assertEquals(FLAG_BYPASS_TEXT_BLACKLISTED, filter.test(call))

        call = call.copy(text = "This is a message for Bobson or Ann")
        filter.textBlacklist = setOf("BOB")
        assertEquals(FLAG_BYPASS_TEXT_BLACKLISTED, filter.test(call))

        call = call.copy(text = "This is a message for Bob or Ann")
        filter.textBlacklist = setOf("bob")
        assertEquals(FLAG_BYPASS_TEXT_BLACKLISTED, filter.test(call))

        call = call.copy(text = "This is a message")
        filter.textBlacklist = setOf("Bob", "Ann")
        assertEquals(FLAG_BYPASS_NONE, filter.test(call))
    }

    @Test
    fun testTextBlacklistRegex() {
        var call = createCallInfo(
                phone = "111",
                isIncoming = true,
                text = "This is a message for Bob or Ann"
        )
        val filter = PhoneCallFilter().apply {
            triggers = setOf(VAL_PREF_TRIGGER_IN_SMS)
        }
        assertEquals(FLAG_BYPASS_NONE, filter.test(call))

        filter.textBlacklist = setOf(escapeRegex("REGEX:.*John.*"))
        call = call.copy(text = "This is a message for Bob or Ann")
        assertEquals(FLAG_BYPASS_NONE, filter.test(call))

        filter.textBlacklist = setOf("REX:(.*)John(.*)", "REGEX:.*someone.*", "REGEX:.*other*")
        call = call.copy(text = "This is a message for John or someone else")
        assertEquals(FLAG_BYPASS_TEXT_BLACKLISTED, filter.test(call))

        filter.textBlacklist = setOf("REGEX:(?i:.*SOMEONE.*)")
        call = call.copy(text = "This is a message for John or someone else")
        assertEquals(FLAG_BYPASS_TEXT_BLACKLISTED, filter.test(call))

        filter.textBlacklist = setOf("REGEX:?i:.*SOMEONE.*") /* invalid pattern */
        call = call.copy(text = "This is a message for John or someone else")
        assertEquals(FLAG_BYPASS_NONE, filter.test(call))
    }

    @Test
    fun testTextWhitelist() {
        val filter = PhoneCallFilter()
        filter.triggers = setOf(VAL_PREF_TRIGGER_IN_SMS)
        filter.textWhitelist = setOf()

        var call = createCallInfo(
                phone = "111",
                isIncoming = true,
                text = "This is a message for Bob or Ann"
        )
        assertEquals(FLAG_BYPASS_NONE, filter.test(call))

        filter.textWhitelist = setOf("Bob", "Ann")
        call = call.copy(text = "This is a message for Bob or Ann")
        assertEquals(FLAG_BYPASS_NONE, filter.test(call))

        filter.textWhitelist = setOf("Bob", "Ann")
        call = call.copy(text = "This is a message")
        assertEquals(FLAG_BYPASS_NONE, filter.test(call))
    }
}