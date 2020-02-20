package com.bopr.android.smailer

import com.bopr.android.smailer.PhoneEvent.Companion.REASON_ACCEPTED
import com.bopr.android.smailer.PhoneEvent.Companion.REASON_NUMBER_BLACKLISTED
import com.bopr.android.smailer.PhoneEvent.Companion.REASON_TEXT_BLACKLISTED
import com.bopr.android.smailer.PhoneEvent.Companion.REASON_TRIGGER_OFF
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_TRIGGER_IN_CALLS
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_TRIGGER_IN_SMS
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_TRIGGER_MISSED_CALLS
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_TRIGGER_OUT_CALLS
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_TRIGGER_OUT_SMS
import com.bopr.android.smailer.util.AddressUtil.normalizePhone
import com.bopr.android.smailer.util.AddressUtil.phoneToRegEx
import com.bopr.android.smailer.util.unescapeRegex

/**
 * Filters phone events by various criteria.
 *
 * @author Boris Pronin ([bpronin@bttprime.com](mailto:bpronin@bttprime.com))
 */
class PhoneEventFilter {

    var triggers: MutableSet<String> = mutableSetOf()
    var phoneWhitelist: MutableSet<String> = mutableSetOf()
    var phoneBlacklist: MutableSet<String> = mutableSetOf()
    var textWhitelist: MutableSet<String> = mutableSetOf()
    var textBlacklist: MutableSet<String> = mutableSetOf()

    /**
     * Tests if the filter accepts given event.
     *
     * @param event event
     * @return [REASON_ACCEPTED] if event was accepted or reason code if not
     */
    fun test(event: PhoneEvent): Int {
        var reason = REASON_ACCEPTED
        if (!testTrigger(event)) {
            reason = reason or REASON_TRIGGER_OFF
        }
        if (!testPhone(event.phone)) {
            reason = reason or REASON_NUMBER_BLACKLISTED
        }
        if (!testText(event.text)) {
            reason = reason or REASON_TEXT_BLACKLISTED
        }
        return reason
    }

    private fun testTrigger(event: PhoneEvent): Boolean {
        return when {
            triggers.isEmpty() ->
                false
            event.isSms -> {
                when {
                    event.isIncoming ->
                        triggers.contains(VAL_PREF_TRIGGER_IN_SMS)
                    else ->
                        triggers.contains(VAL_PREF_TRIGGER_OUT_SMS)
                }
            }
            else -> {
                when {
                    event.isMissed ->
                        triggers.contains(VAL_PREF_TRIGGER_MISSED_CALLS)
                    event.isIncoming ->
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
        val p = normalizePhone(phone)
        for (pt in patterns) {
            if (p.matches(phoneToRegEx(pt).toRegex())) {
                return true
            }
        }
        return false
    }

    private fun matchesText(patterns: Collection<String>, text: String?): Boolean {
        if (!text.isNullOrEmpty()) {
            for (pt in patterns) {
                val pattern = unescapeRegex(pt)
                if (pattern != null && text.matches(pattern.toRegex()) || text.contains(pt)) {
                    return true
                }
            }
        }
        return false
    }

    override fun toString(): String {
        return "PhoneEventFilter{" +
                "triggers=" + triggers +
                ", numberWhitelist=" + phoneWhitelist +
                ", numberBlacklist=" + phoneBlacklist +
                ", textWhitelist=" + textWhitelist +
                ", textBlacklist=" + textBlacklist +
                '}'
    }
}