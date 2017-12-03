package com.bopr.android.smailer;

import com.bopr.android.smailer.util.Util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static com.bopr.android.smailer.Settings.*;
import static com.bopr.android.smailer.util.Util.containsPhone;
import static com.bopr.android.smailer.util.Util.isEmpty;

/**
 * Class PhoneEventFilter.
 *
 * @author Boris Pronin (<a href="mailto:bpronin@bttprime.com">bpronin@bttprime.com</a>)
 */
public class PhoneEventFilter {

    private String pattern;
    private boolean useWhiteList;
    private Set<String> whitelist = Collections.emptySet();
    private Set<String> blacklist = Collections.emptySet();
    private Set<String> triggers = Collections.emptySet();

    public PhoneEventFilter() {
    }

    public Set<String> getTriggers() {
        return triggers;
    }

    public void setTriggers(Set<String> triggers) {
        this.triggers = triggers;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public boolean isUseWhiteList() {
        return useWhiteList;
    }

    public void setUseWhiteList(boolean useWhiteList) {
        this.useWhiteList = useWhiteList;
    }

    public Set<String> getWhitelist() {
        return whitelist;
    }

    public void setWhitelist(Collection<String> whitelist) {
        this.whitelist = new HashSet<>(whitelist);
    }

    public Set<String> getBlacklist() {
        return blacklist;
    }

    public void setBlacklist(Collection<String> blacklist) {
        this.blacklist = new HashSet<>(blacklist);
    }

    public boolean accept(PhoneEvent event) {
        return acceptTrigger(event) && acceptPhone(event.getPhone()) && acceptPattern(event.getText());
    }

    private boolean acceptTrigger(PhoneEvent event) {
        if (event.isSms()) {
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

    private boolean acceptPhone(String phone) {
        return useWhiteList ? containsPhone(whitelist, phone) : !containsPhone(blacklist, phone);
    }

    private boolean acceptPattern(String text) {
        return isEmpty(text) || isEmpty(pattern) || text.matches(pattern);
    }

    @Override
    public String toString() {
        return "PhoneEventFilter{" +
                "pattern='" + pattern + '\'' +
                ", useWhiteList=" + useWhiteList +
                ", whitelist=" + whitelist +
                ", blacklist=" + blacklist +
                ", triggers=" + triggers +
                '}';
    }
}
