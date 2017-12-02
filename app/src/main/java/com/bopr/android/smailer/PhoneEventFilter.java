package com.bopr.android.smailer;

import com.bopr.android.smailer.util.Util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Class PhoneEventFilter.
 *
 * @author Boris Pronin (<a href="mailto:bpronin@bttprime.com">bpronin@bttprime.com</a>)
 */
public class PhoneEventFilter {

    private String pattern;
    private boolean blackListed = true;
    private Set<String> whitelist = Collections.emptySet();
    private Set<String> blacklist = Collections.emptySet();

    public PhoneEventFilter() {
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public boolean isBlackListed() {
        return blackListed;
    }

    public void setBlackListed(boolean blackListed) {
        this.blackListed = blackListed;
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
        return acceptPhone(event.getPhone()) && acceptPattern(event.getText());
    }

    private boolean acceptPhone(String phone) {
        return blackListed && !containsPhone(blacklist, phone) || containsPhone(whitelist, phone);
    }

    private boolean containsPhone(Set<String> set, String phone) {
        return Util.containsPhone(set, phone);
    }

    private boolean acceptPattern(String text) {
        return text == null || pattern == null || text.matches(pattern);
    }

    @Override
    public String toString() {
        return "PhoneEventFilter{" +
                "pattern='" + pattern + '\'' +
                ", blackListed=" + blackListed +
                ", whiteList=" + whitelist +
                ", blackList=" + blacklist +
                '}';
    }
}
