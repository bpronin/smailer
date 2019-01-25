package com.bopr.android.smailer;

import android.support.annotation.NonNull;

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
    private boolean usePhoneWhitelist;
    private boolean useTextWhitelist;
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

    public boolean isUsePhoneWhitelist() {
        return usePhoneWhitelist;
    }

    public void setUsePhoneWhitelist(boolean usePhoneWhitelist) {
        this.usePhoneWhitelist = usePhoneWhitelist;
    }

    public boolean isUseTextWhitelist() {
        return useTextWhitelist;
    }

    public void setUseTextWhitelist(boolean useTextWhitelist) {
        this.useTextWhitelist = useTextWhitelist;
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
        return usePhoneWhitelist ? isPhoneWhitelisted(phone) : !isPhoneBlacklisted(phone);
    }

    private boolean acceptText(String message) {
        return useTextWhitelist ? containsText(textWhitelist, message) : !containsText(textBlacklist, message);
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

    public boolean isPhoneBlacklisted(String phone) {
        return containsPhone(phoneBlacklist, phone);
    }

    public boolean isPhoneWhitelisted(String phone) {
        return containsPhone(phoneWhitelist, phone);
    }

    private boolean containsText(Collection<String> words, String text) {
        if (!isEmpty(text)) {
            for (String s : words) {
                if (text.contains(s)) {
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
                ", useNumberWhiteList=" + usePhoneWhitelist +
                ", useTextWhiteList=" + useTextWhitelist +
                ", numberWhitelist=" + phoneWhitelist +
                ", numberBlacklist=" + phoneBlacklist +
                ", textWhitelist=" + textWhitelist +
                ", textBlacklist=" + textBlacklist +
                '}';
    }
}
