package com.bopr.android.smailer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Class SmsFilter.
 *
 * @author Boris Pronin (<a href="mailto:bpronin@bttprime.com">bpronin@bttprime.com</a>)
 */
class SmsFilter {

    private String pattern;
    private boolean blackListed = true;
    private Set<String> whiteList = Collections.emptySet();
    private Set<String> blackList = Collections.emptySet();

    public SmsFilter() {
    }

    public String getPattern() {
        return pattern;
    }

    void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public boolean isBlackListed() {
        return blackListed;
    }

    public void setBlackListed(boolean blackListed) {
        this.blackListed = blackListed;
    }

    public Set<String> getWhiteList() {
        return whiteList;
    }

    public void setWhiteList(Set<String> whiteList) {
        this.whiteList = whiteList;
    }

    public Set<String> getBlackList() {
        return blackList;
    }

    public void setBlackList(Set<String> blackList) {
        this.blackList = blackList;
    }

    public boolean accept(Sms sms) {
        return acceptPhone(sms.getPhone()) && acceptPattern(sms.getText());
    }

    private boolean acceptPhone(String phone) {
        return blackListed && !blackList.contains(phone) || whiteList.contains(phone);
    }

    private boolean acceptPattern(String text) {
        return text == null || pattern == null || text.matches(pattern);
    }

}
