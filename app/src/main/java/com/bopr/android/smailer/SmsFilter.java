package com.bopr.android.smailer;

import java.util.Collections;
import java.util.Set;

/**
 * Class SmsFilter.
 *
 * @author Boris Pronin (<a href="mailto:bpronin@bttprime.com">bpronin@bttprime.com</a>)
 */
class SmsFilter {

    private String pattern;
    private Set<String> blackList = Collections.emptySet();

    void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public void setBlackList(Set<String> blackList) {
        this.blackList = blackList;
    }

    boolean test(Sms sms) {
        return !blackList.contains(sms.getPhone())
                && (sms.getText() == null || pattern == null || sms.getText().matches(pattern));
    }
}
