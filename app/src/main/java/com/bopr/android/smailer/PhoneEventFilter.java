package com.bopr.android.smailer;

import androidx.annotation.NonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import static com.bopr.android.smailer.PhoneEvent.REASON_ACCEPTED;
import static com.bopr.android.smailer.PhoneEvent.REASON_NUMBER_BLACKLISTED;
import static com.bopr.android.smailer.PhoneEvent.REASON_TEXT_BLACKLISTED;
import static com.bopr.android.smailer.PhoneEvent.REASON_TRIGGER_OFF;
import static com.bopr.android.smailer.Settings.VAL_PREF_TRIGGER_IN_CALLS;
import static com.bopr.android.smailer.Settings.VAL_PREF_TRIGGER_IN_SMS;
import static com.bopr.android.smailer.Settings.VAL_PREF_TRIGGER_MISSED_CALLS;
import static com.bopr.android.smailer.Settings.VAL_PREF_TRIGGER_OUT_CALLS;
import static com.bopr.android.smailer.Settings.VAL_PREF_TRIGGER_OUT_SMS;
import static com.bopr.android.smailer.util.AddressUtil.normalizePhone;
import static com.bopr.android.smailer.util.AddressUtil.phoneToRegEx;
import static com.bopr.android.smailer.util.Util.isEmpty;
import static com.bopr.android.smailer.util.Util.unquoteRegex;

/**
 * Filters phone events by various criteria.
 *
 * @author Boris Pronin (<a href="mailto:bpronin@bttprime.com">bpronin@bttprime.com</a>)
 */
@SuppressWarnings("WeakerAccess")
public class PhoneEventFilter {

    private Set<String> triggers = Collections.emptySet();
    private Set<String> phoneWhitelist = Collections.emptySet();
    private Set<String> phoneBlacklist = Collections.emptySet();
    private Set<String> textWhitelist = Collections.emptySet();
    private Set<String> textBlacklist = Collections.emptySet();

    public PhoneEventFilter() {
    }

    public Set<String> getTriggers() {
        return triggers;
    }

    public void setTriggers(Set<String> triggers) {
        this.triggers = triggers;
    }

    public Set<String> getPhoneWhitelist() {
        return phoneWhitelist;
    }

    public void setPhoneWhitelist(Set<String> phoneWhitelist) {
        this.phoneWhitelist = phoneWhitelist;
    }

    public Set<String> getPhoneBlacklist() {
        return phoneBlacklist;
    }

    public void setPhoneBlacklist(Set<String> phoneBlacklist) {
        this.phoneBlacklist = phoneBlacklist;
    }

    public Set<String> getTextWhitelist() {
        return textWhitelist;
    }

    public void setTextWhitelist(Set<String> textWhitelist) {
        this.textWhitelist = textWhitelist;
    }

    public Set<String> getTextBlacklist() {
        return textBlacklist;
    }

    public void setTextBlacklist(Set<String> textBlacklist) {
        this.textBlacklist = textBlacklist;
    }

    /**
     * Tests if the filter accepts given event.
     *
     * @param event event
     * @return {@link PhoneEvent#REASON_ACCEPTED} if event was accepted or reason code if not
     */
    public int test(PhoneEvent event) {
        int reason = REASON_ACCEPTED;
        if (!testTrigger(event)) {
            reason |= REASON_TRIGGER_OFF;
        }
        if (!testPhone(event.getPhone())) {
            reason |= REASON_NUMBER_BLACKLISTED;
        }
        if (!testText(event.getText())) {
            reason |= REASON_TEXT_BLACKLISTED;
        }
        return reason;
    }

    private boolean testTrigger(PhoneEvent event) {
        if (triggers.isEmpty()) {
            return false;
        } else if (event.isSms()) {
            if (event.isIncoming()) {
                return triggers.contains(VAL_PREF_TRIGGER_IN_SMS);
            } else {
                return triggers.contains(VAL_PREF_TRIGGER_OUT_SMS);
            }
        } else {
            if (event.isMissed()) {
                return triggers.contains(VAL_PREF_TRIGGER_MISSED_CALLS);
            } else if (event.isIncoming()) {
                return triggers.contains(VAL_PREF_TRIGGER_IN_CALLS);
            } else {
                return triggers.contains(VAL_PREF_TRIGGER_OUT_CALLS);
            }
        }
    }

    private boolean testPhone(String phone) {
        return matchesPhone(phoneWhitelist, phone) || !matchesPhone(phoneBlacklist, phone);
    }

    private boolean testText(String text) {
        return matchesText(textWhitelist, text) || !matchesText(textBlacklist, text);
    }

    private boolean matchesPhone(Collection<String> patterns, String phone) {
        String p = normalizePhone(phone);
        for (String pattern : patterns) {
            if (p.matches(phoneToRegEx(pattern))) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesText(Collection<String> patterns, String text) {
        if (!isEmpty(text)) {
            for (String s : patterns) {
                String pattern = unquoteRegex(s);
                if (((pattern != null) && text.matches(pattern)) || text.contains(s)) {
                    return true;
                }
            }
        }
        return false;
    }

    @NonNull
    @Override
    public String toString() {
        return "PhoneEventFilter{" +
                "triggers=" + triggers +
                ", numberWhitelist=" + phoneWhitelist +
                ", numberBlacklist=" + phoneBlacklist +
                ", textWhitelist=" + textWhitelist +
                ", textBlacklist=" + textBlacklist +
                '}';
    }

}
