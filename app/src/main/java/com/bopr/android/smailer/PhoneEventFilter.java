package com.bopr.android.smailer;

import android.support.annotation.NonNull;

import com.bopr.android.smailer.util.Util;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import static com.bopr.android.smailer.Settings.VAL_PREF_TRIGGER_IN_CALLS;
import static com.bopr.android.smailer.Settings.VAL_PREF_TRIGGER_IN_SMS;
import static com.bopr.android.smailer.Settings.VAL_PREF_TRIGGER_MISSED_CALLS;
import static com.bopr.android.smailer.Settings.VAL_PREF_TRIGGER_OUT_CALLS;
import static com.bopr.android.smailer.Settings.VAL_PREF_TRIGGER_OUT_SMS;
import static com.bopr.android.smailer.util.Util.isEmpty;
import static com.bopr.android.smailer.util.Util.normalizePhone;
import static com.bopr.android.smailer.util.Util.normalizePhonePattern;

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

    public boolean accept(PhoneEvent event) {
        return acceptTrigger(event)
                && acceptPhone(event.getPhone())
                && acceptText(event.getText());
    }

    public boolean acceptTrigger(PhoneEvent event) {
        if (triggers.isEmpty()) {
            return true;
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

    public boolean acceptPhone(String phone) {
        return containsPhone(phoneWhitelist, phone) || !containsPhone(phoneBlacklist, phone);
    }

    public boolean acceptText(String text) {
        return containsText(textWhitelist, text) || !containsText(textBlacklist, text);
    }

    private boolean containsPhone(Collection<String> patterns, String phone) {
        String p = normalizePhone(phone);
        for (String pattern : patterns) {
            if (p.matches(normalizePhonePattern(pattern))) {
                return true;
            }
        }
        return false;
    }

    private boolean containsText(Collection<String> patterns, String text) {
        if (!isEmpty(text)) {
            for (String pattern : patterns) {
                String p = Util.unquoteRegex(pattern);
                if (p != null && text.matches(p)) {
                    return true;
                } else if (text.contains(pattern)) {
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
