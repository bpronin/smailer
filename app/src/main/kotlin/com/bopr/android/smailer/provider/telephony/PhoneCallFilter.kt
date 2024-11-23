package com.bopr.android.smailer.provider.telephony

import com.bopr.android.smailer.Settings.Companion.VAL_PREF_TRIGGER_IN_CALLS
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_TRIGGER_IN_SMS
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_TRIGGER_MISSED_CALLS
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_TRIGGER_OUT_CALLS
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_TRIGGER_OUT_SMS
import com.bopr.android.smailer.messenger.Event.Companion.FLAG_BYPASS_NUMBER_BLACKLISTED
import com.bopr.android.smailer.messenger.Event.Companion.FLAG_BYPASS_TEXT_BLACKLISTED
import com.bopr.android.smailer.messenger.Event.Companion.FLAG_BYPASS_TRIGGER_OFF
import com.bopr.android.smailer.util.Bits
import com.bopr.android.smailer.util.phoneNumberToRegEx
import com.bopr.android.smailer.util.stripPhoneNumber
import com.bopr.android.smailer.util.unescapeRegex
import java.util.regex.PatternSyntaxException

/**
 * Filters phone events by various criteria.
 *
 * @author Boris Pronin ([bpronin@bttprime.com](mailto:bpronin@bttprime.com))
 */
class PhoneCallFilter(
    var triggers: Set<String> = emptySet(),
    var phoneBlacklist: Set<String> = emptySet(),
    var phoneWhitelist: Set<String> = emptySet(),
    var textBlacklist: Set<String> = emptySet(),
    var textWhitelist: Set<String> = emptySet()
) {

    /**
     * Tests if the filter accepts given phone call.
     *
     * @param info phone call info
     * @return empty flags if event accepted or explanation flags otherwise
     */
    fun test(info: PhoneCallInfo): Bits {
        var result = Bits()
        if (!testTrigger(info)) result += FLAG_BYPASS_TRIGGER_OFF
        if (!testPhone(info.phone)) result += FLAG_BYPASS_NUMBER_BLACKLISTED
        if (!testText(info.text)) result += FLAG_BYPASS_TEXT_BLACKLISTED
        return result
    }

    private fun testTrigger(info: PhoneCallInfo): Boolean {
        return when {
            triggers.isEmpty() ->
                false

            info.isSms -> {
                when {
                    info.isIncoming ->
                        triggers.contains(VAL_PREF_TRIGGER_IN_SMS)

                    else ->
                        triggers.contains(VAL_PREF_TRIGGER_OUT_SMS)
                }
            }

            else -> {
                when {
                    info.isMissed ->
                        triggers.contains(VAL_PREF_TRIGGER_MISSED_CALLS)

                    info.isIncoming ->
                        triggers.contains(VAL_PREF_TRIGGER_IN_CALLS)

                    else ->
                        triggers.contains(VAL_PREF_TRIGGER_OUT_CALLS)
                }
            }
        }
    }

    private fun testPhone(phone: String): Boolean {
        return matchesPhone(phoneWhitelist, phone) || !matchesPhone(phoneBlacklist, phone)
    }

    private fun testText(text: String?): Boolean {
        return matchesText(textWhitelist, text) || !matchesText(textBlacklist, text)
    }

    private fun matchesPhone(patterns: Collection<String>, phone: String): Boolean {
        val p = stripPhoneNumber(phone)
        for (pt in patterns) {
            if (p.matches(phoneNumberToRegEx(pt).toRegex())) {
                return true
            }
        }
        return false
    }

    private fun matchesText(patterns: Collection<String>, text: String?): Boolean {
        if (!text.isNullOrEmpty()) {
            for (pattern in patterns) {
                val regex = unescapeRegex(pattern)
                if (regex != null) {
                    try {
                        if (text matches Regex(regex)) {
                            return true
                        }
                    } catch (_: PatternSyntaxException) {
                        /* ignore invalid patterns */
                    }
                } else {
                    if (text.contains(pattern, true)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    override fun toString(): String {
        return "PhoneCallFilter{" +
                "triggers=" + triggers +
                ", numberWhitelist=" + phoneWhitelist +
                ", numberBlacklist=" + phoneBlacklist +
                ", textWhitelist=" + textWhitelist +
                ", textBlacklist=" + textBlacklist +
                '}'
    }
}