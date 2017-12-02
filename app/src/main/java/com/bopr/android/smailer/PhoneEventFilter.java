package com.bopr.android.smailer;

import java.util.Collections;
import java.util.Set;

/**
 * Class PhoneEventFilter.
 *
 * @author Boris Pronin (<a href="mailto:bpronin@bttprime.com">bpronin@bttprime.com</a>)
 */
class PhoneEventFilter {

    private String pattern;
    private boolean blackListed = true;
    private Set<String> whiteList = Collections.emptySet();
    private Set<String> blackList = Collections.emptySet();

    PhoneEventFilter() {
    }

    String getPattern() {
        return pattern;
    }

    void setPattern(String pattern) {
        this.pattern = pattern;
    }

    boolean isBlackListed() {
        return blackListed;
    }

    void setBlackListed(boolean blackListed) {
        this.blackListed = blackListed;
    }

    Set<String> getWhiteList() {
        return whiteList;
    }

    void setWhiteList(Set<String> whiteList) {
        this.whiteList = whiteList;
    }

    Set<String> getBlackList() {
        return blackList;
    }

    void setBlackList(Set<String> blackList) {
        this.blackList = blackList;
    }

    boolean accept(PhoneEvent event) {
        return acceptPhone(event.getPhone()) && acceptPattern(event.getText());
    }

    private boolean acceptPhone(String phone) {
        return blackListed && !blackList.contains(phone) || whiteList.contains(phone);
    }

    private boolean acceptPattern(String text) {
        return text == null || pattern == null || text.matches(pattern);
    }

}
